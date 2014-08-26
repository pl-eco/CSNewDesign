package cs.frontend;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.goals.TypeChecked;
import polyglot.types.TypeSystem;

public class CSTypeChecked extends TypeChecked {
	public CSTypeChecked(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
		this.v = new CSTypeChecker(job, ts, nf);
	}

}
