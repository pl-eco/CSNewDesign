package cs.types;

import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Context_c;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.Context_c;
import polyglot.types.TypeSystem;

public class CSContext_c extends JL5Context_c {

	public String currentThisAlias = ""; //WHY

	public CSContext_c(TypeSystem ts) {
		super(ts);
	}
	
	@Override
    protected Context_c push() {
        CSContext_c c = (CSContext_c) super.push();
        c.currentThisAlias = this.currentThisAlias;
        return c;
    }

	@Override
	public TypeVariable findTypeVariableInThisScope(String name) {
		if (typeVariable != null && typeVariable.name().equals(name))
			return typeVariable;
		if (typeVars != null && typeVars.containsKey(name)) {
			// duplicate the typeVariable and return
			CSTypeVariable tv = ((CSTypeVariable) typeVars.get(name))
					.duplicate();
			return tv;
		}
		if (outer != null) {
			return ((JL5Context) outer).findTypeVariableInThisScope(name);
		}
		return null;
	}

}
