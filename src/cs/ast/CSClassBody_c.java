package cs.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassMember;
import polyglot.ext.jl5.ast.JL5ClassBody_c;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class CSClassBody_c extends JL5ClassBody_c {

	public CSClassBody_c(Position pos, List<ClassMember> members) {
		super(pos, members);
	}

	/*
	 * Ignore the duplicate method check since it cause problem in checking
	 * library code
	 */
	protected void duplicateMethodCheck(TypeChecker tc)
			throws SemanticException {
		ClassType type = tc.context().currentClass();
		TypeSystem ts = tc.typeSystem();

		ArrayList<MethodInstance> l = new ArrayList<MethodInstance>(
				type.methods());

		for (int i = 0; i < l.size(); i++) {
			MethodInstance mi = l.get(i);

			for (int j = i + 1; j < l.size(); j++) {
				MethodInstance mj = l.get(j);

				if (isSameMethod(ts, mi, mj)) {
					// throw new SemanticException("Duplicate method \"" + mj
					// + "\".", mj.position());
				}
			}
		}
	}
}
