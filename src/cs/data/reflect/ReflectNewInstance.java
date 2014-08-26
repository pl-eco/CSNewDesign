package cs.data.reflect;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.graph.GraphBuilder;
import cs.graph.MethodExpander;
import cs.linearsolver.ValueHolder;
import cs.types.CSBaseType;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

public class ReflectNewInstance extends ReflectID {

	public ReflectNewInstance(String id, ID target, List<ID> args) {
		this(id, target, args, null);
	}

	private ReflectNewInstance(String id, ID target, List<ID> args,
			AnalysisContext context) {
		super(id, target, "newInstance", args, context);
	}

	@Override
	public Constraint refresh(AnalysisContext context) {
		// 1. refresh aliasName
		String nID = context.refreshID(id);

		// 2. refresh target;
		ID nTarget = target.refresh(context);

		// 3. refresh arguments
		List<ID> nArguments = new LinkedList<ID>();
		for (ID id : arguments) {
			nArguments.add(id.refresh(context));
		}

		// create a new call constraint
		return new ReflectNewInstance(nID, nTarget, nArguments, context);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("newInstance(");
		for (ID arg : super.arguments)
			sb.append(arg.toString()).append(",");
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");

		return sb.toString();
	}

	@Override
	public List<ID> callArguments() {
		return arguments;
	}
}
