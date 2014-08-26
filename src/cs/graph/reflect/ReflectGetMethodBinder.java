package cs.graph.reflect;



import cs.data.id.InstID;
import cs.data.reflect.ReflectGetMethodOrConstructor;
import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.util.CSUtil;
import cs.util.EasyDebugger;

public class ReflectGetMethodBinder implements GraphBuilder {
	private InstID instID;
	private ReflectGetMethodOrConstructor getMethod;

	public ReflectGetMethodBinder(InstID instID,
			ReflectGetMethodOrConstructor getMethod) {
		this.instID = instID;
		this.getMethod = getMethod;
	}

	@Override
	public void buildGraph(CSGraph graph) {
		// 1. set instID
		getMethod.setInstID(instID);

		// 2. remove getMethod from taskMap
		boolean removed = graph.removeTask(getMethod.target().uniqueID(),
				getMethod.uniqueID());
		assert removed;

		// 3. add getMethod to instMap
		graph.addInstance(getMethod);
	}

}
