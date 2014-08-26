package cs.types;

import polyglot.types.Type;
import cs.data.id.ID;

public interface CSBaseType {
	public void setID(ID id);

	public ID getID();

	public String getAliasName();

	public void setAliasName(String name);

	// public Type getType();
	//
	// public void setType(Type inner_type);
}