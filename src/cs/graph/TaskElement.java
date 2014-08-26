package cs.graph;

import java.util.Map;

import cs.data.id.InstID;

/*
 * A TaskElement is a node that a binding of an InstID to this element may result in a new GraphBuilder
 * 
 * e.g. CallConstraint and FieldID are TaskElement because they will result in either a MethodExpander or a FieldDisambiguator
 */
public interface TaskElement {

	/*
	 * a string representative from an instID to this TaskElement.
	 * 
	 * This can be used as a key to search if this binding is already precessed
	 * or not
	 */
	public String lookUpName(InstID instID);

	/*
	 * return the GraphBuilder that this TaskElement should produce
	 */
	public GraphBuilder getGraphBuilder(InstID instID);

	public String uniqueID();
}
