package cs.graph;

import java.util.LinkedList;
import java.util.List;

import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.MethodInstance;
import polyglot.types.PrimitiveType;
import polyglot.types.ProcedureInstance;
import cs.data.ConstraintContainer;
import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.data.id.SpecialID;
import cs.data.id.VariableGenerator;
import cs.data.reflect.ReflectionHandler;
import cs.linearsolver.ValueHolder;
import cs.types.CSBaseType;
import cs.util.CSUtil;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

/* 
 * This method expander can expand both regular call and constructor call
 */

public class MethodExpander implements GraphBuilder {
	private InstID instID;
	private CallConstraint call;

	public MethodExpander(InstID instID, CallConstraint call) {
		this.instID = instID;
		this.call = call;
	}

	public String toString() {
		return "";// "Expand call. T: " + instID.uniqueID() + " C: " +
					// call.desc();
	}

	/*
	 * method expand based on the target's instID and name of the method
	 */
	@Override
	public void buildGraph(CSGraph graph) {
		((CSSimpleGraph) graph).addReachableMethod(call.targetClass());

		String[] classNames = null;

		// process reflection
		if (instID.className().equals(ReflectionHandler.UNKNOWN_NAME)) {
			/*
			 * if the className is unknown, get the concrete className based on
			 * the target's type
			 */
			String castedClass = call.targetClass();
			classNames = ConstraintContainer.getInstance().getConcreteClasses(
					castedClass);

			// TODO this only return the very first concrete class, should it
			// return ALL of the possibility?
			if (classNames.length > 0)
				classNames = new String[] { classNames[0] };
		} else {
			classNames = new String[] { instID.className() };
		}

		/*
		 * 1. avoid invoking ignorable calls. Otherwise, create a context and
		 * put the method body of the call into the list
		 * 
		 * Which are ignorable calls
		 * 
		 * 2. java library method if process java library code is not required
		 * 
		 * 3. benchmark specific methods which are not important but take us a
		 * long time to make them run*
		 */
		List<GraphBuilder> tasks = new LinkedList<GraphBuilder>();

		for (String className : classNames) {

			// the class where the method exist
			String realClass = ConstraintContainer.getInstance()
					.translateMethod(className, call.target(),
							call.methodName());
			if (realClass.equals(ConstraintContainer.IGNORE))
				return;

			// create the context based on the instID and call
			AnalysisContext newContext = AnalysisContext.createNewContext(
					instID, call.invocationPoint());

			// get the method instance
			ProcedureInstance pInst = ConstraintContainer.getInstance()
					.getProcedureInstance(realClass, call.methodName());
			assert pInst != null : "realClass: " + realClass + "  method: "
					+ call.methodName();

			List<ID> params = new LinkedList<ID>();
			// get params
			for (Object param : pInst.formalTypes()) {
				ID paramID = ((CSBaseType) param).getID();

				params.add(paramID.refresh(newContext));
			}

			if (/* should never happen */
			call.arguments().size() < params.size()
					||
					/* should not happen for non-vararg case */
					(!JL5Flags.isVarArgs(pInst.flags()) && call.arguments()
							.size() > params.size())) {
				assert false : "the # of call's argument and param doesn't match";
			}

			/*
			 * 3.5 flow from argument to formal
			 */
			ValueHolder vh = graph.getTypeSystem().getValueHolder();
			for (int i = 0; i < call.arguments().size(); i++) {
				GraphElement from = (GraphElement) call.arguments().get(i);

				/*
				 * For vararg cases, the # of call's argument can be more than
				 * parameters
				 */
				ID param = i < params.size() ? params.get(i) : params
						.get(params.size() - 1);

				if (vh.ifOptimizablePri(CSUtil.getSimpleVar(from.uniqueID()))
						|| vh.ifOptimizablePri(CSUtil.getSimpleVar(param
								.uniqueID()))) {
					continue;
				}

				GraphElement to = new TargetableGraphElement(
						(GraphElement) param, (GraphElement) call.target());

				graph.addFlow(from, to);
			}

			/*
			 * 3.6 flow from formal return to call value for MethodInstance
			 * currently, the flow only happens when the return is not primitive
			 * type
			 */
			if (pInst instanceof MethodInstance
			/*
			 * && !(((MethodInstance) pInst).returnType() instanceof
			 * PrimitiveType)
			 */) {
				MethodInstance mInst = (MethodInstance) pInst;
				ID formalRet = ((CSBaseType) mInst.returnType()).getID();
				GraphElement from = new TargetableGraphElement(
						(GraphElement) formalRet.refresh(newContext),
						(GraphElement) call.target());

				GraphElement callValue = new GraphID(call.invocationPoint());
				if (!vh.ifOptimizablePri(CSUtil.getSimpleVar(from.uniqueID()))
						&& !vh.ifOptimizablePri(CSUtil.getSimpleVar(callValue
								.uniqueID()))) {
					graph.addFlow(from, callValue);
				}
			}

			// 3.4. find method list from container.
			List<Constraint> methodList = ConstraintContainer.getInstance()
					.getMethodList(realClass, call.methodName());

			// refresh method body
			for (Constraint cons : methodList) {
				tasks.add((GraphBuilder) cons.refresh(newContext));
			}

			/*
			 * Special logic for constructor call
			 * 
			 * if the constructor's target is not specialID, it means it is from
			 * creating a new object.
			 * 
			 * the global constraints should be added at this time
			 */
			if (call.isConstructorCall()
					&& !(call.target() instanceof SpecialID)) {
				// add class level constraints
				assert call.target() == instID;
				// 3.1 get global
				List<Constraint> global = ConstraintContainer.getInstance()
						.getMethodList(className, CallConstraint.GLOBAL_NAME);

				// 3.2 refresh the global
				AnalysisContext context = AnalysisContext.createNewContext(
						instID, CallConstraint.GLOBAL_NAME);
				for (Constraint cons : global) {
					tasks.add((GraphBuilder) cons.refresh(context));
				}
			}
			// if the call is constructor call and the first statement in it is
			// not constructor call, insert "super()"
			if (call.isConstructorCall() && methodList.size() > 0) {
				Constraint cons = methodList.get(0);
				if (cons instanceof CallConstraint
						&& ((CallConstraint) cons).isConstructorCall()
						&& ((CallConstraint) cons).target() instanceof SpecialID)
					;
				else {
					// create super();
					AnalysisContext context = AnalysisContext.createNewContext(
							instID, call.invocationPoint());

					ID target = new SpecialID(SpecialID.TYPE.SUPER, realClass,
							className + "@super()", context);

					CallConstraint constructorCall = new CallConstraint(target,
							className, CallConstraint.CONSTRUCTOR_NAME,
							CSUtil.EMPTY_LIST, context);

					tasks.add(constructorCall);
				}
			}

			// 3.8 if the call itself is suer(...), add the super class' level
			// constraints
			if (call.isConstructorCall() && call.target() instanceof SpecialID
					&& ((SpecialID) call.target()).isSuper()) {

				List<Constraint> global = ConstraintContainer.getInstance()
						.getMethodList(realClass, CallConstraint.GLOBAL_NAME);
				for (Constraint cons : global) {
					tasks.add((GraphBuilder) cons.refresh(AnalysisContext
							.createNewContext(instID, call.invocationPoint())));
				}
			}
		}

		graph.addGraphBuilders(tasks);
	}
}
