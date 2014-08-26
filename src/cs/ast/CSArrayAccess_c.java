package cs.ast;

import cs.types.CSNonGenericType;
import cs.types.CSNonGenericType_c;
import cs.types.CSParsedClassType_c;
import cs.types.csTypeSystem_c;
import polyglot.ast.ArrayAccess_c;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.types.ArrayType;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import cs.data.id.VariableGenerator;
import cs.types.CSBaseType;

public class CSArrayAccess_c extends ArrayAccess_c {

	public CSArrayAccess_c(Position pos, Expr array, Expr index) {
		super(pos, array, index);
	}

	/*
	 * this method make all of the ArrayAccess' element use the same aliasName
	 * 
	 * Meanwhile, it convert element of ParsedClassType to CSObjectType
	 */
	public Expr type(Type type) {
		csTypeSystem_c ts = ((csTypeSystem_c) type.typeSystem());

		if (type instanceof CSParsedClassType_c) {
			CSBaseType target = (CSBaseType) array.type();

			String elementName = VariableGenerator.getArrayElementVar(target
					.getAliasName());

			CSNonGenericType temp = ts.createDefaultObjectType(elementName,
					type);

			return super.type(temp);
		} else if (type instanceof ArrayType) {
			Type base = ((ArrayType) type).base();
			if (((ArrayType) type).base() instanceof CSParsedClassType_c) {
				CSBaseType target = (CSBaseType) array.type();

				String elementName = VariableGenerator
						.getArrayElementVar(target.getAliasName());

				CSNonGenericType temp = ts.createDefaultObjectType(elementName,
						base);

				return super.type(((ArrayType) type).base(temp));
			} else
				return super.type(type);
		} else
			return super.type(type);
	}
}
