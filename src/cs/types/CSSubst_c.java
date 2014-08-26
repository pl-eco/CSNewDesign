package cs.types;

import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5SubstClassType_c;
import polyglot.ext.jl5.types.JL5Subst_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.param.Topics;
import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.main.Report;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;

public class CSSubst_c extends JL5Subst_c {

	public CSSubst_c(ParamTypeSystem<TypeVariable, ReferenceType> ts,
			Map<TypeVariable, ? extends ReferenceType> subst) {
		super(ts, subst);
	}

	// /** Perform substitutions on a type. */
	// @Override
	// public Type substType(Type t) {
	// if (t == null || t == this) // XXX comparison t == this can't succeed!
	// // (Findbugs)
	// return t;
	//
	// Type cached = uncachedSubstType(t);
	//
	// // if (Report.should_report(Topics.subst, 2))
	// // Report.report(2, "substType(" + t + ": " + t.getClass().getName()
	// // + ") = " + cached + ": " + cached.getClass().getName());
	//
	// return cached;
	// }

	@Override
	public ClassType substClassType(ClassType t) {
		// Don't bother trying to substitute into a non-JL5 class.
		if (!(t instanceof JL5ClassType)) {
			return t;
		}

		if (t instanceof RawClass) {
			// don't substitute raw classes
			return t;
		}
		if (t instanceof JL5SubstClassType) {
			// this case should be impossible
			throw new InternalCompilerError("Should have no JL5SubstClassTypes");
		}

		if (t instanceof JL5ParsedClassType) {
			JL5ParsedClassType pct = (JL5ParsedClassType) t;
			JL5TypeSystem ts = (JL5TypeSystem) this.ts;
			List<TypeVariable> typeVars = ts
					.classAndEnclosingTypeVariables(pct);
			// are the type variables of pct actually relevant to this subst? If
			// not, then return the pct.
			boolean typeVarsRelevant = false;
			for (TypeVariable tv : typeVars) {
				if (this.substitutions().containsKey(tv)) {
					typeVarsRelevant = true;
					break;
				}
			}
			if (!typeVarsRelevant) {
				// no parameters to be instantiated!
				return pct;
			}

			return new CSSubstClassType_c(ts, t.position(), pct, this);
		}

		throw new InternalCompilerError("Don't know how to handle class type "
				+ t.getClass());

	}

}
