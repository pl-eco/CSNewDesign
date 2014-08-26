package cs.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType_c;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import cs.util.CSUtil;

public class CSParsedClassType_c extends JL5ParsedClassType_c {
	private ID id;

	public CSParsedClassType_c(TypeSystem ts, LazyClassInitializer init,
			Source fromSource) {
		super(ts, init, fromSource);
	}

	public void setID(ID id) {
		assert id != null;
		this.id = id;
	}

	// the return value may be null
	public ID getID() {
		return this.id;
	}

	@Override
	public void addMethod(MethodInstance mi) {
		// if (mi.returnType() instanceof TypeVariable) {
		// Type type = ((JL5TypeSystem) this.typeSystem()).erasureType(mi
		// .returnType());
		// mi = mi.returnType(type);
		// this.addMethod(mi);
		// }

		if (mi.returnType() instanceof CSParsedClassType_c) {
			CSNonGenericType retType = ((csTypeSystem_c) typeSystem())
					.createDefaultObjectType(mi.returnType());
			mi = mi.returnType(retType);
		}
		// List formalList = new ArrayList();
		// for (Iterator iter = mi.formalTypes().iterator(); iter.hasNext();) {
		// Object element = (Object) iter.next();
		// Object arg = null;
		// if (element instanceof CSParsedClassType_c) {
		// arg = ((csTypeSystem_c) typeSystem())
		// .createDefaultObjectType((ClassType) element);
		// } else {
		// arg = element;
		// }
		// formalList.add(arg);
		// }
		//

		List args = CSUtil.wrapArguments(mi.formalTypes(),
				(csTypeSystem_c) typeSystem());
		mi = mi.formalTypes(args);

		methods.add(mi);
	}

	@Override
	public void addConstructor(ConstructorInstance ci) {
		// List formalList = new ArrayList();
		// for (Iterator iter = ci.formalTypes().iterator(); iter.hasNext();) {
		// Object element = (Object) iter.next();
		// Object arg = null;
		// if (element instanceof CSParsedClassType_c) {
		// arg = ((csTypeSystem_c) typeSystem())
		// .createDefaultObjectType((ClassType) element);
		// } else {
		// arg = element;
		// }
		// formalList.add(arg);
		// }
		//
		// ci = ci.formalTypes(formalList);

		List args = CSUtil.wrapArguments(ci.formalTypes(),
				(csTypeSystem_c) typeSystem());
		ci = ci.formalTypes(args);
		constructors.add(ci);
	}

	@Override
	public void addField(FieldInstance fi) {
		if (fi.type() instanceof CSParsedClassType_c) {
			CSNonGenericType ecmType = ((csTypeSystem_c) typeSystem())
					.createDefaultObjectType(fi.type());
			fi = fi.type(ecmType);
		}
		fields.add(fi);
	}
}
