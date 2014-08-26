package cs.types;

import java.util.List;

import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import polyglot.ext.jl5.types.inference.LubType_c;
import polyglot.types.ReferenceType;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class CSLubType_c extends LubType_c implements CSObjectType {

	public CSLubType_c(TypeSystem ts, Position pos, List<ReferenceType> lubElems) {
		super(ts, pos, lubElems);
		aliasName = VariableGenerator.nextAlias();
	}

	private String aliasName;
	private ID id = null;

	@Override
	public void setID(ID id) {
		assert id != null;
		this.id = id;
	}

	public ID getID() {
		assert id != null;
		return this.id;
	}

	@Override
	public String getAliasName() {
		return aliasName;
	}

	@Override
	public void setAliasName(String name) {
		assert aliasName == null;
		aliasName = name;
	}

	@Override
	public String fullClassName() {
		return this.fullName();
	}

}
