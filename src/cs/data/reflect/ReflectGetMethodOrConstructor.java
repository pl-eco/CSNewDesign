package cs.data.reflect;

import java.util.LinkedList;
import java.util.List;

import cs.data.ConstraintContainer;
import cs.data.constraint.CallConstraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.graph.TaskElement;
import cs.graph.reflect.ReflectGetMethodBinder;
import cs.linearsolver.ValueHolder;
import cs.visit.AnalysisContext;

//class.getMethod();
/*
 * Before the instance of its target is binded, this class is a TaskElement, the task is to bind its target's instance. After that, it is an instID
 */
public class ReflectGetMethodOrConstructor extends InstID implements
		TaskElement {
	private ID target;

	private InstID instID;

	public InstID instID() {
		assert instID != null;
		return instID;
	}

	public void setInstID(InstID instID) {
		this.instID = instID;
	}

	public ID target() {
		return target;
	}

	private String methodName;

	// methodName.equals(NameIndex.constructorName) --> "getConstructor"
	// else --> "getMethod"
	public ReflectGetMethodOrConstructor(String id, ID target, String methodName) {
		this(id, target, methodName, null);
	}

	private ReflectGetMethodOrConstructor(String id, ID target,
			String methodName, InstID instID) {
		this(id, target, methodName, instID, null);
	}

	private ReflectGetMethodOrConstructor(String id, ID target,
			String methodName, InstID instID, AnalysisContext context) {
		super(id, methodName, id + "M/C");
		this.target = target;
		this.methodName = methodName;
		this.instID = instID;
		this.context = context;
	}

	@Override
	public ID refresh(AnalysisContext context) {
		// 1. refresh the id
		String uid = context.refreshID(id);
		ID nTarget = target.refresh(context);
		return new ReflectGetMethodOrConstructor(uid, nTarget, methodName,
				instID, context);
	}

	public void buildGraph(CSGraph graph) {
		// put to TaskMap
		graph.addTask(target.uniqueID(), this);
	}

	public String toString() {
		return "class."
				+ (methodName != null
						&& methodName
								.startsWith(CallConstraint.CONSTRUCTOR_NAME) ? "getConstructor("
						: ("getMethod(" + methodName)) + ")";
	}

	public String getReflectionInfo() {
		if (methodName == null || methodName.startsWith("#"))
			return ReflectionHandler.UNKNOWN_NAME;

		return target.uniqueID() + "$" + methodName;
	}

	public String methodName() {
		return methodName;
	}

	@Override
	public String className() {
		// this must return the loaded className
		assert instID != null;
		return instID.className();
	}

	@Override
	public String lookUpName(InstID instID) {
		return instID.uniqueID() + ConstraintContainer.CLASS_SEPARATOR
				+ uniqueID();
	}

	@Override
	public GraphBuilder getGraphBuilder(InstID instID) {
		return new ReflectGetMethodBinder(instID, this);
	}

}
