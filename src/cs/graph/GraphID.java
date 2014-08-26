package cs.graph;

public class GraphID implements GraphElement {
	protected String uid;

	public GraphID(String uid) {
		this.uid = uid;
	}

	@Override
	public final boolean equals(Object o) {
		GraphElement element = (GraphElement) o;
		return this.uniqueID().equals(element.uniqueID());
	}

	@Override
	public String toString() {
		return uid;
	}

	@Override
	public final int hashCode() {
		return uniqueID().hashCode();
	}

	@Override
	public String uniqueID() {
		return uid;
	}
}
