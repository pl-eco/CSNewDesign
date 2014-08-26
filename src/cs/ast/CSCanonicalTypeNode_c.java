package cs.ast;

import cs.types.CSNonGenericType;
import cs.types.CSNonGenericType_c;
import cs.types.CSNonGenericWrapper;
import cs.types.CSParsedClassType_c;
import cs.util.EasyDebugger;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.ast.JL5CanonicalTypeNode_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ParsedClassType;
import polyglot.types.ParsedClassType_c;
import polyglot.types.Type;
import polyglot.util.Position;

public class CSCanonicalTypeNode_c extends JL5CanonicalTypeNode_c {

	public CSCanonicalTypeNode_c(Position pos, Type type) {
		super(pos, type);

		if (this.type instanceof CSParsedClassType_c) {
			// this.type = new CSNonGenericType_c((CSParsedClassType_c) type);
			this.type = CSNonGenericWrapper
					.createNonGenericType((JL5ParsedClassType_c) type);
		}
	}

	@Override
	public TypeNode type(Type type) {
		if (type instanceof CSParsedClassType_c) {
			type = CSNonGenericWrapper
					.createNonGenericType((JL5ParsedClassType_c) type);
		}
		return super.type(type);
	}

}
