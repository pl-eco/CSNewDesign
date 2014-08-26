package cs.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import polyglot.frontend.FileSource;

import cs.util.EasyDebugger;

/*
 * This class maintain all of the compiler runtime settings, statistical data.
 */
public class Properties {
	public static final boolean PROCESS_JAVALIB = false;

	public static int K_CFA;

	public static boolean debug = true;

	public static String entryMethod;
	public static String entryClass;

	private static Set<String> ignoreClass = new HashSet<String>();

	private static Set<String> libPrefix = new HashSet<String>();

	private static Set<String> mustParse = new HashSet<String>();

	static {
		ignoreClass.add("java.lang.Class");
		ignoreClass.add("java.lang.Enum");
		ignoreClass.add("java.lang.annotation.Annotation");
		ignoreClass.add("java.lang.Override");
		ignoreClass.add("java.lang.annotation.Target");
		ignoreClass.add("java.lang.annotation.Retention");
		ignoreClass.add("java.lang.annotation.ElementType");
		ignoreClass.add("java.lang.Iterable");
		ignoreClass.add("java.lang.Object");
		ignoreClass.add("java.lang.String");
		ignoreClass.add("java.lang.Throwable");
		ignoreClass.add("java.lang.Error");
		ignoreClass.add("java.lang.Exception");
		ignoreClass.add("java.lang.RuntimeException");
		ignoreClass.add("java.lang.Cloneable");
		ignoreClass.add("java.lang.NullPointerException");
		ignoreClass.add("java.lang.ClassCastException");
		ignoreClass.add("java.lang.ArrayIndexOutOfBoundsException");
		ignoreClass.add("java.lang.ArrayStoreException");
		ignoreClass.add("java.lang.ArithmeticException");
		ignoreClass.add("java.lang.System");
		ignoreClass.add("java.io.Serializable");
		ignoreClass.add("java.util.Iterator");
		ignoreClass.add("java.util.TimeZone");

		libPrefix.add("java.");
		libPrefix.add("javax.");
		libPrefix.add("sun.");
		libPrefix.add("org.w3c");
		libPrefix.add("org.xml");
		libPrefix.add("android.");

		mustParse.add("android.view.LayoutInflater");
	}

	/*
	 * load property file
	 */
	static {

		java.util.Properties prop = new java.util.Properties();
		FileInputStream is = null;
		try {
			is = new FileInputStream("contextSen.prop");
			prop.load(is);

			entryMethod = prop.getProperty("entryName").trim();
			K_CFA = Integer.parseInt(prop.getProperty("context_sensitivity")
					.trim());
			debug = new Boolean(prop.getProperty("debug").trim());

		} catch (IOException e) {
			e.printStackTrace();
			EasyDebugger.exit("Read property file error!");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Property File Loaded");
	}

	public static void setEntryClass(FileSource source) {
		if (entryClass == null) {
			String sourcePath = source.getName();
			String curPath = System.getProperty("user.dir");
			sourcePath = sourcePath.replace(curPath, "");
			sourcePath = sourcePath.replace('\\', '.');
			sourcePath = sourcePath.substring(0, sourcePath.lastIndexOf("."));
			entryClass = sourcePath.replaceFirst("\\.*", "");
		}
	}

	/*
	 * the centralized method to decide if all of the methods of "className" can
	 * be ignored of parsing
	 */
	public static boolean ignore(String className) {
		if (ignoreClass.contains(className))
			return true;
		else if (mustParse.contains(className))
			return false;
		else if (isJavaLib(className)) {
			// must parse, since containers classes are here
			if (className.startsWith("java.util"))
				return false;
			// parse only when the library source is in
			else if (PROCESS_JAVALIB && className.startsWith("java.lang"))
				return false;
			// very special class, never pass
			else
				return true;
		} else {
			return false;
		}
	}

	public static boolean isJavaLib(String className) {
		for (String prefix : libPrefix) {
			if (className.startsWith(prefix))
				return true;
		}
		return false;
	}

	private static Map<Object, Long> timeRecord = new HashMap<Object, Long>();

	public static void stopWatchStart(Object label) {
		timeRecord.put(label, System.currentTimeMillis());
	}

	public static long stopWatchEnd(Object label) {
		long totalTime = -1;
		if (timeRecord.containsKey(label)) {
			totalTime = System.currentTimeMillis() - timeRecord.get(label);
		} else {
			EasyDebugger
					.message("get not get the total time. The start time was not set");
		}
		return totalTime;
	}

	// -----lines of code statistic
	public static int constraints = 0;
}
