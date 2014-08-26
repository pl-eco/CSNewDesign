package cs.types;

import polyglot.ext.jl5.types.JL5NullType_c;
import polyglot.types.NullType_c;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import cs.data.id.ID;
import cs.data.id.LocalID;
import cs.data.id.VariableGenerator;
import cs.util.EasyDebugger;

public class CSNullType extends JL5NullType_c implements CSBaseType {
	private String aliasName;
	private ID id;

	public CSNullType(TypeSystem ts) {
		super(ts);
		aliasName = VariableGenerator.nextAlias();
	}

	@Override
	public String getAliasName() {
		return aliasName;
	}

	// @Override
	// public Type getType() {
	// return this.toType();
	// }
	//
	// @Override
	// public void setType(Type inner_type) {
	// EasyDebugger.exit("Shouldn't call this");
	// }

	@Override
	public CSNullType copy() {
		CSNullType type = null; // new CSArrayType();
		try {
			type = (CSNullType) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return type;
	}

	@Override
	public void setID(ID id) {
		this.id = id;
	}

	@Override
	public ID getID() {
		return new LocalID("null", "null");
	}

	@Override
	public void setAliasName(String name) {
		assert false;
	}
}
