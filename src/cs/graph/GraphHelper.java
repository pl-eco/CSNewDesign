package cs.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cs.util.EasyDebugger;

//this class is used by post and pred
public class GraphHelper<K, V> {
	private Map<K, Set<V>> map = new HashMap<K, Set<V>>(5000);

	// return the set, if empty, create one
	public Set<V> getSet(K key) {
		Set<V> retList = null;
		if (!map.containsKey(key)) {
			map.put(key, new HashSet());
		}
		retList = map.get(key);
		return retList;
	}

	public boolean contains(K key) {
		return map.containsKey(key);
	}

	// add the V into Set if the set represent by the K is empty
	public boolean addIfEmpty(K key, V value) {
		Set<V> tmpList = getSet(key);
		if (!tmpList.contains(value)) {
			tmpList.add(value);
			return true;
		}
		return false;
	}

	/*
	 * for debug, print out element in the map
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		for (K key : map.keySet()) {
			Set set = map.get(key);
			sb.append("key: ").append(key);
			sb.append(set.toString());
		}
		return sb.toString();
	}

}
