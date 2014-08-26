package cs.types;

import polyglot.ext.jl5.types.JL5ArrayType_c;
import polyglot.types.ArrayType_c;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import cs.data.id.ID;
import cs.data.id.VariableGenerator;

public class CSArrayType extends JL5ArrayType_c implements CSBaseType {
	private String aliasName;

	private ID id = null;

	public CSArrayType(TypeSystem ts, Position pos, Type base, boolean isVarargs) {
		super(ts, pos, base, isVarargs);
		assert base != null;
		this.aliasName = VariableGenerator.nextAlias();
	}

	public String getAliasName() {
		return aliasName;
	}

	public Type getType() {
		return this.toType();
	}

	public void setType(Type inner_type) {
		assert false : "Shouldn't invoke this";
	}

	@Override
	public void setID(ID id) {
		assert id != null;
		this.id = id;
	}

	@Override
	public ID getID() {
		assert this.id != null;
		return this.id;
	}

	@Override
	public void setAliasName(String name) {
		assert false;
	}
}
