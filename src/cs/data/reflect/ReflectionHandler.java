package cs.data.reflect;

import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.ClassLit;
import polyglot.ast.Expr;
import polyglot.ast.NewArray;
import polyglot.ast.StringLit;
import polyglot.types.ClassType;

import cs.data.constraint.CallConstraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.util.CSUtil;

/*
 * A centralized class handle reflection related static analysis
 */
public class ReflectionHandler {
	public static final boolean PROCESS_REFLECTION = true;
	public static final String UNKNOWN_NAME = "~UNKNOWN_NAME";

	/*
	 * return the target's className if the target is classType and call's
	 * method name
	 */
	private static String[] getInfo(Call call) {
		String targetType = null;
		String methodName = null;

		if (call.target().type() instanceof ClassType) {
			targetType = ((ClassType) call.target().type()).fullName();
			methodName = call.name();
		} else {
			// give invalid names as a useless result
			targetType = "$$$$";
			methodName = "$$$$";
		}
		return new String[] { targetType, methodName };
	}

	/*
	 * capture "Class.forName" method and create a InstID
	 */
	public static ID captureForName(String id, Call call) {
		String[] info = getInfo(call);
		String targetType = info[0];
		String methodName = info[1];

		if (targetType.equals("java.lang.Class")
				&& methodName.equals("forName")) {
			String className = ReflectionHandler.getStringValue((Expr) call
					.arguments().get(0));

			return new InstID(id, className, call.position().toString()
					+ " REFLECTION");
		} else {
			return null;
		}
	}

	//
	public static ReflectID captureReflection(String id, ID target, Call call) {
		ReflectID data = null;

		String[] info = getInfo(call);
		String targetType = info[0];
		String methodName = info[1];

		if (targetType.equals("java.lang.Class")) {
			if (methodName.equals("newInstance"))
				return ReflectionHandler.captureNewInstance(id, target,
						call.arguments());
		} else if (targetType.equals("java.lang.reflect.Constructor")) {
			if (methodName.equals("newInstance"))
				return ReflectionHandler.captureNewInstance(id, target,
						call.arguments());
		} else if (targetType.equals("java.lang.reflect.Method")) {
			if (methodName.equals("invoke"))
				return ReflectionHandler.captureMethodInvoke(id, target,
						call.arguments());
		}
		return data;
	}

	/*
	 * Case: class.getMethod(String methodName, Object args...) and
	 * class.getConstructor(Object args...) share the same logic
	 */
	public static InstID captureGetMethodOrConstructor(String id, ID target,
			Call call) {
		String[] info = getInfo(call);
		String targetType = info[0];
		String methodName = info[1];

		if (targetType.equals("java.lang.Class")
				&& ((methodName.equals("getMethod") || methodName
						.equals("getConstructor")))) {
			// safe guard
			assert call.arguments().size() >= 1;

			// 1. get method name/if constructor, just set it
			String signature = null;
			List<Expr> arguments = null;
			if (call.name().equals("getMethod")) {
				// get method name if lit_string
				signature = ReflectionHandler.getStringValue((Expr) call
						.arguments().get(0));
				arguments = call.arguments()
						.subList(1, call.arguments().size());
			} else if (call.name().equals("getConstructor")) {
				signature = CallConstraint.CONSTRUCTOR_NAME;
				arguments = call.arguments();
			} else {
				assert false : "unknown method name";
			}

			// 3. get type info of the argument(will be used to match the
			// invocation
			// later);
			signature += "#" + ReflectionHandler.getMethodSignature(arguments);
			// 4. create class ReflectGetMethod by providing above data
			return new ReflectGetMethodOrConstructor(id, target, signature);
		}
		return null;
	}

	/*
	 * Case: class.newInstance() and constructor.newInstance(Object args...)
	 * share the same logic
	 */
	private static ReflectNewInstance captureNewInstance(String id, ID target,
			List<Expr> rawArgs) {
		// get arguments
		List<Expr> flattenedArgs = flattenArgument(rawArgs);
		List<ID> arguments = CSUtil.getArguments(flattenedArgs);

		return new ReflectNewInstance(id, target, arguments);
	}

	/*
	 * Case: method.invoke(obj, Object args...);
	 */
	private static ReflectID captureMethodInvoke(String id, ID target,
			List<Expr> arguments) {
		List<ID> args = new LinkedList<ID>();
		args.add(CSUtil.getID(((Expr) arguments.get(0)).type()));

		List<Expr> flattenedArgs = flattenArgument(arguments.subList(1,
				arguments.size()));
		args.addAll(CSUtil.getArguments(flattenedArgs));

		String callTargetClass = null;
		if (arguments.get(0).type() instanceof ClassType) {
			callTargetClass = ((ClassType) arguments.get(0).type()).fullName();
		} else {
			callTargetClass = UNKNOWN_NAME;
		}
		return new ReflectMethodInvoke(id, target, args, callTargetClass);
	}

	// return string value, otherwise, return null
	private static String getStringValue(Expr expr) {
		if (expr instanceof StringLit) {
			return ((StringLit) expr).value();
		}
		return UNKNOWN_NAME;
	}

	/*
	 * parse the parameters as String
	 */
	private static String getMethodSignature(List<Expr> parameters) {
		StringBuffer formalPara = new StringBuffer("");

		List<Expr> flatPara = flattenArgument(parameters);
		String param = null;
		for (Expr expr : flatPara) {
			if (expr instanceof ClassLit) {
				param = ((ClassLit) expr).typeNode().toString();
			} else {
				param = expr.type().toString();
			}
			formalPara.append(param).append("|");
		}
		return formalPara.toString();
	}

	/*
	 * if the argument is passed in terms of (new Object[]{arg1, arg2,...},
	 * return in terms of (arg1, arg2,...)
	 */
	private static List<Expr> flattenArgument(List<Expr> arguments) {
		if (arguments.size() == 0)
			return arguments;

		List<Expr> expanded = new LinkedList<Expr>();
		for (Expr expr : arguments) {
			if (expr instanceof NewArray) {
				assert arguments.size() == 1;

				NewArray newArray = (NewArray) expr;
				if (newArray.init() != null) {
					for (Object element : newArray.init().elements()) {
						expanded.add((Expr) element);
					}
				}
			} else {
				expanded = arguments;
			}
		}
		return expanded;
	}
}
