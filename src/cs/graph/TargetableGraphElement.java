package cs.graph;

import cs.data.id.Targetable;

public class TargetableGraphElement implements Targetable, GraphElement {
	private GraphElement target;
	private GraphElement inner;

	public TargetableGraphElement(GraphElement inner, GraphElement target) {
		this.inner = inner;
		this.target = target;
	}

	@Override
	public String toString() {
		return "target: " + target + " inner: " + inner;
	}

	@Override
	public GraphElement target() {
		return target;
	}

	@Override
	public final boolean equals(Object o) {
		GraphElement element = (GraphElement) o;
		return this.uniqueID().equals(element.uniqueID());
	}

	@Override
	public final int hashCode() {
		return inner.uniqueID().hashCode();
	}

	@Override
	public String uniqueID() {
		return inner.uniqueID();
	}
}
