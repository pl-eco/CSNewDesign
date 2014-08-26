package cs.types;

import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import polyglot.ext.jl5.types.TypeVariable_c;
import polyglot.types.ReferenceType;
import polyglot.types.TypeObject_c;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.types.ClassType;

public class CSTypeVariable extends TypeVariable_c implements CSObjectType {
	public CSTypeVariable(TypeSystem ts, Position pos, String id,
			ReferenceType upperBound) {
		super(ts, pos, id, upperBound);

	}

	private String alias = VariableGenerator.nextAlias();;
	private ID id;

	/*
	 * Both CSTypeVariable and its upperBound should be duplicated
	 */
	public CSTypeVariable duplicate() {
		CSTypeVariable tv = (CSTypeVariable) this.copy();
		tv.id = null;
		tv.alias = VariableGenerator.nextAlias();

		if (tv.upperBound() instanceof CSNonGenericType) {
			CSNonGenericType newUpperBound = (CSNonGenericType) tv.upperBound()
					.copy();
			tv.setUpperBound(newUpperBound);
		}
		return tv;
	}

	public String getAliasName() {
		return alias;
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

	@Override
	public String fullClassName() {
		return ((CSObjectType) this.upperBound()).fullClassName();
	}
}
