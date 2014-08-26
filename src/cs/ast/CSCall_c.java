package cs.ast;

import java.util.List;

import cs.types.CSNonGenericType;
import cs.types.CSParsedClassType_c;
import cs.types.csTypeSystem_c;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5Call_c;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class CSCall_c extends JL5Call_c {

	public CSCall_c(Position pos, Receiver target, List<TypeNode> typeArgs,
			Id name, List<Expr> arguments) {
		super(pos, target, typeArgs, name, arguments);
	}

	@Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Call call = (Call) super.typeCheck(tc);

		if (call.type() instanceof CSParsedClassType_c) {
			CSNonGenericType type = ((csTypeSystem_c) this.type().typeSystem())
					.createDefaultObjectType(call.type());
			call = (Call) call.type(type);
		}

		return call;
	}
}
