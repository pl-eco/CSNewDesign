package cs.data.id;

import java.io.Serializable;

import cs.graph.CSGraph;
import cs.graph.GraphElement;
import cs.linearsolver.ValueHolder;
import cs.util.CSUtil;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

/*
 * present Local, Call
 */
public class LocalID extends ID implements GraphElement, Serializable {

	public LocalID(String id, String varName, String pos) {
		super(id, varName, pos);
	}

	public LocalID(String varName, String pos) {
		this(VariableGenerator.nextAlias(), varName, pos);
	}

	@Override
	public ID refresh(AnalysisContext context) {
		return new LocalID(context.refreshID(id), varName, position);
	}

	@Override
	public void buildGraph(CSGraph graph) {
	}
}
