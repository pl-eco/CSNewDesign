package cs.ast;

import cs.types.CSNonGenericType;
import cs.types.CSParsedClassType_c;
import cs.types.csTypeSystem_c;
import cs.types.CSContext_c;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5Special_c;
import polyglot.ext.jl5.types.RawClass;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

/*
 * This class acts as a place holder to by pass JL5Special_c's method. The reason to have this class instead of directly using Special_c is to avoid any use of "instanceof JL5Special" case
 * 
 * The "this" won't be parsed if jl5Special_c's method return non-null value.
 */
public class CSSpecial_c extends JL5Special_c {

	public CSSpecial_c(Position pos, Kind kind, TypeNode qualifier) {
		super(pos, kind, qualifier);
	}

	@Override
	public Node typeCheckOverride(Node parent, TypeChecker tc)
			throws SemanticException {
		return null; // super.typeCheckOverride(parent, tc);
	}

	@Override
	/*
	 * wrap the parsedClassType with NonGenericType
	 */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Special sp = (Special) super.typeCheck(tc);

		if (sp.type() instanceof CSParsedClassType_c) {
			
			String currentThisAlias = ((CSContext_c) tc.context()).currentThisAlias;
			
			CSNonGenericType wrapper = ((csTypeSystem_c) tc.typeSystem())
					.createDefaultObjectType(currentThisAlias, sp.type());
			sp = (Special) sp.type(wrapper);
		}

		return sp;
	}
}
