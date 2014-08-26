package cs.types;

import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import polyglot.ext.jl5.types.WildCardType_c;
import polyglot.types.ReferenceType;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class CSWildCardType_c extends WildCardType_c implements CSBaseType {
	public CSWildCardType_c(TypeSystem ts, Position position,
			ReferenceType upperBound, ReferenceType lowerBound) {
		super(ts, position, upperBound, lowerBound);
	}

	private String alias = VariableGenerator.nextAlias();
	private ID id;

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
}
