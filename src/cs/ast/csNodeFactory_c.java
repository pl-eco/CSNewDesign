package cs.ast;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import polyglot.ast.AmbTypeNode;
import polyglot.ast.AmbTypeNode_c;
import polyglot.ast.ArrayAccess;
import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassLit;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.New;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.QualifierNode;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5Call_c;
import polyglot.ext.jl5.ast.JL5CanonicalTypeNode_c;
import polyglot.ext.jl5.ast.JL5ClassBody_c;
import polyglot.ext.jl5.ast.JL5ClassDecl;
import polyglot.ext.jl5.ast.JL5ClassDecl_c;
import polyglot.ext.jl5.ast.JL5ClassLit_c;
import polyglot.ext.jl5.ast.JL5EnumDecl;
import polyglot.ext.jl5.ast.JL5EnumDecl_c;
import polyglot.ext.jl5.ast.JL5New_c;
import polyglot.ext.jl5.ast.JL5NodeFactory_c;
import polyglot.ext.jl5.ast.JL5Special_c;
import polyglot.ext.jl5.ast.ParamTypeNode;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class csNodeFactory_c extends JL5NodeFactory_c {
	public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier,
			Id name, Map typeValues) {
		AmbTypeNode n = new CSAmbTypeNode_c(pos, qualifier, name, typeValues);
		return n;
	}

	@Override
	public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
			TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
		return new CSClassDecl_c(pos, flags,
				Collections.<AnnotationElem> emptyList(), name, superClass,
				interfaces, body, Collections.<ParamTypeNode> emptyList());
	}

	public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
		ArrayAccess n = new CSArrayAccess_c(pos, base, index);
		return n;
	}

	@Override
	public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
		if (!type.isCanonical()) {
			throw new InternalCompilerError("Cannot construct a canonical "
					+ "type node for a non-canonical type.");
		}

		CanonicalTypeNode n = new CSCanonicalTypeNode_c(pos, type);
		return n;
	}

	@Override
	public ClassLit ClassLit(Position pos, TypeNode typeNode) {
		ClassLit n = new CSClassLit_c(pos, typeNode);
		return n;
	}

	@Override
	public ClassBody ClassBody(Position pos, List<ClassMember> members) {
		ClassBody n = new CSClassBody_c(pos,
				CollectionUtil.nonNullList(members));
		return n;
	}

	@Override
	public Call Call(Position pos, Receiver target, List<TypeNode> typeArgs,
			Id name, List<Expr> args) {
		Call n = new CSCall_c(pos, target,
				CollectionUtil.nonNullList(typeArgs), name,
				CollectionUtil.nonNullList(args));
		// n = (Call) n.ext(extFactory().extCall());
		// n = (Call) n.del(delFactory().delCall());
		return n;
	}

	@Override
	public JL5ClassDecl ClassDecl(Position pos, Flags flags,
			List<AnnotationElem> annotations, Id name, TypeNode superType,
			List<TypeNode> interfaces, ClassBody body,
			List<ParamTypeNode> paramTypes) {
		if (pos == null) {
			pos = body.position();
		}
		JL5ClassDecl n = new JL5ClassDecl_c(pos, flags, annotations, name,
				superType, interfaces, body, paramTypes);
		return n;
	}

	@Override
	public JL5EnumDecl EnumDecl(Position pos, Flags flags,
			List<AnnotationElem> annotations, Id name, TypeNode superType,
			List<TypeNode> interfaces, ClassBody body) {
		JL5EnumDecl n = new CSEnumDecl_c(pos, flags, annotations, name,
				superType, interfaces, body);
		return n;

	}

	@Override
	public New New(Position pos, Expr outer, List<TypeNode> typeArgs,
			TypeNode objectType, List<Expr> args, ClassBody body) {
		New n = new CSNew_c(pos, outer, CollectionUtil.nonNullList(typeArgs),
				objectType, CollectionUtil.nonNullList(args), body);
		return n;
	}

	@Override
	public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
		Special n = new CSSpecial_c(pos, kind, outer);
		return n;
	}
}
