package cs.ast;

import java.util.List;

import cs.data.id.VariableGenerator;
import cs.types.CSContext_c;
import polyglot.ast.ClassBody;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5ClassDecl_c;
import polyglot.ext.jl5.ast.ParamTypeNode;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.util.Position;

public class CSClassDecl_c extends JL5ClassDecl_c {

	public CSClassDecl_c(Position pos, Flags fl,
			List<AnnotationElem> annotations, Id name, TypeNode superType,
			List<TypeNode> interfaces, ClassBody body,
			List<ParamTypeNode> paramTypes) {
		super(pos, fl, annotations, name, superType, interfaces, body, paramTypes);
	}

	private String fixedThisAlias = VariableGenerator.nextAlias();
	
	public Context enterChildScope(Node child, Context c) {
		CSContext_c csContext = (CSContext_c) super
				.enterChildScope(child, c);
		csContext.currentThisAlias = fixedThisAlias;

		return csContext;
	}
}
