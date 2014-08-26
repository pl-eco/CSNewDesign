package cs.types;

import java.util.List;

import cs.data.id.ID;
import cs.data.id.VariableGenerator;

import polyglot.ext.jl5.types.IntersectionType_c;
import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.ReferenceType;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class CSIntersectionType_c extends IntersectionType_c implements
		CSObjectType {

	public CSIntersectionType_c(TypeSystem ts, Position pos,
			List<ReferenceType> bounds) {
		super(ts, pos, bounds);
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
		return ((CSObjectType) this.bounds().get(0)).fullClassName();
	}

}
