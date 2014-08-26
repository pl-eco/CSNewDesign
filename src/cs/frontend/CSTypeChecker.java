package cs.frontend;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ext.jl5.ast.JL5Special_c;
import polyglot.ext.jl5.types.RawClass;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.TypeChecker;

public class CSTypeChecker extends TypeChecker {

	public CSTypeChecker(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	public <N extends Node> N visitEdgeNoOverride(Node parent, N child) {
		if (child == null) {
			return null;
		}

		N ret = null;
		if (child instanceof JL5Special_c) {
			/*
			 * The above code is moved from JL5Special. The original JL5Special
			 * will be bypassed
			 */
			Special s = (Special) child.visitChildren(this);
			if (s.qualifier() != null
					&& s.qualifier().type() instanceof RawClass) {
				// we got a raw class. Fix it up
				RawClass rc = (RawClass) s.qualifier().type();
				s = s.qualifier(s.qualifier().type(rc.base()));
			}
			try {
				ret = (N) s.typeCheck(this);
			} catch (SemanticException e) {
				e.printStackTrace();
			}
			/*
			 * End of JL5Special's code
			 */
		} else {
			ret = super.visitEdgeNoOverride(parent, child);
		}

		return ret;

	}
}
