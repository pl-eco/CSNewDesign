package cs.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import linearsolver.CPlexOriginalEquations;
//import linearsolver.CPlexWrapper;

import cs.data.Properties;
import cs.data.constraint.CallConstraint;
import cs.data.id.FieldID;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.data.id.LocalID;
import cs.data.id.SpecialID;
import cs.linearsolver.CPlexWrapper;
import cs.linearsolver.ValueHolder;
import cs.types.csTypeSystem_c;
import cs.util.CSUtil;
import cs.util.EasyDebugger;

public class CSSimpleGraph extends CSGraph {
	protected static final List<GraphBuilder> emptyList = new LinkedList<GraphBuilder>();
	private static final Object exist = new Boolean(true);

	// cache the connected vertex between instance to methodMap/fieldMap
	// associated with the instance as target
	protected Map<String, Object> connectionCache = new HashMap<String, Object>();

	protected Map<String, InstID> instMap = new HashMap<String, InstID>();

	protected CPlexWrapper cplex = null;

	protected csTypeSystem_c ts = null;

	protected ValueHolder vh = null;
	/*
	 * set C
	 * 
	 * key: uid of the call's target
	 */
	// private GraphHelper<String, CallConstraint> methodMap = new
	// GraphHelper<String, CallConstraint>();

	// set D
	private GraphHelper<String, TaskElement> taskMap = new GraphHelper<String, TaskElement>();

	public CSSimpleGraph(csTypeSystem_c ts) {
		this.ts = ts;
		cplex = ts.createCPlexSolver();
		this.vh = ts.getValueHolder();
	}

	public CPlexWrapper getCPlexSolver() {
		return cplex;
	}

	public String printGraph() {
		StringBuilder graph = new StringBuilder("");

		graph.append("setC: ").append(taskMap.toString());

		return graph.toString();
	}

	private int appRM = 0;
	private int libRM = 0;

	public void addReachableMethod(String className) {
		if (className.startsWith("java."))
			++libRM;
		else
			++appRM;
	}

	// check if the given node currently bind to any instance
	public boolean hasInst(GraphElement node) {
		for (GraphElement pred : predVertex.getSet(node)) {
			if (instMap.containsKey(pred.uniqueID()))
				return true;
		}
		return false;
	}

	/*
	 * return the list of instance that participated in solving
	 * 
	 * Only the instIDs that are aliased/referenced can be added
	 */
	@Override
	public List<InstID> getInstanceList() {
		List<InstID> list = new LinkedList<InstID>();
		for (InstID id : instMap.values()) {
			// postVertex contains it self
			// int count = 0;
			// for (GraphElement elmt : postVertex.getSet(id)) {
			// if (!(elmt instanceof SpecialID))
			// count++;
			// if (count > 1) {
			list.add(id);
			// break;
			// }
			// }
		}
		return list;
	}

	@Override
	public int instanceCount() {
		return instMap.size();
	}

	public void report() {
		System.out.println("Reachable method, lib: " + this.libRM + "  app: "
				+ this.appRM);

		System.out.println("# of instance: " + instanceCount());
		System.out.println("# of constraints: " + Properties.constraints);
	}

	@Override
	/*
	 * Solve the equation set
	 */
	public boolean solve(ValueHolder vh, boolean printConstraints,
			boolean printEquations) {
		if (cplex == null) return false;
		Object solvingTime = new Object();
		Properties.stopWatchStart(solvingTime);
		boolean solvable = cplex.solve();
		long elapse = Properties.stopWatchEnd(solvingTime) / 1000;

		if (solvable)
			vh.setCplexResult(cplex.getSolution());

		if (printEquations) {
			cplex.printEquations();
			EasyDebugger.printMap(cplex.getSolution());
		}

		System.out.println("solving time: " + elapse + " secs");
		System.out.println("Solvable: " + solvable);

		// vh.printPreValues();

		return solvable;
	}

	@Override
	public void addExtraConstraint() {
		if (cplex != null) cplex.addExtraEq();
	}

	@Override
	public List<GraphBuilder> addInstance(InstID instID) {
		// put the InstID node to the set A
		assert instID.getContext() != null : instID.toString();

		if (!instMap.containsKey(instID.uniqueID())) {
			instMap.put(instID.uniqueID(), instID);
		}

		// the above is necessary, since "new A().m()" is possible
		predVertex.addIfEmpty(instID, instID);
		postVertex.addIfEmpty(instID, instID);

		return emptyList;
	}

	@Override
	public boolean addFlow(GraphElement from, GraphElement to) {
		// add equations
		boolean ret = super.addEdge(from, to);
		if (ret && cplex != null) {
			cplex.addFlow(from, to);
			Properties.constraints++;
		}
		// add a directed edge to the graph
		return ret;
	}

	@Override
	public boolean addTask(String targetID, TaskElement task) {
		return taskMap.addIfEmpty(targetID, task);
	}

	@Override
	public boolean removeTask(String targetID, String uniqueID) {
		if (taskMap.contains(targetID)) {
			Set<TaskElement> tasks = taskMap.getSet(targetID);
			for (TaskElement task : tasks) {
				if (task.uniqueID().equals(uniqueID)) {
					return tasks.remove(task);
				}
			}
		}
		return false;
	}

	@Override
	public boolean addCall(String targetID, CallConstraint call) {
		// add a call to the set C
		boolean ret = addTask(targetID, call);
		if (ret && cplex != null) {
			cplex.addCall(call);
			Properties.constraints++;
		}
		return ret;
	}

	@Override
	public boolean addField(String key, FieldID field) {
		// add a field to the set D
		// if the field is "this/super.field", don't do anything
		if (!(field.target() instanceof SpecialID)) {
			return addTask(key, field);
		}
		return false;
	}

	@Override
	public List<InstID> getInstance(String uniqueID) {
		List<InstID> list = new LinkedList<InstID>();
		for (GraphElement preNode : predVertex.getSet(new GraphID(uniqueID))) {
			if (instMap.containsKey(preNode.uniqueID()))
				list.add(instMap.get(preNode.uniqueID()));
		}
		return list;
	}

	@Override
	/*
	 * find a new path in the graph
	 */
	public List<GraphBuilder> findNewPath() {
		List<GraphBuilder> list = new LinkedList<GraphBuilder>();
		/*
		 * check the reachability of each element in instanceMap to methodMap
		 * and fieldMap.
		 */
		for (InstID instID : instMap.values()) {
			// for each of the instID's post node
			for (GraphElement postNode : postVertex.getSet(instID)) {
				// if the reachable is in set C, add a MethodExpander
				if (taskMap.contains(postNode.uniqueID())) {
					doTask(list, instID, postNode);
				}
			}
		}
		return list;
	}

	// generate GraphBuilder for call (MethodExpander)
	private void doTask(List<GraphBuilder> list, InstID instID,
			GraphElement node) {
		for (TaskElement taskElement : taskMap.getSet(node.uniqueID())) {
			String cacheName = taskElement.lookUpName(instID);
			if (!connectionCache.containsKey(cacheName)) {
				list.add(taskElement.getGraphBuilder(instID));
				// label the path from instID + CallConstraint as used
				connectionCache.put(cacheName, this.exist);
			}
		}
	}

	@Override
	public csTypeSystem_c getTypeSystem() {
		return ts;
	}

	@Override
	// make sure the input is non-refreshed value (first pass or before).
	// Otherwise, the input will be converted back to non-refreshed value
	public boolean checkAlias(String alias1, String alias2) {
		if(alias1 == null || alias2 == null) return false;
		
		if(alias1.equals(alias2)) {
			return true;
		}
		/*
		 * the use of CSUtil.getAliasName is to make sure the input is a
		 * non-refreshed value. otherwise, a refreshed value will be converted
		 * into a non-refreshed value
		 */
		Set<String> uidSet1 = vh.getMappedID(CSUtil.getAliasName(alias1));
		Set<String> uidSet2 = vh.getMappedID(CSUtil.getAliasName(alias2));

		if (uidSet1 == null || uidSet2 == null)
			return false;
		
		// check in terms of Cartesian product
		for (String uid1 : uidSet1) {
			ID wrapped1 = new LocalID(uid1, "fakeID", "fakePos");
			for (String uid2 : uidSet2) {
				ID wrapped2 = new LocalID(uid2, "fakeID", "fakePos");
				if (this.predVertex.getSet((GraphElement) wrapped1).contains(
						wrapped2)) {
					return true;
				}
				if (this.predVertex.getSet((GraphElement) wrapped2).contains(
						wrapped1)) {
					return true;
				}
			}
		}
		return false;
	}
}
