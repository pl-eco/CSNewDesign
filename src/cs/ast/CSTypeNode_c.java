//package cs.ast;
//
//import java.util.List;
//
//import polyglot.ast.*;
//import polyglot.ext.jl5.ast.ParamTypeNode_c;
//import polyglot.types.*;
//import polyglot.visit.*;
//import polyglot.util.*;
//
//import cs.data.id.VariableGenerator;
//import cs.types.CSObjectType;
//import cs.types.csTypeSystem_c;
//
///**
// * NodeFactory for et extension.
// */
//
//public class CSTypeNode_c extends ParamTypeNode_c {
//	private String alias;
//
//	// private TypeNode node;
//
//	public CSTypeNode_c(Position pos, List<TypeNode> bounds, Id id) {
//		super(pos, bounds, id);
//		this.alias = VariableGenerator.nextAlias();
//	}
//
//	public String getAliasName() {
//		return alias;
//	}
//
//	// public boolean isDisambiguated() {
//	// // Add by Steve 6/10/11
//	// return super.isDisambiguated() && type != null
//	// && node.type().isCanonical();
//	// // End Steve
//	// }
//	//
//	protected CSTypeNode_c reconstruct(TypeNode node) {
//		if (node != this) {
//			CSTypeNode_c n = (CSTypeNode_c) copy();
//			n.alias = alias;
//			return n;
//		}
//		return this;
//	}
//
//	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
//		// node.prettyPrint(w, tr);
//		w.write(type().toString());
//		// Block_c d = (Block_c)node;
//		// if (qual != null) {
//		// print(qual, w, tr);
//		// w.write(".");
//		// }
//		//
//		// w.write(name);
//	}
//
//	public Node buildTypes(TypeBuilder tb) throws SemanticException {
//		TypeNode node = (TypeNode) super.buildTypes(tb);
//
//		return type(((csTypeSystem_c) tb.typeSystem()).createDefaultObjectType(
//				alias, tb.typeSystem().unknownType(node.position())));
//	}
//
//	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
//		((CSObjectType) this.type()).setType(this.node.type());
//		return this.type(this.type());
//	}
//
//	public Node visitChildren(NodeVisitor v) {
//		TypeNode node = (TypeNode) visitChild(this.node, v);
//
//		return reconstruct(node);
//	}
//
// }