package cs.data.id;

import java.io.Serializable;

import cs.graph.GraphBuilder;
import cs.graph.GraphElement;
import cs.visit.AnalysisContext;

public abstract class ID implements GraphBuilder, Serializable {
	protected static final String ARRAY_ELEMENT = "[x]";

	protected String id;
	protected String varName;

	// used only for debugging
	protected String position;

	public ID(String id, String varName, String position) {
		assert id != null;
		this.id = id;
		this.varName = varName;

		assert position != null;
		this.position = position;
	}

	// pgmLabel is the unrefreshed value
	public String pgmLabel() {
		assert id != null;

		// if the id is already refreshed, only retrieve the very last part
		return id.substring(id.lastIndexOf('|') + 1);
	}

	public String getPosition() {
		assert position != null;
		return position;
	}

	public final String getVarName() {
		assert varName != null;
		return varName;
	}

	public String toString() {
		return varName + desc();
	}

	public String desc() {
		return "(" + id + ")";
	}

	public String uniqueID() {
		return id;
	}
	
	public void replaceUniqueID(String id) {
		this.id = id;
	}

	abstract public ID refresh(AnalysisContext context);

	@Override
	/*
	 * this will be used in graph for node existence check and, for FieldID,
	 * check if already exist in set D
	 */
	public final boolean equals(Object id) {
		return this.uniqueID().equals(((GraphElement) id).uniqueID());
	}

	@Override
	public final int hashCode() {
		return uniqueID().hashCode();
	}
}
