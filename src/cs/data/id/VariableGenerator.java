package cs.data.id;

import java.util.HashMap;
import java.util.Map;

public class VariableGenerator {
	private static int count = 0;
	private static int refresh = 0;
	private static int alias = 0;
	private static int virtual = 0;
	private static int sync = 0;
	private static int reflect = 0;

	private static Map array_map = new HashMap();

	public static String nextVar() {
		String s = "var" + count;
		count++;
		return s;
	}

	public static String nextRefresh() {
		String s = "refresh" + refresh;
		refresh++;
		return s;
	}

	public static String nextAlias() {
		String s = "alias" + alias;
		
		if(alias == 10402){
			int i = 2;
		}
		
		alias++;

		return s;
	}

	public static String nextVirtualStatic() {
		String s = "virtual" + virtual;
		virtual++;
		return s;
	}

	public static String nextVirtuaSync() {
		String s = "sync" + sync;
		sync++;
		return s;
	}

	public static String getArrayElementVar(String arrayName) {
		String var = (String) array_map.get(arrayName);
		if (var == null) {
			var = VariableGenerator.nextVar();
			array_map.put(arrayName, var);
		}
		return var;
	}
}
