package cs.ast;

import java.util.ArrayList;
import java.util.List;

import cs.types.csTypeSystem_c;

import polyglot.ast.ClassBody;
import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5New_c;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class CSNew_c extends JL5New_c {
	public CSNew_c(Position pos, Expr outer, List<TypeNode> typeArgs,
			TypeNode objectType, List<Expr> args, ClassBody body) {
		super(pos, outer, typeArgs, objectType, args, body);
	}

	@Override
	/*
	 * have the anonymous class return NonGenericType instead of ParsedClassType
	 */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		CSNew_c n = (CSNew_c) super.typeCheck(tc);

		if (n.type() == anonType) {
			n = (CSNew_c) n.type(((csTypeSystem_c) tc.typeSystem())
					.createDefaultObjectType(anonType));
		}
		return n;
	}
}
