package cs.data.constraint;

import java.util.LinkedList;
import java.util.List;

import polyglot.types.Context;
import polyglot.types.Context_c;

import cs.data.ConstraintContainer;
import cs.data.Properties;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.data.id.SpecialID;
import cs.data.id.VariableGenerator;
import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.graph.MethodExpander;
import cs.graph.TaskElement;
import cs.linearsolver.ValueHolder;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

public class CallConstraint implements Constraint, GraphBuilder, TaskElement {
	public static final String CONSTRUCTOR_NAME = "!Constructor";
	public static final String GLOBAL_NAME = "!Global";

	protected final String id;
	protected ID target;
	protected String methodName;
	protected List<ID> arguments;
	protected Context_c staticContext;
	protected AnalysisContext context;

	private String targetClass;

	public String targetClass() {
		return targetClass;
	}

	public Context getStaticContext() {
		return staticContext;
	}

	public AnalysisContext getAnalysisContext() {
		assert context != null : this.id + " " + this.methodName();
		return context;
	}

	public boolean isConstructorCall() {
		return this.methodName().contains(CONSTRUCTOR_NAME);
	}

	// public static createConstructorCall() {
	// return;
	// }

	public CallConstraint(ID target, String targetClass, String methodName,
			List<ID> args, Context_c staticContext) {
		this(VariableGenerator.nextAlias(), target, targetClass, methodName,
				args, staticContext);
	}

	public CallConstraint(ID target, String targetClass, String methodName,
			List<ID> args, AnalysisContext context) {
		this(VariableGenerator.nextAlias(), target, targetClass, methodName,
				args, null, context);
	}

	public CallConstraint(String id, ID target, String targetClass,
			String methodName, List<ID> args, Context_c staticContext) {
		this(id, target, targetClass, methodName, args, staticContext, null);
	}

	protected CallConstraint(String id, ID target, String targetClass,
			String methodName, List<ID> args, Context_c staticContext,
			AnalysisContext context) {
		assert id != null;
		assert target != null;
		assert methodName != null;
		assert args != null;

		this.id = id;
		this.target = target;
		this.targetClass = targetClass;
		this.methodName = methodName;
		this.arguments = args;
		this.staticContext = staticContext;
		this.context = context;
	}

	public String desc() {
		StringBuilder sb = new StringBuilder("Call: ");

		sb.append(target.getVarName()).append(".").append(methodName());

		if (arguments.size() > 0) {
			sb.append("\n  ARGS:");
			for (ID arg : this.arguments)
				sb.append(arg.uniqueID()).append(
						ConstraintContainer.ARG_SEPARATOR);
		}
		sb.append("\n  Return value:").append(this.invocationPoint());
		return sb.toString();
	}

	@Override
	public String toString(ValueHolder vh) {
		return target.getVarName() + "." + methodName;
	}

	public String methodName() {
		return methodName;
	}

	public ID target() {
		return target;
	}

	public List<ID> arguments() {
		return arguments;
	}

	// should return a string that represent invocation point. This is used to
	// identify the recursion
	public String invocationPoint() {
		return id;
	}

	public String uniqueID() {
		return id;
	}

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
		return new CallConstraint(nID, nTarget, targetClass, methodName,
				nArguments, staticContext, context);
	}

	@Override
	public void buildGraph(CSGraph graph) {
		// 1. build graph for target
		((GraphBuilder) target).buildGraph(graph);

		// 2. build graph for arguments
		for (ID id : arguments) {
			((GraphBuilder) id).buildGraph(graph);
		}

		graph.addCall(target.uniqueID(), this);
	}

	// used for comparison
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object call) {
		return this.id.equals(((CallConstraint) call).id);
	}

	@Override
	public String lookUpName(InstID instID) {
		return instID.uniqueID() + ConstraintContainer.CLASS_SEPARATOR
				+ invocationPoint();
	}

	@Override
	public GraphBuilder getGraphBuilder(InstID instID) {
		return new MethodExpander(instID, this);
	}

}
