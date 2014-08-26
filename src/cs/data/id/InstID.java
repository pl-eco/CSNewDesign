package cs.data.id;

import java.util.LinkedList;
import java.util.List;

import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.graph.GraphElement;
import cs.linearsolver.ValueHolder;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

public class InstID extends ID implements GraphElement {
	private static List<ID> EMPTY_ARGS = new LinkedList<ID>();

	// constructor name
	private boolean isStatic;

	private String className;

	// used in second pass
	protected AnalysisContext context;

	public InstID(String id, String className, String position) {
		this(id, className, position, false, null);
	}

	// ONLY used for static target
	// use class name as the id
	public InstID(String className, boolean isStatic, AnalysisContext context) {
		this(className, className, "~STATIC OF " + className, isStatic, context);
	}

	// used for refreshing
	public InstID(String id, String className, String position,
			boolean isStatic, AnalysisContext context) {
		super(id, isStatic ? "~STATIC" : ("new " + className), position);

		assert className != null;

		this.className = className;

		this.isStatic = isStatic;
		this.context = context;
	}

	public String className() {
		return className;
	}

	public AnalysisContext getContext() {
		return context;
	}

	/*
	 * b = a; b.f; a.f; a = new A(); a = new B();
	 */
	@Override
	public ID refresh(AnalysisContext context) {
		// 1. refresh the id
		String uid = context.refreshID(id);

		return new InstID(uid, className, position, isStatic, context);
	}

	// this field value is used within the instance
	public String fieldNamed(String fieldName) {
		return id + AnalysisContext.CONTEXT_SEPARATOR + fieldName;
	}

	@Override
	/*
	 * InstID should be translated to a new instance + a constructor call
	 * 
	 * Add InstID binded constructor call + the class level constraints
	 */
	public void buildGraph(CSGraph graph) {
		// 1. register the instance to the graph
		ValueHolder vh = graph.getTypeSystem().getValueHolder();

		if (vh.ifOptimizablePri(this.pgmLabel()))
			return;

		graph.addInstance(this);
	}
}