package cs.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.RetainedAnnotations;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.param.types.PClass;
import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.ConstructorInstance;
import polyglot.types.Declaration;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LazyInitializer;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.NullType;
import polyglot.types.Package;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeObject_c;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import cs.data.id.ID;
import cs.data.id.VariableGenerator;
import cs.util.EasyDebugger;

public class CSNonGenericType_c implements CSNonGenericType {
	private String aliasName;
	private ID id = null;

	private Type inner_type;

	private String typeLabel; // the default value is "default"

	public CSNonGenericType_c(String aliasName, Type type) {
		this(aliasName, type, "default");
	}

	public CSNonGenericType_c(String aliasName, Type type, String typeLabel) {
		// super(type.typeSystem(), type.init(), type.fromSource());
		this.inner_type = type;
		this.aliasName = aliasName;
		this.typeLabel = typeLabel;
	}

	public CSNonGenericType_c(Type type) {
		this(VariableGenerator.nextAlias(), type);
	}

	@Override
	public String getTypeLabel() {
		return typeLabel;
	}

	public void setTypeLabel(String typeLabel) {
		this.typeLabel = typeLabel;
	}

	/** Return an immutable list of fields */
	@Override
	public List<? extends FieldInstance> fields() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		// if (obj instanceof TypeVariable) {
		// TypeVariable tv = (TypeVariable) obj;
		// return inner_type.equals(tv.upperBound());
		// } else if (obj instanceof JL5ParsedClassType_c) {
		// return inner_type.equals(obj);
		// }
		// if (obj instanceof CSNonGenericType) {
		// return inner_type.equals(((CSNonGenericType) obj).getType());
		// } else if (obj instanceof PrimitiveType) {
		// return inner_type.equals(obj);
		// }
		// assert false : "unexpected type: " + obj.getClass();
		// return false;
		if (obj instanceof CSNonGenericType) {
			// check if the outter type is the same
			if (!this.typeLabel.equals(((CSNonGenericType) obj).getTypeLabel()))
				return false;
			return inner_type.equals(((CSNonGenericType) obj).getType());
		}

		return inner_type.equals(obj);
	}

	// @Override
	// public int hashCode() {
	// return inner_type.hashCode();
	// }

	@Override
	public CSNonGenericType copy() {
		CSNonGenericType n = CSNonGenericWrapper.createNonGenericType(
				(JL5ParsedClassType_c) inner_type, this.typeLabel);
		if (this.id != null) {
			n.setID(this.id);
		}
		return n;
	}

	@Override
	public void setID(ID id) {
		// double setting ID is possible when call want to override
		// MethodInstance's returnType id. This happens only when the method is
		// reflection related

		assert id != null;
		this.id = id;
	}

	public ID getID() {
//		assert this.id != null;
		return this.id;
	}

	public void setType(Type type) {
		this.inner_type = (Type) type;
	}

	public Type getType() {
		return this.inner_type;
	}

	public String getAliasName() {
		return aliasName;
	}

	@Override
	public void setAliasName(String name) {
		assert false;
	}

	// @Override
	// public String fullName() {
	// return ((JL5ClassType) inner_type).fullName();
	// }

	@Override
	public PClass<TypeVariable, ReferenceType> pclass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPClass(PClass<TypeVariable, ReferenceType> pc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTypeVariables(List<TypeVariable> typeVars) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<TypeVariable> typeVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEnumConstant(EnumInstance ei) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<EnumInstance> enumConstants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumInstance enumConstantNamed(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends JL5MethodInstance> methods(JL5MethodInstance mi) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JL5Subst erasureSubst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void printNoParams(CodeWriter w) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toStringNoParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAnnotationElem(AnnotationTypeElemInstance ai) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRetainedAnnotations(
			RetainedAnnotations createRetainedAnnotations) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJob(Job job) {
		// TODO Auto-generated method stub

	}

	@Override
	public void position(Position pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public Source fromSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void package_(Package p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void superType(Type t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInterface(ReferenceType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInterfaces(List<? extends ReferenceType> l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addField(FieldInstance fi) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFields(List<? extends FieldInstance> l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMethod(MethodInstance mi) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMethods(List<? extends MethodInstance> l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addConstructor(ConstructorInstance ci) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConstructors(List<? extends ConstructorInstance> l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMemberClass(ClassType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMemberClasses(List<? extends ClassType> l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flags(Flags flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outer(ClassType t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void name(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void kind(Kind kind) {
		// TODO Auto-generated method stub
	}

	@Override
	public void inStaticContext(boolean inStaticContext) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean defaultConstructorNeeded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean membersAdded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supertypesResolved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean signaturesResolved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int numSignaturesUnresolved() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMembersAdded(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSupertypesResolved(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSignaturesResolved(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean needSerialization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void needSerialization(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public Resolver resolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Kind kind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTopLevel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Deprecated
	public boolean isInner() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNested() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInnerClass() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMember() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLocal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAnonymous() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inStaticContext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<? extends ConstructorInstance> constructors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends ClassType> memberClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassType memberClassNamed(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldInstance fieldNamed(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnclosed(ClassType outer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnclosedImpl(ClassType outer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasEnclosingInstance(ClassType encl) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasEnclosingInstanceImpl(ClassType encl) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ClassType outer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Package package_() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCanonical() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TypeSystem typeSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Position position() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equalsImpl(TypeObject t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type superType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends ReferenceType> interfaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends MemberInstance> members() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends MethodInstance> methods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends MethodInstance> methodsNamed(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends MethodInstance> methods(String name,
			List<? extends Type> argTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasMethod(MethodInstance mi) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasMethodImpl(MethodInstance mi) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String translate(Resolver c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayType arrayOf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayType arrayOf(int dims) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassType toClass() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public NullType toNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReferenceType toReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrimitiveType toPrimitive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayType toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean typeEquals(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean typeEqualsImpl(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubtype(Type ancestor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean descendsFrom(Type ancestor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCastValid(Type toType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isImplicitCastValid(Type toType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean numericConversionValid(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean numericConversionValid(long value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubtypeImpl(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean descendsFromImpl(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCastValidImpl(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isImplicitCastValidImpl(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean numericConversionValidImpl(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean numericConversionValidImpl(long value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrimitive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVoid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChar() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isByte() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShort() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInt() {
		// TODO Auto-generated method stub
		assert false;
		return false;
	}

	@Override
	public boolean isLong() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFloat() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDouble() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIntOrLess() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLongOrLess() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNumeric() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReference() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNull() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isArray() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClass() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isThrowable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUncheckedException() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComparable(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void print(CodeWriter w) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPackage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Package toPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type toType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flags flags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFlags(Flags flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public ReferenceType container() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContainer(ReferenceType container) {
		// TODO Auto-generated method stub

	}

	@Override
	public Declaration declaration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDeclaration(Declaration decl) {
		// TODO Auto-generated method stub

	}

	@Override
	public Job job() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LazyInitializer initializer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitializer(LazyInitializer init) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRawClass() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AnnotationTypeElemInstance annotationElemNamed(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AnnotationTypeElemInstance> annotationElems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String translateAsReceiver(Resolver resolver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetainedAnnotations retainedAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fullClassName() {
		return ((ClassType) this.inner_type).fullName();
	}

	// /*
	// * implement the superType after this line
	// */
	// public List constructors() {
	// return ((ClassType) inner_type).constructors();
	// }
	//
	// public List fields() {
	// return ((ClassType) inner_type).fields();
	// }
	//
	// public Flags flags() {
	// return ((ClassType) inner_type).flags();
	// }
	//
	// public List interfaces() {
	// return ((ClassType) inner_type).interfaces();
	// }
	//
	// public static final Kind UNKNOWN = new Kind("<unknown>");
	//
	// public Kind kind() {
	//
	// if (inner_type instanceof ClassType) {
	// return ((ClassType) inner_type).kind();
	// } else if (inner_type instanceof UnknownType) {
	// return UNKNOWN;
	// } else {
	// return null;
	// }
	// }
	//
	// public List memberClasses() {
	// return ((ClassType) inner_type).memberClasses();
	// }
	//
	// public List methods() {
	// return ((ClassType) inner_type).methods();
	// }
	//
	// public String name() {
	// return ((ClassType) inner_type).name();
	// }
	//
	// public ClassType outer() {
	// return ((ClassType) inner_type).outer();
	// }
	//
	// public Package package_() {
	// return ((ClassType) inner_type).package_();
	// }
	//
	// public Type superType() {
	// return ((ClassType) inner_type).superType();
	// }
	//
	// public boolean inStaticContext() {
	// return ((ClassType) inner_type).inStaticContext();
	// }
	//
	// public void setContainer(ReferenceType container) {
	// ((ClassType) inner_type).setContainer(container);
	// }
	//
	// public void setFlags(Flags flags) {
	// ((ClassType) inner_type).setFlags(flags);
	// }
	//
	// @Override
	// public Job job() {
	// return null;
	// }
	//
	// @Override
	// public boolean isRawClass() {
	// return ((JL5ClassType) inner_type).isRawClass();
	// }
	//
	// @Override
	// public AnnotationTypeElemInstance annotationElemNamed(String name) {
	// return ((JL5ClassType) inner_type).annotationElemNamed(name);
	// }
	//
	// @Override
	// public List<AnnotationTypeElemInstance> annotationElems() {
	// return ((JL5ClassType) inner_type).annotationElems();
	// }
	//
	// @Override
	// public String translateAsReceiver(Resolver resolver) {
	// return ((JL5ClassType) inner_type).translateAsReceiver(resolver);
	// }
	//
	// @Override
	// public RetainedAnnotations retainedAnnotations() {
	// return ((JL5ClassType) inner_type).retainedAnnotations();
	// }
	//
	// @Override
	// public List<EnumInstance> enumConstants() {
	// return ((JL5ClassType) inner_type).enumConstants();
	// }

	// /*
	// * methods from super class
	// */
	// @Override
	// public void addEnumConstant(EnumInstance ei) {
	// inner_type.addEnumConstant(ei);
	// }
	//
	// @Override
	// public List<EnumInstance> enumConstants() {
	// return inner_type.enumConstants();
	// }
	//
	// @Override
	// public EnumInstance enumConstantNamed(String name) {
	// return inner_type.enumConstantNamed(name);
	// }
	//
	// @Override
	// public AnnotationTypeElemInstance annotationElemNamed(String name) {
	// return inner_type.annotationElemNamed(name);
	// }
	//
	// @Override
	// public void addAnnotationElem(AnnotationTypeElemInstance ai) {
	// inner_type.addAnnotationElem(ai);
	// }
	//
	// @Override
	// public List<AnnotationTypeElemInstance> annotationElems() {
	// return inner_type.annotationElems();
	// }
	//
	// // find methods with compatible name and formals as the given one
	// @Override
	// public List<? extends JL5MethodInstance> methods(JL5MethodInstance mi) {
	// return inner_type.methods(mi);
	// }
	//
	// @Override
	// public List<JL5MethodInstance> methodsNamed(String name) {
	// return inner_type.methodsNamed(name);
	// }
	//
	// @Override
	// public ClassType outer() {
	// return inner_type.outer();
	// }
	//
	// @Override
	// public boolean isEnclosedImpl(ClassType maybe_outer) {
	// return inner_type.isEnclosedImpl(maybe_outer);
	// }
	//
	// @Override
	// public boolean isCastValidImpl(Type toType) {
	// return inner_type.isCastValidImpl(toType);
	// }
	//
	// @Override
	// public boolean isImplicitCastValidImpl(Type toType) {
	// return inner_type.isImplicitCastValidImpl(toType);
	// }
	//
	// @Override
	// public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
	// return inner_type.isImplicitCastValidChainImpl(toType);
	// }
	//
	// // /////////////////////////////////////
	// //
	// @Override
	// public PClass<TypeVariable, ReferenceType> pclass() {
	// return inner_type.pclass();
	// }
	//
	// @Override
	// public void setPClass(PClass<TypeVariable, ReferenceType> pc) {
	// inner_type.setPClass(pc);
	// }
	//
	// @Override
	// public void setTypeVariables(List<TypeVariable> typeVars) {
	// inner_type.setTypeVariables(typeVars);
	// }
	//
	// @Override
	// public List<TypeVariable> typeVariables() {
	// return inner_type.typeVariables();
	// }
	//
	// @Override
	// public JL5Subst erasureSubst() {
	// return inner_type.erasureSubst();
	// }
	//
	// /** Pretty-print the name of this class to w. */
	// @Override
	// public void print(CodeWriter w) {
	// inner_type.print(w);
	// }
	//
	// @Override
	// public void printNoParams(CodeWriter w) {
	// inner_type.printNoParams(w);
	// }
	//
	// @Override
	// public String toStringNoParams() {
	// return inner_type.toStringNoParams();
	// }
	//
	// @Override
	// public String toString() {
	// return inner_type.toString();
	// }
	//
	// @Override
	// public boolean isRawClass() {
	// return inner_type.isRawClass();
	// }
	//
	// @Override
	// public String translateAsReceiver(Resolver c) {
	// return inner_type.translateAsReceiver(c);
	// }
	//
	// @Override
	// public String translate(Resolver c) {
	// return inner_type.translate(c);
	// }
	//
	// @Override
	// public boolean descendsFromImpl(Type ancestor) {
	// return inner_type.descendsFromImpl(ancestor);
	// }
	//
	// @Override
	// public RetainedAnnotations retainedAnnotations() {
	// return inner_type.retainedAnnotations();
	// }
	//
	// @Override
	// public void setRetainedAnnotations(RetainedAnnotations
	// retainedAnnotations) {
	// inner_type.setRetainedAnnotations(retainedAnnotations);
	// }

}
