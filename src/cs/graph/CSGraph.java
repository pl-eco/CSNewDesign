package cs.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cs.data.constraint.CallConstraint;
import cs.data.constraint.Constraint;
import cs.data.id.FieldID;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.linearsolver.CPlexWrapper;
import cs.linearsolver.ValueHolder;
import cs.types.csTypeSystem_c;
import cs.util.EasyDebugger;

public abstract class CSGraph {
	abstract public int instanceCount();

	abstract public List<GraphBuilder> addInstance(InstID instID);

	abstract public List<InstID> getInstanceList();

	abstract public CPlexWrapper getCPlexSolver();

	// just for debugging
	abstract public boolean hasInst(GraphElement node);

	abstract public csTypeSystem_c getTypeSystem();

	abstract public boolean addFlow(GraphElement from, GraphElement to);

	abstract public boolean addCall(String uniqueID, CallConstraint call);

	abstract public boolean addField(String uniqueID, FieldID field);

	abstract public boolean addTask(String uniqueID, TaskElement field);

	abstract public void addExtraConstraint();

	// return if the task is removed
	abstract public boolean removeTask(String targetID, String uniqueID);

	abstract protected List<GraphBuilder> findNewPath();

	/* this method check if two given alias names refer to each other */
	abstract public boolean checkAlias(String alias1, String alias2);

	// return the list of InstID that bind to with the given uniqueID
	abstract public List<InstID> getInstance(String uniqueID);

	abstract public boolean solve(ValueHolder vh, boolean printConstraints,
			boolean printEquations);

	// abstract public boolean ifCalled(InstID instID, CallConstraint call);

	abstract public String printGraph();

	protected List<GraphBuilder> tempBuilderList = new LinkedList<GraphBuilder>();

	protected GraphHelper<GraphElement, GraphElement> predVertex = new GraphHelper<GraphElement, GraphElement>();

	protected GraphHelper<GraphElement, GraphElement> postVertex = new GraphHelper<GraphElement, GraphElement>();

	public final boolean addEdge(GraphElement from, GraphElement to) {
		if (from.equals(to) || postVertex.getSet(from).contains(to)
				|| predVertex.getSet(to).contains(from))
			return false;

		// EasyDebugger
		// .debug("from: " + from.uniqueID() + " to: " + to.uniqueID());

		// add self to the connection
		predVertex.addIfEmpty(from, from);
		postVertex.addIfEmpty(from, from);

		predVertex.addIfEmpty(to, to);
		postVertex.addIfEmpty(to, to);

		// add "to" and "to"'s post to "from" and "from"'s pred's post
		for (GraphElement fromPred : predVertex.getSet(from)) {
			postVertex.getSet(fromPred).addAll(postVertex.getSet(to));
		}

		// add "from" and "from"'s pred to "to" and "to"'s pred
		for (GraphElement toPost : postVertex.getSet(to)) {
			predVertex.getSet(toPost).addAll(predVertex.getSet(from));
		}

		return true;
	}

	/*
	 * this method add a list of GraphBuilder object, which will be returned
	 * together when findNewPath is invoked
	 * 
	 * After this list is returned, they must be removed, the graph will not
	 * remember them for use in future
	 */
	public void addGraphBuilders(List<GraphBuilder> builders) {
		tempBuilderList.addAll(builders);
	}

	/*
	 * clears the tempBuilderList, this method should be called right after the
	 * "fineNewPath" method
	 */
	private void clearTempList() {
		tempBuilderList.clear();
	}

	public final List<GraphBuilder> findBuildingTask() {
		List<GraphBuilder> list = new LinkedList<GraphBuilder>();

		// add temp list
		list.addAll(tempBuilderList);
		clearTempList();

		// add new path
		list.addAll(this.findNewPath());

		return list;
	}
}
