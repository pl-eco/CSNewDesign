//package cs.util;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import cs.data.constraint.CallConstraint;
//import cs.types.CSArrayType;
//import cs.types.CSBaseType;
//import cs.types.CSObjectType;
//
//import polyglot.ast.Assign;
//import polyglot.ast.Binary;
//import polyglot.ast.Call;
//import polyglot.ast.Cast;
//import polyglot.ast.Expr;
//import polyglot.ast.New;
//import polyglot.ast.StringLit;
//import polyglot.types.ArrayType;
//import polyglot.types.NullType;
//import polyglot.types.ParsedClassType;
//import polyglot.types.ParsedClassType_c;
//import polyglot.types.PrimitiveType;
//import polyglot.types.Type;
//
//public class XXXUtils {
//	public static boolean compJavaLib = true;
//
//	public static boolean thinkMore = true;
//
//	// types that doesn't ignore
//	private static Map useTypes = new HashMap();
//
//	public static Map<String, Object> syncNodes = new HashMap();
//
//	static {
//		useTypes.put("java.util.concurrent.ConcurrentLinkedQueue", new Object());
//		useTypes.put("java.util.HashMap", new Object());
//		useTypes.put("java.util.Hashtable", new Object());
//		useTypes.put("java.util.Vector", new Object());
//		useTypes.put("java.util.Stack", new Object());
//		useTypes.put("java.util.HashSet", new Object());
//		useTypes.put("java.util.LinkedList", new Object());
//		useTypes.put("java.util.Stack", new Object());
//		useTypes.put("java.util.TreeSet", new Object());
//		useTypes.put("java.util.ArrayList", new Object());
//	}
//
//	public static boolean isIgnorable(Expr expr) {
//		if (expr instanceof Cast) {
//			expr = ((Cast) expr).expr();
//		}
//
//		if (expr instanceof Call) {
//			if (XXXUtils.ignoreCall((Call) expr))
//				return true;
//		}
//
//		
//		return XXXUtils.isIgnorable(expr.type());
//	}
//
//	public static boolean isIgnorable(Type type) {
//		boolean isIgnorable = false;
//
////		if (type.toString().contains("GetReflectionFactoryAction")) {
////			EasyDebugger.exit(type.toString() + "  " + type.getClass());
////		}
//		if (type == null)
//			isIgnorable = true;
//		else if (type instanceof PrimitiveType)
//			isIgnorable = true;
//		else if (type instanceof NullType)
//			isIgnorable = true;
//		else if (type instanceof Binary)
//			isIgnorable = true;
//		else if (type instanceof StringLit)
//			isIgnorable = true;
//		else if (type instanceof ArrayType) {
//			ArrayType ar = (ArrayType) type;
//			return XXXUtils.isIgnorable(ar.base());
//		} else if (type instanceof CSObjectType) {
//			CSObjectType temp = (CSObjectType) type;
//			if (null == temp.name()) {
//				isIgnorable = false;
//				return false;
//			} else if (temp.name().equals("String"))
//				isIgnorable = true;
//			if (!compJavaLib) {
//				if (type.toString().startsWith("java.")) {
//					if (type.toString().startsWith("java.util.")) {
//						if (type.toString().contains("Date"))
//							return true;
//						else if (type.toString().contains("Calendar"))
//							return true;
//						else if (type.toString().contains("OutputStream"))
//							return true;
//						else if (type.toString().contains("InputStream"))
//							return true;
//						else
//							return false;
//					} else if (type.toString().startsWith("java.io.")) {
//						if (type.toString().contains("OutputStream"))
//							return true;
//						else if (type.toString().contains("InputStream"))
//							return true;
//					} else if (type.toString().startsWith("java.lang")) {
//						if (type.toString().contains("Integer"))
//							return false;
//					} else if (type.toString().contains("Runnable")
//							|| type.toString().contains("Thread"))
//						return false;
//					else if (type.toString().contains("Object"))
//						return false;
//					else {
//						return true;
//					}
//				}else if(type.toString().startsWith("sun.reflect")){
//					return true;
//				}
//			}
//		}
//		if (type instanceof ParsedClassType) {
//			if (type.toString().startsWith("java.")) {
//				if (type.toString().startsWith("java.util."))
//					return false;
//				else if (type.toString().contains("Runnable")
//						|| type.toString().contains("Thread"))
//					return false;
//				else if (type.toString().contains("Object"))
//					return false;
//				else if (type.toString().contains("OutputStream"))
//					return true;
//				else if (type.toString().startsWith("java.lang.reflect"))
//					return true;
//				else if (type.toString().equals("java.lang.Class"))
//					return true;
//				else
//					return true;
//			}else return true;
//		}
//		return isIgnorable;
//	}
//
//	public static boolean ignoreCall(Call call) {
//		// any call other than reflection will be ignored
//		String targetType = call.target().type().toString();
//		if (targetType.equals("java.lang.Class")
//				|| targetType.equals("java.lang.reflect.Method")
//				|| targetType.equals("java.lang.reflect.Constructor")) {
//			return true;
//		}
//
//		// java5
//		if (call.target() instanceof StringLit
//				|| call.target() instanceof Binary)
//			return true;
//
//		// java5 END
//		return XXXUtils.ignoreCall(call.target().type().toString(), call.name());
//	}
//
//	public static boolean ignoreCall(String classType, String methodName) {
//		boolean flag = false;
//
//		EasyDebugger.debug("classType: " + classType + " name " + methodName);
//
//		// ignoreable methods
//		String[] ignoreNames = new String[] { "java.io.PrintStream.println",
//				"java.io.PrintStream.print",
//				"java.lang.System.currentTimeMillis", "java.lang.System.gc",
//				"java.lang.Thread.yield", "java.lang.Thread.join",
//				"java.lang.String.equals", "java.lang.System.exit",
//				"java.lang.String.valueOf", "java.io.PrintStream.println",
//				"java.io.PrintStream.close", "java.lang.StringBuffer.append",
//				"java.io.ObjectOutputStream.writeObject",
//				"java.io.ObjectInputStream.readObject",
//				"java.util.Calendar.setTime" };
//
//		for (int i = 0; i < ignoreNames.length; i++) {
//			if ((classType + "." + methodName).equals(ignoreNames[i])) {
//				flag = true;
//				break;
//			}
//		}
//		if (flag == true)
//			return flag;
//		if (methodName.equals("notify") || methodName.equals("notifyAll")
//				|| methodName.equals("wait"))
//			return true;
//
//		if (methodName.equals("getClass"))
//			return true;
//
//		if (classType.startsWith("java.")) {
//			if (!XXXUtils.compJavaLib) {
//				if (useTypes.containsKey(classType))
//					return false;
//				else if (methodName.startsWith(CallConstraint.CONSTRUCTOR_NAME))
//					return true;
//				else if (classType.startsWith("java.util")) {
//					if (classType.startsWith("java.util.concurrent"))
//						return false;
//					if (classType.equals("java.util.StringTokenizer"))
//						return true;
//					else if (classType
//							.equals("java.util.PropertyResourceBundle"))
//						return true;
//					else if (classType.equals("java.util.Date"))
//						return true;
//					else if (classType.equals("java.util.Properties"))
//						return true;
//					else if (classType.equals("java.util.Locale"))
//						return true;
//					else if (classType.equals("java.util.BitSet"))
//						return true;
//					else if (classType.equals("java.util.ResourceBundle"))
//						return true;
//					else
//						return false;
//				} else if (classType.startsWith("java.lang.")) {
//					if (classType.contains("Runnable")
//							|| classType.contains("Thread")) {
//						if (methodName.contains("run")
//								|| methodName.contains("start")) {
//							return false;
//						} else
//							return true;
//					} else if ((classType.equals("java.lang.Class") || classType
//							.equals("java.lang.reflect.Constructor"))
//							|| classType.equals("java.lang.reflect.Method")
//					/* && methodName.equals("newInstance") */) {
//						return false;
//					} else
//						return true;
//				} else if (classType.contains("Object"))
//					return false;
//				else
//					return true;
//			}
//		}
//		if (classType.startsWith("sun.misc"))
//			return true;
//		if (true /* BenchMarkLabel.benchHSQL */) {
//			String[] ignores = { "org.hsqldb.Trace.trace" };
//			for (int i = 0; i < ignores.length; i++) {
//				if ((classType + "." + methodName).equals(ignores[i])) {
//					flag = true;
//					break;
//				}
//			}
//			if (flag == true)
//				return flag;
//
//			String[] ignoreTypes = { "org.hsqldb.Trace" };
//			for (int i = 0; i < ignoreTypes.length; i++) {
//				if ((classType).equals(ignoreTypes[i])) {
//					flag = true;
//					break;
//				}
//			}
//			if (flag == true)
//				return flag;
//		}
//		if (true /* BenchMarkLabel.benchXalan */) {
//			if (classType.startsWith("javax.xml"))
//				return true;
//			else if (classType.startsWith("org.xml"))
//				return true;
//		}
//
//		return flag;
//	}
//
//	// return the leftmost expression for assignment as "a = b = c = d..."
//	public static Expr getLHV(Expr expr) {
//		if (expr instanceof Assign) {
//			return ((Assign) expr).left();
//		} else {
//			return expr;
//		}
//	}
//
//	public static boolean javaLibName(String className) {
//		if (className == null) {
//			EasyDebugger.message("&&empty className");
//			return false;
//		}
//		if (className.startsWith("java.lang")
//				|| className.startsWith("java.util"))
//			return true;
//		return false;
//	}
//
//	// input: alias22|alis23|alias4|...|alias3
//	// return: alias3
//	public static String getSimpleVar(String uniqueID) {
//		int start = uniqueID.lastIndexOf("|") + 1;
//		if (start == 0)
//			start = uniqueID.lastIndexOf(".") + 1;
//		
//		return uniqueID.substring(start, uniqueID.length());
//	}
//
//	public static String getFullClassName(Type type) {
//		EasyDebugger.message("get full name");
//		while (true) {
//			if (type instanceof CSObjectType) {
//				return ((CSObjectType) type).fullName();
//			} else if (type instanceof CSArrayType) {
//				type = ((CSArrayType) type).base();
//			} else if (type instanceof ParsedClassType_c) {
//				return ((ParsedClassType_c) type).fullName();
//			} else {
//				EasyDebugger.exit("unknown type: " + type.toString() + " "
//						+ type.getClass());
//			}
//
//			if (type == null)
//				EasyDebugger.exit("the checked type is NULL");
//		}
//	}
//
// }
