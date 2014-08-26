package cs.data.reflect;

import java.util.LinkedList;
import java.util.List;

import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.graph.GraphBuilder;
import cs.graph.reflect.ReflectDisambiguator;
import cs.visit.AnalysisContext;

public abstract class ReflectID extends CallConstraint {

	public ReflectID(String id, ID target, String methodName, List<ID> args,
			AnalysisContext context) {
		super(id, target, ReflectionHandler.UNKNOWN_NAME, methodName, args,
				null, context);
	}

	public abstract List<ID> callArguments();

	abstract public Constraint refresh(AnalysisContext context);

	@Override
	public GraphBuilder getGraphBuilder(InstID instID) {
		return new ReflectDisambiguator(instID, this);
	}
}
