package cs.data.id;

import cs.graph.CSGraph;
import cs.graph.GraphElement;
import cs.linearsolver.ValueHolder;
import cs.util.CSUtil;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

public class SpecialID extends ID implements GraphElement {
	public enum TYPE {
		THIS, SUPER;
	}

	private SpecialID.TYPE specialType;
	private AnalysisContext context;

	// this className indicate where this method exists from
	private String currentClass;

	public SpecialID(String uid, SpecialID.TYPE type, String currentClass, String pos) {
		this(uid, type, currentClass, pos, null);
	}
	
	public SpecialID(SpecialID.TYPE type, String currentClass, String pos) {
		this(VariableGenerator.nextAlias(), type, currentClass, pos, null);
	}

	public SpecialID(SpecialID.TYPE type, String currentClass, String position,
			AnalysisContext context) {
		this(VariableGenerator.nextRefresh(), type, currentClass, position,
				context);
	}

	// only used in second pass
	private SpecialID(String uid, SpecialID.TYPE type, String currentClass,
			String position, AnalysisContext context) {
		super(uid, type.toString(), position);
		this.specialType = type;

		assert currentClass != null;
		this.currentClass = currentClass;
		this.context = context;
	}

	public AnalysisContext getContext() {
		return context;
	}

	public boolean isThis() {
		return specialType.equals(TYPE.THIS);
	}

	public boolean isSuper() {
		return specialType.equals(TYPE.SUPER);
	}

	public String currentClass() {
		return currentClass;
	}

	@Override
	public String toString() {
		return specialType + " frm@ " + currentClass + ": "
				+ ((context == null) ? "" : context.getInstID().className());
	}

	// this.field;
	// new A().field;
	// s = this;
	@Override
	public ID refresh(AnalysisContext context) {
		return new SpecialID(context.refreshID(id), specialType, currentClass,
				position, context);
	}

	@Override
	public void buildGraph(CSGraph graph) {
		graph.addEdge(context.getInstID(), this);
		// graph.addEdge(this, context.getInstID());
	}
}