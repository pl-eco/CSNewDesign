package cs.data.id;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import cs.data.ConstraintContainer;
import cs.graph.CSGraph;
import cs.graph.FieldDisambiguator;
import cs.graph.GraphBuilder;
import cs.graph.GraphElement;
import cs.graph.GraphID;
import cs.graph.TargetableGraphElement;
import cs.graph.TaskElement;
import cs.util.CSUtil;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

/*
 * This class represents a field access. e.g. a.f or even s (with the implicit target "this")
 */
public class FieldID extends ID implements GraphElement, TaskElement,
		Serializable {
	public enum Ops {
		READ, WRITTEN
	}

	private String fieldAlias;

	// polyglot will serialize this class. Label "target" as transient to avoid
	// serialization
	private transient ID target; // a
	private String fieldName; // f
	private Ops ops;

	/*
	 * s = b.f; b = a; a = new A();
	 */
	public FieldID(ID target, String fieldId, String fieldName, Ops ops,
			String position) {
		this(VariableGenerator.nextAlias(), target, fieldId, fieldName, ops,
				position);
	}

	public FieldID(String id, ID target, String fieldId, String fieldName,
			Ops ops, String position) {
		super(id, target.getVarName() + "." + fieldName, position);
		assert fieldId != null;

		this.fieldAlias = fieldId;
		this.target = target;
		this.fieldName = fieldName;
		this.ops = ops;
	}

	public static FieldID createArrayElement(String wholeName, ID target,
			Ops ops, String position) {
		return new FieldID(wholeName, target, ID.ARRAY_ELEMENT,
				ID.ARRAY_ELEMENT, ops, position);
	}

	// pgmLabel is the unrefreshed value
	public final String pgmLabel() {
		assert fieldAlias != null;

		// if the id is already refreshed, only retrieve the very last part
		return fieldAlias.substring(fieldAlias.lastIndexOf('|') + 1);
	}

	public ID target() {
		return target;
	}

	public String fieldID() {
		return this.fieldAlias;
	}

	public String toString() {
		return target.getVarName()
				+ "."
				+ fieldName
				+ "("
				+ this.fieldUniqueID()
				+ "^"
				+ ops
				+ ")"
				+ ((target() instanceof SpecialID) ? "" : "|(" + uniqueID()
						+ ")");
	}

	public Ops getOps() {
		return ops;
	}

	public String fieldName() {
		return fieldName;
	}

	@Override
	public ID refresh(AnalysisContext context) {
		// 1. refresh aliasName
		String nID = context.refreshID(id);
		// 2. refresh the target
		ID nTarget = target.refresh(context);

		// 3. refresh fieldAlias
		// fieldAlias doesn't need to be refreshed. It must be the original
		// alias

		return new FieldID(nID, nTarget, fieldAlias, fieldName, ops, position);
	}

	// @Override
	/*
	 * if the target is this/super, the outter and inner uid is the same
	 */
	public String uniqueID() {
		if (target instanceof SpecialID)
			return fieldUniqueID();

		return super.uniqueID();
	}

	public String fieldUniqueID() {
		if (target instanceof SpecialID) {
			return ((SpecialID) target).getContext().getInstID().uniqueID()
					+ "|" + fieldAlias;
		}
		return ((GraphElement) target).uniqueID() + "|" + fieldAlias;
	}

	@Override
	public void buildGraph(CSGraph graph) {
		// case for this.f
		// if (this.uniqueID().equals(this.fieldUniqueID()))
		// return;

		List<GraphBuilder> list = new LinkedList<GraphBuilder>();

		// build graph for target
		target.buildGraph(graph);

		// put node to D set
		graph.addField(target.uniqueID(), this);

		/*
		 * a flow between inner field to the outer representative
		 * 
		 * e.g. a.f <--> f
		 */
		GraphElement inner = new TargetableGraphElement(new GraphID(
				this.fieldUniqueID()), (GraphElement) target);
		GraphElement outter = this;

		GraphElement from, to;
		if (ops.equals(Ops.READ)) {
			from = inner;
			to = outter;
		} else {
			from = outter;
			to = inner;
		}

		graph.addFlow(from, to);
	}

	@Override
	public String lookUpName(InstID instID) {
		return instID.uniqueID() + ConstraintContainer.CLASS_SEPARATOR
				+ uniqueID();
	}

	@Override
	public GraphBuilder getGraphBuilder(InstID instID) {
		return new FieldDisambiguator(instID, this);
	}
}
