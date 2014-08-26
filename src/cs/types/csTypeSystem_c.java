package cs.types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cs.ComponentFactory;
import cs.graph.CSGraph;
import cs.graph.CSSimpleGraph;
import cs.util.CSUtil;
import cs.util.EasyDebugger;
import cs.visit.AnalysisContext;
import cs.types.CSTypeVariable;
import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.AnnotationElementValueConstant;
import polyglot.ext.jl5.types.IntersectionType_c;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5ArrayType_c;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5ConstructorInstance_c;
import polyglot.ext.jl5.types.JL5Context_c;
import polyglot.ext.jl5.types.JL5FieldInstance;
import polyglot.ext.jl5.types.JL5FieldInstance_c;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5MethodInstance_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem_c;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.RawClass_c;
import polyglot.ext.jl5.types.RetainedAnnotations;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.TypeVariable_c;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.ext.jl5.types.WildCardType_c;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.jl5.types.inference.LubType_c;
import polyglot.ext.param.types.Subst;
import polyglot.frontend.Source;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.NullType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem_c;
import polyglot.util.Position;

public abstract class csTypeSystem_c extends JL5TypeSystem_c implements
		ComponentFactory {
	static {
		// add for jdk7
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	public csTypeSystem_c() {
		// set the ValueHolder
		AnalysisContext.setValueHolder(getValueHolder());
	}

	public CSGraph getGraph() {
		return new CSSimpleGraph(this);
	}

	@Override
	public RawClass rawClass(JL5ParsedClassType base, Position pos) {
		return new CSRawClass_c(base, pos);
	}

	@Override
	public TypeVariable typeVariable(Position pos, String name,
			ReferenceType upperBound) {
		// System.err.println("JL5TS_c typevar created " + name + " " + bounds);
		return new CSTypeVariable(this, pos, name, upperBound);
	}

	@Override
	// override this method to avoid cached arrayType
	protected ArrayType arrayType(Position pos, Type type) {
		ArrayType t = null; // arrayTypeCache.get(type);
		if (t == null) {
			t = createArrayType(pos, type);
			// arrayTypeCache.put(type, t);
		}
		return t;
	}

	@Override
	public LubType lub(Position pos, List<ReferenceType> us) {
		return new CSLubType_c(this, pos, us);
	}

	@Override
	public ReferenceType intersectionType(Position pos,
			List<ReferenceType> types) {
		if (types.size() == 1) {
			return types.get(0);
		}
		if (types.isEmpty()) {
			return Object();
		}

		return new CSIntersectionType_c(this, pos, types);
	}

	@Override
	public JL5Subst erasureSubst(JL5ProcedureInstance pi) {
		List<TypeVariable> typeParams = pi.typeParams();
		Map<TypeVariable, ReferenceType> m = new LinkedHashMap<TypeVariable, ReferenceType>();
		for (TypeVariable tv : typeParams) {
			m.put(tv, tv.erasureType());
		}
		if (m.isEmpty()) {
			return null;
		}
		return new CSSubst_c(this, m);
	}

	@Override
	public JL5MethodInstance methodInstance(Position pos,
			ReferenceType container, Flags flags, Type returnType, String name,
			List<? extends Type> argTypes, List<? extends Type> excTypes,
			List<TypeVariable> typeParams) {
		// wrap args
		List<? extends Type> args = CSUtil.wrapArguments(argTypes, this);

		// wrap return
		if (returnType instanceof CSParsedClassType_c) {
			returnType = this.createDefaultObjectType(returnType);
		}

		return super.methodInstance(pos, container, flags, returnType, name,
				args, excTypes, typeParams);
	}

	@Override
	public JL5ConstructorInstance constructorInstance(Position pos,
			ClassType container, Flags flags, List<? extends Type> argTypes,
			List<? extends Type> excTypes, List<TypeVariable> typeParams) {
		// wrap args
		List<? extends Type> args = CSUtil.wrapArguments(argTypes, this);

		return super.constructorInstance(pos, container, flags, args, excTypes,
				typeParams);
	}

	@Override
	public JL5FieldInstance fieldInstance(Position pos,
			ReferenceType container, Flags flags, Type type, String name) {
		// this is necessary
		if (type instanceof CSParsedClassType_c) {
			type = this.createDefaultObjectType(type);
		}

		return super.fieldInstance(pos, container, flags, type, name);
	}

	@Override
	public Subst<TypeVariable, ReferenceType> subst(
			Map<TypeVariable, ? extends ReferenceType> substMap) {
		return new CSSubst_c(this, substMap);
	}

	/**
	 * Given an annotation of type annotationType, should the annotation be
	 * retained in the binary? See JLS 3rd ed, 9.6.1.2
	 */
	/*
	 * Override this method because the AnnotationElementValueConstant condition
	 * is not implemented by JL5. Just add a simple logic here...
	 */
	protected boolean retainAnnotation(Type annotationType) {
		if (annotationType.isClass()
				&& annotationType.toClass().isSubtype(this.Annotation())) {
			// well, it's an annotation type at least.
			// check if there is a retention policy on it.
			JL5ClassType ct = (JL5ClassType) annotationType.toClass();
			RetainedAnnotations ra = ct.retainedAnnotations();
			if (ra == null) {
				// by default, use RetentionPolicy.CLASS
				return true;
			}
			AnnotationElementValue v = ra.singleElement(RetentionAnnotation());
			if (v == null) {
				// by default, use RetentionPolicy.CLASS
				return true;
			}
			if (v instanceof AnnotationElementValueConstant) {
				// XXX missing logic here
				// System.err.println("What do we do with " + v);
				// throw new UnsupportedOperationException("To implement...");
				return false;
			}
			return true;
		}
		return false;
	}

	// ---------------------------------------------------
	// it is a must to have CSParsedClassType, otherwise, the CSNonGenericType
	// can not be differanticated from
	@Override
	public CSParsedClassType_c createClassType(LazyClassInitializer init,
			Source fromSource) {
		return new CSParsedClassType_c(this, init, fromSource);
	}

	@Override
	public Context createContext() {
		return new CSContext_c(this);
	}

	protected NullType createNull() {
		return new CSNullType(this);
	}

	@Override
	public WildCardType wildCardType(Position position,
			ReferenceType upperBound, ReferenceType lowerBound) {
		if (upperBound == null) {
			upperBound = this.Object();
		}
		return new CSWildCardType_c(this, position, upperBound, lowerBound);
	}

	public static String primitiveNameConversion(String primitive) {
		String ret = null;
		if (primitive.equals("boolean")) {
			ret = "java.lang.Boolean";
		} else if (primitive.equals("byte")) {
			ret = "java.lang.Bype";
		} else if (primitive.equals("short")) {
			ret = "java.lang.Short";
		} else if (primitive.equals("int")) {
			ret = "java.lang.Integer";
		} else if (primitive.equals("long")) {
			ret = "java.lang.Long";
		} else if (primitive.equals("float")) {
			ret = "java.lang.Float";
		} else if (primitive.equals("double")) {
			ret = "java.lang.Double";
		} else if (primitive.equals("char")) {
			ret = "java.lang.Character";
		}
//		if(ret == null){
//			return primitive;
//		}else{
			return ret;
//		}
	}
	
	public static String isPrimitiveType(String primitive) {
		String ret = null;
		if (primitive.equals("boolean")) {
			ret = "java.lang.Boolean";
		} else if (primitive.equals("byte")) {
			ret = "java.lang.Bype";
		} else if (primitive.equals("short")) {
			ret = "java.lang.Short";
		} else if (primitive.equals("int")) {
			ret = "java.lang.Integer";
		} else if (primitive.equals("long")) {
			ret = "java.lang.Long";
		} else if (primitive.equals("float")) {
			ret = "java.lang.Float";
		} else if (primitive.equals("double")) {
			ret = "java.lang.Double";
		} else if (primitive.equals("char")) {
			ret = "java.lang.Character";
		}
		if(ret == null){
			return primitive;
		}else{
			return ret;
		}
	}

	// used to convert a string_lieral to MyClassType
	@Override
	public ClassType String() {
		if (STRING_ == null) {
			STRING_ = load("java.lang.String");
		}
		return this.createDefaultObjectType(STRING_);
	}

	@Override
	public ClassType Object() {
		if (OBJECT_ == null) {
			OBJECT_ = load("java.lang.Object");
		}
		return this.createDefaultObjectType(OBJECT_);
	}

	protected PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
		return new CSPrimitiveType(this, kind);
	}

	public final boolean equals(TypeObject type1, TypeObject type2) {
		if (type1 instanceof CSNonGenericType
				&& type2 instanceof CSNonGenericType) {
			if (!((CSNonGenericType) type1).getTypeLabel().equals(
					((CSNonGenericType) type2).getTypeLabel()))
				return false;
		}
		type1 = getInnerType(type1);
		type2 = getInnerType(type2);
		return super.equals(type1, type2);
	}

	public final boolean typeEquals(Type type1, Type type2) {
		if (type1 instanceof CSNonGenericType
				&& type2 instanceof CSNonGenericType) {
			if (!((CSNonGenericType) type1).getTypeLabel().equals(
					((CSNonGenericType) type2).getTypeLabel()))
				return false;
		}

		type1 = (Type) getInnerType(type1);// ((CSNonGenericType)
											// type1).getType();
		type2 = (Type) getInnerType(type2); // ((CSNonGenericType)
											// type2).getType();
		// }

		return super.typeEquals(type1, type2);
	}

	@Override
	public boolean isSubtype(Type type1, Type type2) {
		// if (type1 instanceof CSNonGenericType
		// && type2 instanceof CSNonGenericType) {
		type1 = (Type) getInnerType(type1);// ((CSNonGenericType)
											// type1).getType();
		type2 = (Type) getInnerType(type2); // ((CSNonGenericType)
											// type2).getType();
		// }
		return super.isSubtype(type1, type2);
	}

	@Override
	public boolean isImplicitCastValid(Type fromType, Type toType) {
		if (fromType instanceof CSNonGenericType
				&& toType instanceof CSNonGenericType) {
			if (!((CSNonGenericType) fromType).getTypeLabel().equals(
					((CSNonGenericType) toType).getTypeLabel()))
				return false;
		}

		fromType = (Type) getInnerType(fromType); // ((CSNonGenericType)
		// fromType).getType();
		toType = (Type) getInnerType(toType);// ((CSNonGenericType)
												// toType).getType();
		// }
		return super.isImplicitCastValid(fromType, toType);
	}

	@Override
	public boolean isCastValid(Type fromType, Type toType) {
		// if (fromType instanceof CSNonGenericType
		// && toType instanceof CSNonGenericType) {
		fromType = (Type) getInnerType(fromType); // ((CSNonGenericType)
		// fromType).getType();
		toType = (Type) getInnerType(toType);// ((CSNonGenericType)
												// toType).getType();
		// }
		return super.isCastValid(fromType, toType);
	}

	private TypeObject getInnerType(TypeObject type) {
		if (type instanceof CSNonGenericType)
			return ((CSNonGenericType) type).getType();
		return (TypeObject) type;
	}

	protected ArrayType createArrayType(Position pos, Type type) {
		return new CSArrayType(this, pos, type, false);
	}

	protected ArrayType createArrayType(Position pos, Type type,
			boolean isVarargs) {
		return new CSArrayType(this, pos, type, isVarargs);
	}

	// /*
	// * used for array typeCheck
	// */
	// @Override
	// public boolean isReifiable(Type t) {
	// // if (t instanceof CSNonGenericType)
	// // return super.isReifiable(((CSNonGenericType) t).getType());
	//
	// return super.isReifiable(t);
	// }

	/*
	 * Ignore part of the conformance check
	 */
	@Override
	public void checkClassConformance(ClassType ct) throws SemanticException {
		if (ct.flags().isAbstract()) {
			// don't need to check interfaces or abstract classes
			return;
		}

		// build up a list of superclasses and interfaces that ct
		// extends/implements that may contain abstract methods that
		// ct must define.
		List<ReferenceType> superInterfaces = abstractSuperInterfaces(ct);

		// check each abstract method of the classes and interfaces in
		// superInterfaces
		for (ReferenceType rt : superInterfaces) {
			for (MethodInstance mi : rt.methods()) {
				if (!mi.flags().isAbstract()) {
					// the method isn't abstract, so ct doesn't have to
					// implement it.
					continue;
				}

				MethodInstance mj = findImplementingMethod(ct, mi);
				if (mj == null) {
					if (!ct.flags().isAbstract()) {
						// throw new SemanticException(ct.fullName()
						// + " should be "
						// + "declared abstract; it does not define "
						// + mi.signature() + ", which is declared in "
						// + rt.toClass().fullName(), ct.position());
					} else {
						// no implementation, but that's ok, the class is
						// abstract.
					}
				} else if (!equals(ct, mj.container())
						&& !equals(ct, mi.container())) {
					try {
						// check that mj can override mi, which
						// includes access protection checks.
						checkOverride(mj, mi);
					} catch (SemanticException e) {
						// change the position of the semantic
						// exception to be the class that we
						// are checking.
						throw new SemanticException(e.getMessage(),
								ct.position());
					}
				} else {
					// the method implementation mj or mi was
					// declared in ct. So other checks will take
					// care of access issues
				}
			}
		}
	}
}
