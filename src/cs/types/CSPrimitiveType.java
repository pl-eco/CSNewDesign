package cs.types;

import polyglot.ext.jl5.types.JL5PrimitiveType_c;
import polyglot.types.PrimitiveType_c;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import cs.data.ConstraintContainer;
import cs.data.id.ID;
import cs.data.id.LocalID;
import cs.data.id.VariableGenerator;
import cs.util.EasyDebugger;

public class CSPrimitiveType extends JL5PrimitiveType_c implements CSBaseType {
	private static final String PRIMITIVEIGNORE = "_PRIMITIVEIGNORE";
	private static final LocalID fixedID = new LocalID(PRIMITIVEIGNORE,
			"primitive",
			"primitive");

	public CSPrimitiveType(TypeSystem ts, Kind kind) {
		super(ts, kind);
	}

	@Override
	public void setID(ID id) {
	}

	@Override
	public ID getID() {
		return fixedID;
	}

	@Override
	public String getAliasName() {
		return PRIMITIVEIGNORE;
	}

	@Override
	public void setAliasName(String name) {
		assert false;
	}

	// @Override
	// public Type getType() {
	// return this.toType();
	// }
	//
	// @Override
	// public void setType(Type inner_type) {
	// EasyDebugger.exit("should never invoke this");
	// }

}
