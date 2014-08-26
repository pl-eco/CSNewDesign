package cs.data.constraint;

import cs.linearsolver.ValueHolder;
import cs.visit.AnalysisContext;

public interface Constraint {
	public Constraint refresh(AnalysisContext context);

	public String toString(ValueHolder vh);
}
