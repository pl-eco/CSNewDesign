package cs.ast;

import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5EnumDecl_c;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public class CSEnumDecl_c extends JL5EnumDecl_c {

	public CSEnumDecl_c(Position pos, Flags flags,
			List<AnnotationElem> annotations, Id name, TypeNode superClass,
			List<TypeNode> interfaces, ClassBody body) {
		super(pos, flags, annotations, name, superClass, interfaces, body);
	}

	// @Override
	// /*
	// * This override just avoid the setting of abstract flag on enum. This
	// might
	// * be removed later
	// */
	// public NodeVisitor typeCheckEnter(TypeChecker tc) throws
	// SemanticException {
	// // figure out if this should be an abstract type.
	// // need to do this before any anonymous subclasses are typechecked.
	// for (MethodInstance mi : type().methods()) {
	// if (!mi.flags().isAbstract())
	// continue;
	//
	// // mi is abstract! First, mark the class as abstract.
	// // type().setFlags(type().flags().Abstract());
	//
	// mi.setFlags(mi.flags().clearAbstract());
	// }
	// return super.typeCheckEnter(tc);
	// }

}
