package cs.types;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import cs.util.EasyDebugger;
import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.types.ClassType_c;
import polyglot.types.ParsedClassType;
import polyglot.types.Type;

public class CSNonGenericWrapper implements InvocationHandler, Serializable {

	private transient final CSNonGenericType_c proxied;

	private static final String INNER_TYPE = "inner_type";

	/*
	 * this method wrap up the CSNonGenericType and return a proxy in form of a
	 * CSNonGenericType obj
	 */
	public static CSNonGenericType createNonGenericType(Type type) {
		// create the CSNonGenericType object
		CSNonGenericType_c nonGeneric = new CSNonGenericType_c(type);
		return getWrapper(nonGeneric);
	}

	public static CSNonGenericType createNonGenericType(String aliasName,
			ClassType_c type) {
		// create the CSNonGenericType object
		CSNonGenericType_c nonGeneric = new CSNonGenericType_c(aliasName, type);
		return getWrapper(nonGeneric);
	}

	public static CSNonGenericType createNonGenericType(Type type,
			String typeLabel) {
		// create the CSNonGenericType object
		CSNonGenericType_c nonGeneric = new CSNonGenericType_c(
				VariableGenerator.nextAlias(), type, typeLabel);
		return getWrapper(nonGeneric);
	}

	private static CSNonGenericType getWrapper(CSNonGenericType_c nonGeneric) {
		// wrap it up
		CSNonGenericType proxy = (CSNonGenericType) Proxy.newProxyInstance(
				CSNonGenericWrapper.class.getClassLoader(),
				new Class[] { CSNonGenericType.class },
				new CSNonGenericWrapper(nonGeneric));

		// return the proxy
		return (CSNonGenericType) proxy;

	}

	private CSNonGenericWrapper(CSNonGenericType_c proxied) {
		this.proxied = proxied;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Exception {
		Object ret = null;
		try {
			Field innerType = CSNonGenericType_c.class
					.getDeclaredField(INNER_TYPE);

			innerType.setAccessible(true);

			// filter out the method from CSParsedClassType
			Type pct = (Type) innerType.get(proxied);

			// JL5ParsedClassType_c pct = (JL5ParsedClassType_c) innerType
			// .get(proxied);

			if (method.getName().equals("getType")
					|| method.getName().equals("setType")
					|| method.getName().equals("equals")
					|| method.getName().equals("getAliasName")
					|| method.getName().equals("setID")
					|| method.getName().equals("getID")
					|| method.getName().equals("fullClassName")
					|| method.getName().equals("toClass")
					|| method.getName().equals("copy")
					|| method.getName().equals("getTypeLabel")
					|| method.getName().equals("setTypeLabel")) {
				ret = method.invoke(proxied, args);
			} else {
				ret = method.invoke(pct, args);
			}

			// if (ret == pct) {
			// assert false : "cannot return parsedClassType: "
			// + method.getName();
			// }

			if (ret == proxied)
				ret = proxy;

			return ret;
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			}
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return ret;
	}
}