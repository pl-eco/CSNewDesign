package cs.types;

import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5SubstClassType_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ClassType_c;
import polyglot.util.Position;

public class CSSubstClassType_c extends JL5SubstClassType_c implements
		CSObjectType {

	public CSSubstClassType_c(JL5TypeSystem ts, Position pos,
			JL5ParsedClassType base, JL5Subst subst) {
		super(ts, pos, base, subst);
		aliasName = VariableGenerator.nextAlias();
	}

	private String aliasName;
	private ID id = null;

	@Override
	public void setID(ID id) {
		this.id = id;
		assert this.id != null;
	}

	public ID getID() {
		assert this.id != null;
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
		if (base() instanceof CSParsedClassType_c) {
			return base().fullName();
		}
		return ((CSObjectType) base()).fullClassName();
	}

}
