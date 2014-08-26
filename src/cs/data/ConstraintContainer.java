/*
 * 
 */
package cs.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context_c;
import polyglot.types.InitializerInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.constraint.FlowConstraint;
import cs.data.constraint.FlowConstraint.FLOWTYPE;
import cs.data.id.FieldID;
import cs.data.id.ID;
import cs.data.id.SpecialID;
import cs.data.id.StaticID;
import cs.data.reflect.ReflectionHandler;
import cs.linearsolver.ValueHolder;
import cs.util.CSUtil;
import cs.util.EasyDebugger;

public class ConstraintContainer {
	public static final String MAIN_METHOD_NAME = "main"
			+ ConstraintContainer.METHOD_SEPARATOR + "java.lang.String[]"
			+ ConstraintContainer.ARG_SEPARATOR;
	public static final String CLASS_SEPARATOR = "$";
	public static final String METHOD_SEPARATOR = "#";
	public static final String ARG_SEPARATOR = "|";
	public static final String IGNORE = "!IGNOREMETHOD";

	private static final String GLOBAL_STATIC = "@globalStatic";

	/*
	 * all of statements in different methods key: name of class value: set
	 * containing all of methods' statement
	 */
	private Map<String, Map<String, List<Constraint>>> wholeList;

	private ClassHierarchy clsHrchy;

	private Map<String, ProcedureInstance> mInstanceMap;

	// private Stack<ContextInfo> context;

	private static ConstraintContainer container = new ConstraintContainer();

	public static ConstraintContainer getInstance() {
		return container;
	}

	protected ConstraintContainer() {
		this.wholeList = new HashMap<String, Map<String, List<Constraint>>>();
		mInstanceMap = new HashMap<String, ProcedureInstance>();
		// context = new Stack<ContextInfo>();
		this.clsHrchy = new ClassHierarchy();
	}

	private class ContextInfo {
		protected final String className;
		protected final String methodName;

		public ContextInfo(String className, String methodName) {
			assert (className != null) && (methodName != null);

			this.className = className;
			this.methodName = methodName;
		}
	}

	public String toString(ValueHolder vh) {
		StringBuilder sb = new StringBuilder("");
		for (String className : wholeList.keySet()) {
			sb.append("In class: ").append(className).append("\n");
			// EasyDebugger.message(sb.toString());
			Map<String, List<Constraint>> classMap = wholeList.get(className);

			for (String methodName : classMap.keySet()) {
				sb.append("   ").append(methodName).append("\n");
				List<Constraint> methodList = classMap.get(methodName);

				for (Constraint cons : methodList) {
					sb.append("      ").append(cons.toString(vh)).append("\n");
				}
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	public String methodListOfClass(String className) {
		StringBuilder sb = new StringBuilder("Methods in ").append(className)
				.append("\n");

		if (wholeList.containsKey(className)) {
			for (String methodName : wholeList.get(className).keySet()) {
				sb.append(methodName).append("\n");
			}
		}

		return sb.toString();
	}

	private List<Constraint> getList(ContextInfo info) {
		if (!wholeList.containsKey(info.className)) {
			wholeList.put(info.className,
					new HashMap<String, List<Constraint>>());
		}
		Map<String, List<Constraint>> classMap = wholeList.get(info.className);

		if (!classMap.containsKey(info.methodName)) {
			classMap.put(info.methodName, new LinkedList<Constraint>());
		}

		return classMap.get(info.methodName);
	}

	// check if the constraint is a flow to a static field
	private boolean isStaticField(Constraint cons, Context_c context) {
		if (cons instanceof FlowConstraint) {
			FlowConstraint flow = (FlowConstraint) cons;
			if (!(flow.getTo() instanceof FieldID)) {
				return false;
			}

			String fieldName = ((FieldID) flow.getTo()).fieldName();
			try {
				return context.findField(fieldName).flags().isStatic();
			} catch (SemanticException e) {
				e.printStackTrace();
				assert false : "cannot find field: " + fieldName;
				return false;
			}
		} else {
			return false;
		}
	}

	/*
	 * add an empty list that represent the current block to the container
	 * 
	 * This method is not necessary. It is only used to make sure each
	 * method/block are visited and at the second pass, the specific method can
	 * always be found. Otherwise, an empty method or default constructor decl
	 * would not be found from here
	 * 
	 * The reason of this method is to help find possible bugs
	 */
	public void addBlockContainer(Context_c context) {
		String className = context.currentClass().fullName();

		if (context.currentCode() instanceof ProcedureInstance) {
			getList(new ContextInfo(className,
					ConstraintContainer
							.buildMethodName((JL5ProcedureInstance) context
									.currentCode())));
		} else {
			assert false : context.currentCode().getClass();
		}
	}

	public void addFlowConstraint(ID to, ID from, FLOWTYPE flowType,
			Context_c context, ValueHolder vh) {
		// optimizer check
		if (vh != null && (vh.ifOptimizablePri(to.pgmLabel())
				|| vh.ifOptimizablePri(from.pgmLabel()))) {

			return;
		}
		this.addConstraint(new FlowConstraint(to, from, flowType), context);
	}

	public void addCallConstraint(ID target, String targetClass,
			String methodName, List<ID> args, Context_c context, ValueHolder vh) {
		this.addCallConstraint(new CallConstraint(target, targetClass,
				methodName, args, context), context, vh);
	}

	public void addCallConstraint(CallConstraint call, Context_c context,
			ValueHolder vh) {
		if (vh != null && vh.ifOptimizablePri(call.target().pgmLabel())
				|| call.target() instanceof StaticID
				&& (((StaticID) call.target()).className().equals(
						"java.lang.Boolean")
						|| ((StaticID) call.target()).className().equals(
								"java.lang.Character")
						|| ((StaticID) call.target()).className().equals(
								"java.lang.Integer") || ((StaticID) call
							.target()).className().equals("java.lang.Float"))) {
			return;
		}
		this.addConstraint(call, context);
	}
	
	// add a constraint to the container based on the current context
	public void addConstraint(Constraint cons, Context_c context) {
		assert cons != null && context != null;

		List<Constraint> container = null;

		if (context.currentClass() != null) {
			String className = context.currentClass().fullName();

			// class level
			if (context.isClass()) {
				// static
				if (context.inStaticContext()
						|| this.isStaticField(cons, context)) {
					container = this.getStaticContainer();
				}
				// non-static
				else {
					container = this.getList(new ContextInfo(className,
							CallConstraint.GLOBAL_NAME));
				}
			}
			// block level, including initializer or method
			else {
				CodeInstance cInst = context.currentCode();
				// class level && static
				if (cInst instanceof InitializerInstance
						&& context.inStaticContext()) {
					container = this.getStaticContainer();
				}
				// class level && non-static
				else if (cInst instanceof InitializerInstance
						&& !context.inStaticContext()) {
					container = this.getList(new ContextInfo(className,
							CallConstraint.GLOBAL_NAME));
				} else if (cInst instanceof ProcedureInstance) {
					String methodName = ConstraintContainer
							.buildMethodName((JL5ProcedureInstance) cInst);
					container = this.getList(new ContextInfo(className,
							methodName));
				} else {
					assert false : "Cannot find correct context to store constraint: "
							+ cInst.getClass();
				}
			}

		} else {
			assert false : "Cannot find correct context to store constraint";
		}

		container.add(cons);
	}

	public ClassHierarchy getClassHierarchy() {
		return clsHrchy;
	}

	public List<Constraint> getStaticContainer() {
		return this.getList(new ContextInfo(ConstraintContainer.GLOBAL_STATIC,
				ConstraintContainer.GLOBAL_STATIC));
	}

	public void putProcedureInstance(Context_c context, ProcedureInstance pInst) {
		String className = context.currentClass().fullName();
		String methodName = this.buildMethodName((JL5ProcedureInstance) pInst);

		this.mInstanceMap.put(className + ConstraintContainer.CLASS_SEPARATOR
				+ methodName, pInst);
	}

	/*
	 * return the method instance by the given className and methodName.
	 * 
	 * Note: the className may be the subclass, so looking up the super class is
	 * necessary to find out the method instance
	 */
	public ProcedureInstance getProcedureInstance(String className,
			String methodName) {
		assert className != null : "Cannot find methodInstance of "
				+ methodName + " in class " + className;

		String queryStr = className + ConstraintContainer.CLASS_SEPARATOR
				+ methodName;
		return this.mInstanceMap.get(queryStr);
	}

	/*
	 * All of below methods are used in second pass
	 */
	public String[] getConcreteClasses(String interfaceName) {
		Set<String> concreteSet = clsHrchy.getConcreteClasses(interfaceName);

		String[] strs = new String[concreteSet.size()];
		concreteSet.toArray(strs);

		return strs;
	}

	// try to automatically get the methodList of main
	public List<Constraint> getEntryMethodList(String className,
			String methodName) {
		assert className != null && methodName != null;

		// find from the specified class&method
		if (wholeList.containsKey(className)
				&& wholeList.get(className).containsKey(methodName)) {
			return wholeList.get(className).get(methodName);
		}

		// // otherwise, find the default method --
		// // static void main(String[] args))
		// for (String tmpClassName : wholeList.keySet()) {
		// for (String tmpMethodName : wholeList.get(tmpClassName).keySet()) {
		// if (tmpMethodName.equals(MAIN_METHOD_NAME))
		// return wholeList.get(tmpClassName).get(tmpMethodName);
		// }
		// }
		EasyDebugger.exit("Cannot find the required method: " + className
 + "'s "
						+ methodName + "  " + this.methodListOfClass(className));
		return null;
	}

	public String superClass(String className) {
		return clsHrchy.getSuperName(className);
	}

	// /*
	// * given the class name and method name, return the list of constraint
	// * should be processed for constructor call
	// *
	// * all of the current and super class's constructor call and class level
	// * constraints should be added
	// */
	// public List<Constraint> getConstructorList(String className,
	// String methodName) {
	// List<Constraint> list = new LinkedList<Constraint>();
	//
	// assert false;
	// return list;
	// }

	/*
	 * give the className and methodName, return the className that really have
	 * this method the return can be the same as the parameter or the superClass
	 * of the parameter
	 * 
	 * @param: className: instance class name
	 * 
	 * @return: it must not return a null
	 */
	public String translateMethod(String instClassName, ID target,
			String methodName) {
		assert instClassName != null && methodName != null : "translateMethod: parameter cannot be null";

		String realName = instClassName;
		if (target instanceof SpecialID && ((SpecialID) target).isSuper()) {
			realName = clsHrchy.getSuperName(((SpecialID) target)
					.currentClass());
		}

		// temporarily used
		String subName = null;
		while (realName != null) {
			// All of the ignorable method should comes here
			if (Properties.ignore(realName)) {
				return IGNORE;
			} else if (realName.startsWith("<anonymous sub")
					&& methodName.equals(CallConstraint.CONSTRUCTOR_NAME)) {
				return IGNORE;
			} else if (realName.equals(ReflectionHandler.UNKNOWN_NAME)) {
				return IGNORE;
			} else if (CSUtil.ignorable(realName, methodName)) {
				return IGNORE;
			}

			if (getMethodList(realName, methodName) != null) {
				break;
			}

			subName = realName;
			realName = clsHrchy.getSuperName(realName);

		}

		if (realName == null) {
			// this happens when a java lib class extends from another lib
			// class, which is not parsed from source code
			if (Properties.ignore(subName)/*
										 * subName.startsWith("java.") &&
										 * !Properties.PROCESS_JAVALIB
										 */)
				return IGNORE;

			return IGNORE;
			// assert false : "Cannon find method: "
			// + instClassName
			// + ((target instanceof SpecialID) ? "@" + target.toString()
			// : "") + "@" + methodName + "\n"
			// + methodListOfClass(instClassName);
		}

		return realName;
	}// end method

	/*
	 * return: constraints in the method body if the method body is not found,
	 * an empty list will be returned
	 */
	public List<Constraint> getMethodList(String className, String methodName) {
		// check parameters
		assert className != null && methodName != null : "Container: parameter cannot be null";

		List<Constraint> methodList = null;
		if (wholeList.containsKey(className)) {
			Map<String, List<Constraint>> temp = wholeList.get(className);
			methodList = temp.get(methodName);

			// it is possible the global level constraint set is null
			if (methodList == null
					&& methodName.equals(CallConstraint.GLOBAL_NAME)) {
				methodList = CSUtil.EMPTY_LIST;
			}
		}
		// else {
		// if (className.startsWith("java.") && !Properties.PROCESS_JAVALIB)
		// return CSUtil.EMPTY_LIST;
		// }
		return methodList;
	}

	// build an unique name for each method, so that the method can be found in
	// second pass
	public static String buildMethodName(ProcedureInstance pInst) {
		StringBuilder sb = new StringBuilder("");

		// get methodName
		if (pInst instanceof ConstructorInstance) {
			sb.append(CallConstraint.CONSTRUCTOR_NAME);
		} else {
			sb.append(((MethodInstance) pInst).name());
		}

		if (pInst.formalTypes().size() > 0) {
			sb.append(ConstraintContainer.METHOD_SEPARATOR);
			// get formalType names
			for (Object formalType : pInst.formalTypes()) {
				sb.append(((Type) formalType).toType()).append(
						ConstraintContainer.ARG_SEPARATOR);
			}
		}

		return sb.toString();
	}
}
