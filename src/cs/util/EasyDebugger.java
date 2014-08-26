package cs.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Node;

import cs.data.Properties;
import cs.types.CSBaseType;

public class EasyDebugger {

	public static void printError(Object error) {
		System.err.println(error);
	}

	public static void exit(Object error) {
		try {
			EasyDebugger.printError(error);
			EasyDebugger.printError("serious error detected!! will exit!!!");
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void message(Object message) {
		System.out.println(message);
	}

	public static void debug(Object info) {
		if (Properties.debug)
			System.out.println(info);
	}

	public static void printCollection(Collection coll) {
		int counter = 0;
		System.out.println("//Print collection: ");
		for (Object value : coll) {
			EasyDebugger.message(value.toString());
		}
	}

	public static void printMap(Map map) {
		Set keys = map.keySet();
		// SortedSet
		System.out.println("\nPrint the map: size: " + map.size());
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			Object key = iter.next();

			System.out.println(key + ": " + map.get(key));
		}
		System.out.println();
	}

	// "org.apache.xml.Factory" return "Factory"
	public static String simpleName(String className) {
		if (className == null)
			return null;
		return (String) className.subSequence(className.lastIndexOf(".") + 1,
				className.length());
	}

	public static void main(String[] args) {
		EasyDebugger.message(EasyDebugger.simpleName("org.xm.lapache.fae.ccc"));
	}
}
