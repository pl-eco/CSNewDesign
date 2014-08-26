package cs.ast;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5ClassLit_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class CSClassLit_c extends JL5ClassLit_c {

	public CSClassLit_c(Position pos, TypeNode typeNode) {
		super(pos, typeNode);
	}

	@Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
		if (typeNode.type() instanceof PrimitiveType) {
			typeNode = typeNode().type(
					ts.wrapperClassOfPrimitive((PrimitiveType) typeNode()
							.type()));
		}
		return type(ts
				.Class(this.position(), (ReferenceType) typeNode().type()));
	}
}
