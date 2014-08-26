package cs.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.StringLit;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance_c;
import polyglot.types.NullType;
import polyglot.types.ParsedClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ProcedureInstance;
import polyglot.types.ProcedureInstance_c;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import cs.data.id.ID;
import cs.data.id.LocalID;
import cs.data.id.SpecialID;
import cs.types.CSBaseType;
import cs.types.CSNonGenericType;
import cs.types.CSObjectType;
import cs.types.CSParsedClassType_c;
import cs.types.csTypeSystem_c;

public class CSUtil {
	public static final List EMPTY_LIST = new LinkedList();

	// get argument information
	public static List<ID> getArguments(List<Expr> arguments) {
		List<ID> args = new LinkedList<ID>();
		for (Expr arg : arguments) {
			args.add(CSUtil.getID(arg.type()));
		}
		return args;
	}

	public static Type getArrayElementType(ArrayType arrayType) {
		if (arrayType.base() instanceof ArrayType)
			return getArrayElementType((ArrayType) arrayType.base());
		else
			return arrayType.base();
	}

	// wrap the return type, argument as CSNonGenericType
	public static ProcedureInstance wrapProcedureInstance(ProcedureInstance pi,
			csTypeSystem_c ts) {
		List<Type> formalList = new ArrayList();
		for (Type element : pi.formalTypes()) {
			Type arg = null;
			if (element instanceof CSParsedClassType_c) {
				arg = ts.createDefaultObjectType((ClassType) element);
			} else {
				arg = element;
			}
			formalList.add(arg);
		}

		if (!CollectionUtil.equals(pi.formalTypes(), formalList)) {
			ProcedureInstance n = (ProcedureInstance) pi.copy();
			n.setFormalTypes(formalList);
			return n;
		}
		return pi;
	}

	public static List<? extends Type> wrapArguments(List<? extends Type> args,
			csTypeSystem_c ts) {
		List<Type> formalList = new LinkedList();

		for (Type element : args) {
			Type arg = null;
			if (element instanceof CSParsedClassType_c) {
				arg = ts.createDefaultObjectType((ClassType) element);
			} else {
				arg = element;
			}
			formalList.add(arg);
		}
		return formalList;
	}

	public static void setID(Expr node, ID id) {
		if (node.type() instanceof CSBaseType) {
			((CSBaseType) node.type()).setID(id);
		} else if (node.type() instanceof CSParsedClassType_c) {
			((CSParsedClassType_c) node.type()).setID(id);
		} else {
			assert false : "node.type() is " + node.type().getClass()
					+ " instead of CSBaseType. Node: " + node.toString() + " "
					+ node.position();
		}
	}

	// get the ID from the node. if the type is parsedclass type, will create a
	// fake ID and return
	public static ID getID(Type type) {
		if (type instanceof CSBaseType) {
			/*
			 * I don't remember why I have the above code, so commented out
			 */
			// if (type instanceof CSObjectType) {
			// String name = ((CSObjectType) type).fullClassName();
			// // return a fake ID for ClassLit
			// if (name.equals("java.lang.Class"))
			// return new LocalID("ParsedType(reflection)", "ParsedType");
			// }
			return ((CSBaseType) type).getID();
		} else if (type instanceof CSParsedClassType_c) {
			ID ret = ((CSParsedClassType_c) type).getID();
			// assert ret instanceof SpecialID : "wrong ID type(" +
			// ret.getClass()
			// + ") SpecialID expected";
			return ret;

			// TODO shoud fix this
			// // create a fake ID
			// return ret != null ? ret : new LocalID("ParsedType",
			// "ParsedType");
		} else {
			assert false : type.getClass();
			return null;
		}
	}

	public static boolean ignorable(String className, String method) {
		if (className.startsWith("sun."))
			return true;

		return false;
	}

	// input the uniqueID, get its aliasName
	public static String getAliasName(String uniqueID) {
		int i = uniqueID.lastIndexOf("|");
		if (i == -1)
			return uniqueID;

		return uniqueID.substring(i + 1, uniqueID.length());
	}

	public static String getSimpleVar(String uniqueID) {
		int start = uniqueID.lastIndexOf("|") + 1;
		if (start == 0)
			start = uniqueID.lastIndexOf(".") + 1;

		return uniqueID.substring(start, uniqueID.length());
	}

	public static void main(String[] args) {
		System.out.println(CSUtil.getAliasName("f"));
	}
}
