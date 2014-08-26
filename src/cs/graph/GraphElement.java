package cs.graph;

public interface GraphElement {
	// // return the id or p c value
	// public PCInfo getTypeInfo();

	@Override
	public boolean equals(Object o);

	@Override
	int hashCode();

	public String uniqueID();
}
