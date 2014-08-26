package cs.visit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cs.data.Properties;
import cs.data.id.InstID;
import cs.linearsolver.ValueHolder;

public class AnalysisContext implements Serializable {
	public static final String CONTEXT_SEPARATOR = "|";
	private static final String root = "$Entry";
	
	private static final InstID entryID = new InstID(root, "EntryClass",
			"Entry");

	private static Map<String, AnalysisContext> contextCache = new HashMap<String, AnalysisContext>();
	// the enclosing object of the current context
	private InstID instID;

	private String invocationPoint;

	private static ValueHolder valueHolder;

	public static boolean isRootContext(String uniqueID){
		return root.equals(uniqueID);
	}
	
	// the instID of entry method is NULL
	private AnalysisContext(InstID instID, String invocationPoint) {
		this.instID = instID;
		this.invocationPoint = invocationPoint;
	}

	public static void setValueHolder(ValueHolder vh) {
		// ensure this static field is only set once
		assert valueHolder == null : "ValueHolder cannot be set twice";

		valueHolder = vh;
	}

	// create a new context by the giving instID and invocationPoint
	public static AnalysisContext createNewContext(InstID instID,
			String invocationPoint) {
		String key = instID.uniqueID() + "##" + invocationPoint;
		if (!contextCache.containsKey(key)) {
			contextCache.put(key, new AnalysisContext(instID, invocationPoint));
		}
		return contextCache.get(key);
	}

	// create a context for entry method, normally the "Main" method
	public static AnalysisContext createEntryContext() {
		return new AnalysisContext(entryID, "Entry");
	}

	public static boolean isEntryID(InstID instID) {
		return instID.equals(entryID);
	}

	// refresh a local ID(many special cases of different IDs are considered as
	// a local variable)
	public String refreshID(String id) {
		String key = this.getFullContext(Properties.K_CFA - 1) + id;

		// safeguard here
		assert !key.contains("||") : key + "  " + id;

		valueHolder.bindAliasToID(id, key);
		return key;
	}
	
	// Format: inst1#pos1|inst2#pos2|aliasName
	private String getFullContext(int n_sensitive) {
		assert n_sensitive >= 0;

		// reaches the sensitivity or the entry method
		if (n_sensitive == 0 || instID.getContext() == null)
			return "";

		// current context == instID.aliasName + invocationPoint
		AnalysisContext history = instID.getContext();
		String ret = history.getFullContext(--n_sensitive) + instID.pgmLabel()
				+ CONTEXT_SEPARATOR;
		return ret;
	}

	public InstID getInstID() {
		return instID;
	}
	
	public ValueHolder getValueHolder() {
		return valueHolder;
	}
}