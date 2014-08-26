package cs.data.constraint;

import java.util.LinkedList;
import java.util.List;

import cs.data.Properties;
import cs.data.id.ID;
import cs.data.id.SpecialID;
import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.graph.GraphElement;
import cs.linearsolver.ValueHolder;
import cs.visit.AnalysisContext;

public class FlowConstraint implements Constraint, GraphBuilder {
	private ID from;
	private ID to;
	private FLOWTYPE flowType;

	public enum FLOWTYPE {
		ASSIGN, CAST, RETURN, FORMAL
	}

	public FlowConstraint(ID to, ID from, FLOWTYPE flowType) {
		this.to = to;
		this.from = from;
		this.flowType = flowType;
	}

	private CSGraph tempGraph;

	public String toString(ValueHolder vh) {
		StringBuilder ret = new StringBuilder(to.getVarName());
		if (flowType == FLOWTYPE.ASSIGN)
			ret.append(" = ");
		else if (flowType == FLOWTYPE.CAST)
			ret.append(" <--CAST  ");
		else if (flowType == FLOWTYPE.RETURN)
			ret.append(" <--RETURN ");
		ret.append(from.getVarName());

		ret.append(" ---> ").append(to.uniqueID()).append(" (")
				.append(vh.printValue((GraphElement) to)).append(")");
		if (tempGraph != null)
			ret.append(tempGraph.hasInst((GraphElement) to) ? "(*)" : "");
		ret.append(", ");

		if (tempGraph != null)
			ret.append(tempGraph.hasInst((GraphElement) from) ? "(*)" : "");

		ret.append(", ").append(from.uniqueID()).append(" (")
				.append(vh.printValue((GraphElement) from)).append(")");

		return ret.toString();
	}

	public ID getFrom() {
		return from;
	}

	public ID getTo() {
		return to;
	}

	public FLOWTYPE getType() {
		return flowType;
	}

	public Constraint refresh(AnalysisContext context) {
		// 1. refresh left;
		ID nFrom = from.refresh(context);

		// 2. refresh right;
		ID nTo = to.refresh(context);

		return new FlowConstraint(nTo, nFrom, flowType);
	}

	@Override
	public void buildGraph(CSGraph graph) {
		List<GraphBuilder> list = new LinkedList<GraphBuilder>();

		// build for right side
		((GraphBuilder) from).buildGraph(graph);

		// build for left side
		((GraphBuilder) to).buildGraph(graph);

		// build for FLOW
		graph.addFlow((GraphElement) from, (GraphElement) to);

		tempGraph = graph;
	}
}
