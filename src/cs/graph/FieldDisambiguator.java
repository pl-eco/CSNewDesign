package cs.graph;

import java.util.LinkedList;
import java.util.List;

import cs.data.id.FieldID;
import cs.data.id.InstID;
import cs.data.id.SpecialID;
import cs.util.EasyDebugger;

/*
 * this class tries to make a connection between the un-binded field with binded field
 */
public class FieldDisambiguator implements GraphBuilder {
	private InstID instID;
	private FieldID fieldID;

	public FieldDisambiguator(InstID instID, FieldID fieldID) {
		this.instID = instID;

		// cannot be this. The case for super should be fixed in "buildGraph"
		assert !(fieldID.target() instanceof SpecialID);

		this.fieldID = fieldID;
	}

	@Override
	public String toString() {
		return "FieldDis: " + fieldID.fieldUniqueID() + " dised to "
				+ instID.fieldNamed(fieldID.fieldName());
	}

	@Override
	public void buildGraph(CSGraph graph) {
		GraphElement realField = new GraphID(instID.fieldNamed(fieldID
				.fieldID()));
		GraphElement nominalField = fieldID;

		GraphElement from, to;
		// flow between real field and nominal field
		if (fieldID.getOps().equals(FieldID.Ops.READ)) {
			from = realField;
			to = nominalField;
		} else {
			from = nominalField;
			to = realField;
		}

		graph.addFlow(from, to);
	}
}
