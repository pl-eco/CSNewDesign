package cs.types;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5SubstClassType_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass_c;
import polyglot.util.Position;
import cs.data.id.ID;
import cs.data.id.VariableGenerator;

public class CSRawClass_c extends RawClass_c implements CSObjectType {
	public CSRawClass_c(JL5ParsedClassType t, Position pos) {
		super(t, pos);
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

	//
	@Override
	public JL5SubstClassType erased() {
		if (this.erased == null) {

			JL5TypeSystem ts = (JL5TypeSystem) this.ts;
			JL5Subst es = ts.erasureSubst(this.base);
			this.erased = new CSSubstClassType_c(ts, base.position(), base, es);
		}
		return this.erased;
	}

	@Override
	public String fullClassName() {
		if (base() instanceof CSParsedClassType_c) {
			return base().fullName();
		}
		return ((CSObjectType) base).fullClassName();
	}

}
