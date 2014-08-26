package cs.linearsolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import polyglot.util.Position;
import cs.data.Properties;
import cs.graph.GraphElement;
import cs.util.CSUtil;
import cs.util.EasyDebugger;

/*
 * this class helps to map from variable to the solved result
 * For instance, given a statement "S s = f;", this class will hold the unrefreshed and refreshed alias name for s and f.
 * Also, it will store the s and f's result solved by liner solver.
 * This class make effort to generalize the mapping strategy from alias name and sovled result
 * 
 * The type "V" is the data type of the project specific value. e.g. Cypress: Integer.  ET: String 
 */
public abstract class ValueHolder<T, V> {
	// private Map<String, Map<T, V>> posToModifiersMap = new HashMap<String,
	// Map<T, V>>();

	protected Map<String, Map<T, V>> classToTypeValueMap = new HashMap<String, Map<T, V>>();

	// private Map<String, String> aliasToPosMap = new HashMap<String,
	// String>();
	/*
	 * the value given from the source code
	 */
	protected Map<String, Map<T, V>> aliasToTypeValueMap = new HashMap<String, Map<T, V>>();

	/*
	 * the reason to separate preSet and post Set value is that preSetValue
	 * always convert the given id to old id value before looking for the
	 * result. However, postSetValue just look for the result by the given
	 * uniqueID
	 */
	protected Map<String, Integer> IDToTypeValueMap = new TreeMap<String, Integer>();

	protected Map<String, Integer> cplexResult = null;

	// map from aliasName to uniquieID
	protected Map<String, Set<String>> aliasToID = new HashMap<String, Set<String>>();

	private Set<String> optimizablePri = new HashSet<String>();

	public void addOptimizablePrimitive(String aliasName) {
		optimizablePri.add(aliasName);
	}

	public boolean ifOptimizablePri(String aliasName) {
		return optimizablePri.contains(aliasName);
	}

	abstract public String printValue(GraphElement elmt);

	/*
	 * print all of the pre-set values in form of "aliasName: values"
	 */
	public void printAliasTypeValues() {
		for (java.util.Map.Entry<String, Map<T, V>> entry : aliasToTypeValueMap
				.entrySet()) {
			EasyDebugger.message(entry.getKey() + ": ");
			EasyDebugger.printMap(entry.getValue());
		}
	}

	public void bindTypeValueToClass(String className, Map<T, V> modifiers) {
		classToTypeValueMap.put(className, modifiers);
	}

	public void bindAliasToID(String alias, String uniqueID) {
		// record id to alias name mapping
		if (!aliasToID.containsKey(alias)) {
			aliasToID.put(alias, new TreeSet<String>());
		}
		aliasToID.get(alias).add(uniqueID);
	}
	
	//input: alias name in first pass
	//output: a set of refreshed variables in second pass
	public Set<String> getMappedID(String alias){
		return aliasToID.get(alias);
	}

	/*
	 * This method binds modifiers from class level to object level
	 */
	public void computeAliasModifiersFromClassDecl(String alias,
			String className) {
		assert className != null;

		Map<T, V> result = null;
		if (aliasToTypeValueMap.containsKey(alias)) {
			result = aliasToTypeValueMap.get(alias);
		}

		if (classToTypeValueMap.containsKey(className)) {
			Map<T, V> classModi = classToTypeValueMap.get(className);
			if (result == null || result.size() == 0)
				result = classModi;
			else {
				for (Map.Entry<T, V> entry : classModi.entrySet()) {
					T key = entry.getKey();

					if (result.containsKey(key)
							&& !compatible(result.get(key), entry.getValue())) {
						EasyDebugger.exit("different phase/mode value declared to both class and object level!");
					} else {
						result.put(key, entry.getValue());
					}
				}
			}
			bindTypeValueToAlias(alias, result);
		} else {
			// it is okay only when the class is library code and the source is
			// not available
			if (Properties.isJavaLib(className) && !Properties.PROCESS_JAVALIB) {
			} else {
				EasyDebugger.printMap(classToTypeValueMap);
				assert false : "Cannot find class: " + className;
			}
		}

	}

	public void bindTypeValueToAlias(String key, Map<T, V> modifiers) {
		assert modifiers != null;
		aliasToTypeValueMap.put(key, modifiers);
	}

	abstract protected boolean compatible(V objModifier, V classModifier);

	/*
	 * set the uniqueID to a specific value. The method is mainly used for
	 * setting "this" and instance's value
	 */
	public void bindTypeValueToID(String uniqueID, int value, T type) {
		if (IDToTypeValueMap.containsKey(uniqueID + type))
			assert IDToTypeValueMap.get(uniqueID + type).equals(value);

		this.IDToTypeValueMap.put(uniqueID + type, value);
	}

	// the input can be refreshed or un refreshed value
	public V getAliasTypeValue(String uniqueID, T type) {
		String aliasName = CSUtil.getAliasName(uniqueID);

		if (!aliasToTypeValueMap.containsKey(aliasName))
			return null;

		// System.out.println("result: " + aliasToTypeValueMap.get(aliasName));
		return aliasToTypeValueMap.get(aliasName).get(type);
	}

	/*
	 * print the result as refreshedValue: result
	 */
	public void printResult() {
		EasyDebugger.message("start printing solved result");
		EasyDebugger.printMap(cplexResult);
	}

	public void setCplexResult(Map<String, Integer> result) {
		this.cplexResult = result;
	}

	public Integer getCplexResult(String var, T type) {
		assert cplexResult != null : "The cplex result is not set";
		String key = var + type;
		if (cplexResult.containsKey(key)) {
			return cplexResult.get(key);
		}

		return null;
	}

	// public void bindPosToModifiers(Position pos, Map<T, V> modifiers) {
	// assert modifiers != null;
	//
	// posToModifiersMap.put(pos.startOf().toString(), modifiers);
	// posToModifiersMap.put(pos.endOf().toString(), modifiers);
	// }

}
