package cs.data.reflect;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.graph.GraphBuilder;
import cs.graph.reflect.ReflectDisambiguator;
import cs.linearsolver.ValueHolder;
import cs.visit.AnalysisContext;

public class ReflectMethodInvoke extends ReflectID {
	private String callTargetClass = null;

	public String callTargetClass() {
		return callTargetClass;
	}

	public ReflectMethodInvoke(String id, ID target, List<ID> arguments,
			String callTargetClass) {
		this(id, target, arguments, callTargetClass, null);
	}

	/*
	 * the argument for MethodInvoke starts from the target of the reflected
	 * call, followed by the arguments
	 */
	private ReflectMethodInvoke(String id, ID target, List<ID> arguments,
			String callTargetClass, AnalysisContext context) {
		super(id, target, "invoke", arguments, context);
		this.callTargetClass = callTargetClass;
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
		return new ReflectMethodInvoke(nID, nTarget, nArguments,
				callTargetClass, context);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("invoke(");
		for (Object arg : arguments)
			sb.append(arg.toString()).append(",");
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}

	// the target of the call this class represents
	public ID callTarget() {
		return arguments.get(0);
	}

	public List<ID> callArguments() {
		return arguments.subList(1, arguments.size());
	}
}
