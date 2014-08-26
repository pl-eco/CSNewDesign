package cs.data.id;

import java.util.LinkedList;
import java.util.List;

import cs.graph.CSGraph;
import cs.graph.GraphElement;
import cs.visit.AnalysisContext;

public class ArrayInstID extends ID implements GraphElement {
	public static final String ARRAY_NAME = "$ARRAY";

	private List<ID> initIDs;

	// used in second pass
	private AnalysisContext context;


	public ArrayInstID(String id, String className,
			List<ID> initIDs,
			String position) {
		this(id, className, initIDs, position, null);
	}

	public ArrayInstID(String id, String className,
			List<ID> initIDs,
			String position, AnalysisContext context) {
		// assign the className to varName
		super(id, className, position);
		this.initIDs = initIDs;
		this.context = context;
	}

	@Override
	public ID refresh(AnalysisContext context) {

		String uid = context.refreshID(id);
		// refresh initIDs
		List<ID> init = new LinkedList<ID>();

		for (ID id : initIDs) {
			init.add(id.refresh(context));
		}
		return new ArrayInstID(uid, varName, init, position, context);
	}

	@Override
	public void buildGraph(CSGraph graph) {
		// represent the instance of the array
		InstID inst = new InstID(this.uniqueID(), ARRAY_NAME,
				this.getPosition(),
				false, context);

		// the created InstID is the target of array access
		graph.addInstance(inst);

		/*
		 * new Array[]{a, b} ==> (within array) this.f = a; this.f = b;
		 */
		// LocalID target = new LocalID(inst.uniqueID(), "ARRAY_TARGET",
		// this.getPosition() + "*target");

		/*
		 * since this is only a fake field access, so the wholeName for the
		 * field is not crucial for value binding
		 */
		FieldID field = FieldID.createArrayElement(
				VariableGenerator.nextRefresh(), inst, FieldID.Ops.WRITTEN,
				this.getPosition() + "*ELEMENT");
		graph.addField(field.target().uniqueID(), field);

		// buildGraph and flow each of the initIDs to the element
		for (ID init : initIDs) {
			init.buildGraph(graph);

			graph.addFlow((GraphElement) init, field);
		}
	}
}
