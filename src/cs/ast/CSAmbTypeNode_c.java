package cs.ast;

import java.util.Map;

import cs.linearsolver.CSTYPE;
import cs.linearsolver.ValueHolder;
import cs.types.CSBaseType;
import cs.types.csTypeSystem_c;
import polyglot.ast.AmbTypeNode_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.QualifierNode;
import polyglot.ast.TypeNode;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;

public class CSAmbTypeNode_c extends AmbTypeNode_c {
	private Map<CSTYPE, Object> typeValues;

	public CSAmbTypeNode_c(Position pos, QualifierNode qual, Id name) {
		this(pos, qual, name, null);
	}

	public CSAmbTypeNode_c(Position pos, QualifierNode qual, Id name,
			Map<CSTYPE, Object> typeValues) {
		super(pos, qual, name);
		this.typeValues = typeValues;
	}

	@Override
	public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
		if (qual != null && !qual.isDisambiguated()) {
			return this;
		}

		Node n = sc.nodeFactory().disamb()
				.disambiguate(this, sc, position(), qual, name);

		if (n instanceof CSCanonicalTypeNode_c) {
			// associate aliasName with declared value
			CSBaseType csType = (CSBaseType) ((CSCanonicalTypeNode_c) n).type();
			
			ValueHolder vh = ((csTypeSystem_c)sc.typeSystem()).getValueHolder();
			vh.bindTypeValueToAlias(csType.getAliasName(), typeValues);
		}

		if (n instanceof TypeNode) {
			return n;
		}

		throw new SemanticException("Could not find type \""
				+ (qual == null ? name.toString() : qual.toString() + "."
						+ name.toString()) + "\".", position());
	}

}
