package cs.types;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.types.Type;

public interface CSNonGenericType extends CSObjectType, JL5ParsedClassType {

	public void setType(Type type);

	public Type getType();

	public String getTypeLabel();

	public void setTypeLabel(String typeLabel);
}
