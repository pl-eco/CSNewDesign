package cs;

import cs.frontend.CSTypeChecked;
import polyglot.ast.NodeFactory;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.frontend.goals.Goal;
import polyglot.types.TypeSystem;

/*
 * This class is used to create a CSTypeChecked instance, instead of the original TypeChecked instance
 */
public class CSScheduler extends JLScheduler {

	public CSScheduler(ExtensionInfo extInfo) {
		super(extInfo);
	}

	@Override
	public Goal TypeChecked(Job job) {
		TypeSystem ts = extInfo.typeSystem();
		NodeFactory nf = extInfo.nodeFactory();
		Goal g = this.internGoal(new CSTypeChecked(job, ts, nf));
		// TypeChecked.create(this, job, ts, nf);
		return g;
	}

}
