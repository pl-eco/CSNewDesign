package cs.data.id;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.graph.GraphElement;
import cs.graph.TargetableGraphElement;
import cs.linearsolver.ValueHolder;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;

/*
 * This class represents the target of static field/method
 * The p and c value of this ID is to the current context
 */

public class StaticID extends ID implements GraphElement {
	private static Map<String, InstID> staitcIDMap = new HashMap<String, InstID>();
	// the position of this staticID relative to higher up context(e.g. Main)
	private InstID source;

	public StaticID(String className, String varName, String position) {
		this(VariableGenerator.nextAlias(), getStaticInstID(className),
				varName, position);
	}

	private StaticID(String id, InstID source, String varName, String position) {
		super(id, varName, position);
		this.source = source;
	}

	public String className() {
		return source.className();
	}

	@Override
	public void buildGraph(CSGraph graph) {
		ID convert = new LocalID(varName + "<convert>", position + "<convert>");
		GraphElement to = (GraphElement) new TargetableGraphElement(this,
				(GraphElement) convert);

		// flow from source to current id
		graph.addFlow(source, to);

		// add instance
		graph.addInstance(source);

	}

	@Override
	public ID refresh(AnalysisContext context) {
		String uniqueID = context.refreshID(id);
		return new StaticID(uniqueID, source, varName, position);
	}

	// input: class name and fields of that class
	// output: instID represent target of static field/method. the fieldList
	// must be all static field
	private static InstID getStaticInstID(String className) {
		assert className != null && className.length() > 3;

		if (!staitcIDMap.containsKey(className)) {
			InstID instID = new InstID(className, true,
					AnalysisContext.createEntryContext());
			staitcIDMap.put(className, instID);
		}
		return staitcIDMap.get(className);
	}
}
