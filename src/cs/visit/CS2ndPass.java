package cs.visit;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceFile;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;
import cs.data.ConstraintContainer;
import cs.data.Properties;
import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.constraint.FlowConstraint;
import cs.graph.CSGraph;
import cs.graph.CSSimpleGraph;
import cs.graph.FieldDisambiguator;
import cs.graph.GraphBuilder;
import cs.types.csTypeSystem_c;
import cs.util.EasyDebugger;

public abstract class CS2ndPass extends TypeChecker {

	public CS2ndPass(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	// make sure second is only visited once
	private static boolean isPorcessed = false;

	protected Node leaveCall(Node old, Node n, NodeVisitor v)
			throws SemanticException {
		Node m = n.del().typeCheck((TypeChecker) v);

		if (m instanceof Expr && ((Expr) m).type() == null) {
			throw new InternalCompilerError("Null type for " + m, m.position());
		}

		if (m instanceof SourceFile) {
			// if(true)return m;
			if (CS2ndPass.isPorcessed)
				return m;
			CS2ndPass.isPorcessed = true;

			preCSEngine();
			CSGraph graph = runCSEngine();
			postCSEngine(graph);
		}

		return m;
	}

	protected void preCSEngine(){}
	
	protected void postCSEngine(CSGraph graph){}

	private CSGraph runCSEngine() {
		System.out.println("\n|-----------------------------------------|");
		System.out.println("|-----CONTEXT SENSITIVE ENGINE START------|");
		System.out.println("|-----------------------------------------|");

		/*
		 * 1. Load property file
		 */
		String mainClassName = Properties.entryClass;
		String mainMethodName = Properties.entryMethod;

		/*
		 * 2. Prepare the statement stack (all of statement to be processed will
		 * be pushed into stack, the engine will not stop until the stack is
		 * empty)
		 */
		List<Constraint> entryMethod = null;

		/*
		 * put in main method
		 */
		assert mainClassName != null && mainMethodName != null;
		entryMethod = ConstraintContainer.getInstance().getEntryMethodList(
				mainClassName, mainMethodName);
		/*
		 * put in static statements consider static statement as in entry main's
		 * context
		 */
		entryMethod.addAll(ConstraintContainer.getInstance()
				.getStaticContainer());

		// create the root context
		AnalysisContext context = AnalysisContext.createEntryContext();
		// the queue for graph tasks
		Queue<GraphBuilder> graphTasks = new LinkedList<GraphBuilder>();

		// refresh
		for (Constraint cons : entryMethod) {
			graphTasks.add((GraphBuilder) cons.refresh(context));
		}

		// start parsing
		// we must ensure constraints in "workQueue" is already refreshed
		CSGraph graph = ((csTypeSystem_c) ts).getGraph();

		boolean flag = true;
		while (flag) {
			flag = false;
			while (!graphTasks.isEmpty()) {
				GraphBuilder cons = graphTasks.poll();
				// build the graph;
				cons.buildGraph(graph);
				// debugging
				//if (cons instanceof Constraint) {
				//	EasyDebugger.debug(((Constraint) cons)
				//			.toString(((csTypeSystem_c) ts).getValueHolder()));
				//}
			}
			List<GraphBuilder> tasks = graph.findBuildingTask();
			if (tasks.size() > 0) {
				flag = true;
				graphTasks.addAll(tasks);
			}
		}
		return graph;
	}
}