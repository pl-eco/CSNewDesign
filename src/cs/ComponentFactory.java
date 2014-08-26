package cs;

import polyglot.types.Type;
import cs.graph.CSGraph;
import cs.graph.GraphElement;
import cs.linearsolver.CPlexWrapper;
import cs.linearsolver.ValueHolder;
import cs.types.CSNonGenericType;
import cs.types.CSNonGenericType_c;

public interface ComponentFactory {
	public CPlexWrapper createCPlexSolver();

	public CSGraph getGraph();

	public ValueHolder getValueHolder();

	public CSNonGenericType createDefaultObjectType(String aliasName, Type type);

	public CSNonGenericType createDefaultObjectType(Type type);
}
