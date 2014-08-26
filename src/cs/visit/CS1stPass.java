package cs.visit;

import java.util.List;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayInit;
import polyglot.ast.Assert;
import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Case;
import polyglot.ast.Cast;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassLit;
import polyglot.ast.CompoundStmt;
import polyglot.ast.Conditional;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Instanceof;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureCall;
import polyglot.ast.Return;
import polyglot.ast.SourceFile;
import polyglot.ast.Special;
import polyglot.ast.StringLit;
import polyglot.ast.Synchronized;
import polyglot.ast.Throw;
import polyglot.ast.Throw_c;
import polyglot.ast.TypeNode;
import polyglot.ast.Typed;
import polyglot.ast.Unary;
import polyglot.ext.jl5.ast.ElementValuePair_c;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.frontend.Job;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.Context_c;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;
import cs.ast.CSSpecial_c;
import cs.data.ClassHierarchy;
import cs.data.ConstraintContainer;
import cs.data.Properties;
import cs.data.constraint.CallConstraint;
import cs.data.constraint.FlowConstraint;
import cs.data.id.ArrayInstID;
import cs.data.id.FieldID;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.data.id.LocalID;
import cs.data.id.SpecialID;
import cs.data.id.StaticID;
import cs.data.id.VariableGenerator;
import cs.data.reflect.ReflectionHandler;
import cs.data.thread.ThreadHandler;
import cs.linearsolver.ValueHolder;
import cs.types.CSBaseType;
import cs.types.CSContext_c;
import cs.types.CSObjectType;
import cs.types.CSParsedClassType_c;
import cs.types.csTypeSystem_c;
import cs.util.CSUtil;
import cs.util.EasyDebugger;

public class CS1stPass extends TypeChecker {
	protected ConstraintContainer wholeSet = ConstraintContainer.getInstance();

	public CS1stPass(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	protected ConstraintContainer getContainer() {
		return wholeSet;
	}

	protected NodeVisitor enterCall(Node parent, Node n)
			throws SemanticException {
		if (n instanceof ClassDecl) {
			ClassDecl c = (ClassDecl) n;

			/*
			 * record the super class
			 */
			String superClass = null;
			if (c.superClass() != null) {
				superClass = ((CSObjectType) c.superClass().type())
						.fullClassName();
			} else {
				superClass = "java.lang.Object";
			}
			ClassHierarchy clsHrchy = wholeSet.getClassHierarchy();
			boolean isConcrete = !c.flags().isAbstract()
					&& !c.flags().isInterface();
			clsHrchy.putClassInfo(c.type().fullName(), superClass, isConcrete);

			/*
			 * record interfaces
			 */
			for (Object interf : c.interfaces()) {
				TypeNode tmpNode = (TypeNode) interf;
				String interfaze = ((CSObjectType) tmpNode.type())
						.fullClassName();

				clsHrchy.putInterfaceInfo(c.type().fullName(), interfaze,
						isConcrete);
			}
		} else if (n instanceof MethodDecl) {
			MethodDecl mDecl = (MethodDecl) n;

			/*
			 * set ID for returnType (VOID is represented by PrimitiveType)
			 */
			MethodInstance mInst = mDecl.methodInstance();

			String aliasName = this.getAliasName(mInst.returnType());
			((CSBaseType) mInst.returnType()).setID(new LocalID(aliasName,
					mInst.name() + "(...)\'s returnType", mInst.position()
							.toString()));

			// register the current block
			wholeSet.addBlockContainer((Context_c) context());
		} else if (n instanceof ConstructorDecl) {
			wholeSet.addBlockContainer((Context_c) context());

			wholeSet.putProcedureInstance((Context_c) context(),
					((ConstructorDecl) n).procedureInstance());
		}
		return super.enterCall(parent, n);
	}

	public Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
			throws SemanticException {
		// protected Node leaveCall(Node old, Node n, NodeVisitor v) {

		Node node = super.leaveCall(old, n, v);

		ValueHolder vh = ((csTypeSystem_c) ts).getValueHolder();

		if (node instanceof MethodDecl) {
			MethodDecl mDecl = (MethodDecl) n;

			if (mDecl.flags().isAbstract()) {
				return n;
			}

			/*
			 * store method instance
			 */
			wholeSet.putProcedureInstance((Context_c) context(),
					mDecl.methodInstance());
		}

		/*
		 * process node
		 */
		else if (node instanceof Special) {

			Special spNode = (Special) node;
			SpecialID.TYPE specialType = null;

			if (spNode.kind().equals(Special.SUPER))
				specialType = SpecialID.TYPE.SUPER;
			else if (spNode.kind().equals(Special.THIS))
				specialType = SpecialID.TYPE.THIS;
			else
				assert false;

			ID id = new SpecialID(((CSContext_c) context).currentThisAlias,
					specialType,
					context.currentClass().fullName(),
					node.position().toString());

			id = ThreadHandler.processRunMethod((Context_c) context,
					(SpecialID) id);

			CSUtil.setID((Expr) node, id);
		} else if (node instanceof New) {
			String aliasName = this.getAliasName(((New) node).type());

			New nNew = (New) node;
			String className = ((CSObjectType) nNew.type()).fullClassName();

			// set arguments
			List<ID> args = CSUtil.getArguments(nNew.arguments());

			String methodName = ConstraintContainer
					.buildMethodName((ProcedureInstance) nNew
							.constructorInstance().declaration());

			ID target = new InstID(aliasName, className, node.position()
					.toString());
			wholeSet.addCallConstraint(target, className, methodName, args,
					(Context_c) context(), vh);
			CSUtil.setID((Expr) node, target);
		} else if (node instanceof Binary || node instanceof StringLit
				|| node instanceof ClassLit) {
			String aliasName = this.getAliasName(((Expr) node).type());

			CSUtil.setID((Expr) node, new LocalID(aliasName, node.position()
					.toString()));
		} else if (node instanceof Cast) {
			Cast cNode = (Cast) node;

			// set type
			String aliasName = this.getAliasName(((Cast) node).type());
			CSUtil.setID(cNode, new LocalID(aliasName, "CAST", node.position()
					.toString()));

			// create FlowConstraint
			ID castee = CSUtil.getID(cNode.expr().type());
			ID castTo = CSUtil.getID(cNode.type());

			wholeSet.addFlowConstraint(castTo, castee,
					FlowConstraint.FLOWTYPE.CAST, (Context_c) context, vh);
		} else if (node instanceof Field) {
			/*
			 * flattened way to process Field
			 * 
			 * A field is represented by a LocalID. The target of a field can be
			 * any ID except FieldID
			 */
			Field fNode = (Field) node;
			String fieldAlias = null;
			if (fNode instanceof EnumConstant) {
				fieldAlias = this.getAliasName(fNode.type());
			} else {
				fieldAlias = this.getAliasName(((FieldInstance) fNode
						.fieldInstance().declaration()).type());
			}

			ID target = null;
			if (fNode.flags().isStatic()) {
				String className = ((ClassType) fNode.target().type())
						.fullName();
				target = new StaticID(className, className + "." + fNode.name()
						+ "'s target ", fNode.target().position().toString());
			} else {
				target = CSUtil.getID(fNode.target().type());
			}
			((CSBaseType) fNode.type()).setID(new FieldID(target, fieldAlias,
					fNode.name(), this.getFieldOps(fNode.type(), parent), node
							.position().toString()));

		} else if (node instanceof ArrayAccess) {
			ArrayAccess arrayNode = (ArrayAccess) node;
			ID target = CSUtil.getID(arrayNode.array().type());

			String wholeName = this.getAliasName(arrayNode.type());

			CSUtil.setID((Expr) node, FieldID.createArrayElement(wholeName,
					target, this.getFieldOps(arrayNode.type(), parent), node
							.position().toString()));
		} else if (node instanceof FieldDecl) {
			FieldDecl fDecl = (FieldDecl) node;

			String fieldAlias = this.getAliasName(fDecl.type().type());
			// if the decl is initialized
			if (fDecl.init() != null) {
				// target
				ID target = null;
				if (fDecl.flags().isStatic()) {
					String className = this.context().currentClass().fullName();
					target = new StaticID(className, className + "."
							+ fDecl.name() + "'s target ", fDecl.position()
							.toString());
				} else {
					target = new SpecialID(((CSContext_c) context).currentThisAlias,
							SpecialID.TYPE.THIS,
							context.currentClass().fullName(),
							fDecl.type().position().toString());
				}

				// FlowConstraint
				ID left = new FieldID(target, fieldAlias, fDecl.name(),
						FieldID.Ops.WRITTEN, fDecl.type().position().toString());

				ID right = CSUtil.getID(fDecl.init().type());

				wholeSet.addFlowConstraint(left, right,
						FlowConstraint.FLOWTYPE.ASSIGN, (Context_c) context(),
						vh);
			}
		} else if (node instanceof LocalDecl) {
			LocalDecl lDecl = (LocalDecl) node;

			this.setLocalInstance(lDecl.localInstance());

			if (lDecl.init() != null) {
				ID left = CSUtil.getID(lDecl.localInstance().type());

				ID right = CSUtil.getID(lDecl.init().type());
				wholeSet.addFlowConstraint(left, right,
						FlowConstraint.FLOWTYPE.ASSIGN, (Context_c) context(),
						vh);
			}
		} else if (node instanceof Assign) {
			Assign nAsgn = (Assign) node;

			ID left = CSUtil.getID(nAsgn.left().type());

			ID right = CSUtil.getID(nAsgn.right().type());
			wholeSet.addFlowConstraint(left, right,
					FlowConstraint.FLOWTYPE.ASSIGN, (Context_c) context(), vh);
		} else if (node instanceof Conditional) {
			Conditional condNode = (Conditional) node;

			// create a localID
			LocalID fakeID = new LocalID("conditionalFake", node.position()
					.toString());

			// make true and false conditions flow to this localID
			ID consequence = CSUtil.getID(condNode.consequent().type());
			wholeSet.addFlowConstraint(fakeID, consequence,
					FlowConstraint.FLOWTYPE.ASSIGN, (Context_c) context(), vh);

			ID alter = CSUtil.getID(condNode.alternative().type());
			wholeSet.addFlowConstraint(fakeID, alter,
					FlowConstraint.FLOWTYPE.ASSIGN, (Context_c) context(), vh);

			// set the localID back to the node
			CSUtil.setID(condNode, fakeID);
		} else if (node instanceof Formal) {
			Formal frmlNode = (Formal) node;
			this.setLocalInstance(frmlNode.localInstance());
		} else if (node instanceof Return) {
			// a flowConstraint
			Return retNode = (Return) node;

			// return is not void (in form of "return;")
			if (retNode.expr() != null) {
				MethodInstance mInst = (MethodInstance) this.context()
						.currentCode();
				ID from = CSUtil.getID(retNode.expr().type());

				ID to = CSUtil.getID(mInst.returnType());

				wholeSet.addFlowConstraint(to, from,
						FlowConstraint.FLOWTYPE.RETURN, (Context_c) context, vh);
			}
		}
		/*
		 * process constraint
		 */
		else if (node instanceof Call) {
			Call cNode = (Call) node;

			// the call from array can be ignored
			if (cNode.target().type() instanceof ArrayType
					|| cNode.methodInstance().container() instanceof ArrayType) {
				// create a LocalID and stick back to the type
				CSUtil.setID(cNode, new LocalID(cNode.target().toString() + "."
						+ cNode.name(), cNode.position().toString() + "*"));
				return node;
			}

			/*
			 * get aliasName
			 */
			String aliasName = null;

			// deal with "clone" specially
			if (cNode.name().equals("clone")) {
				aliasName = this.getAliasName(cNode.target().type());
			} else if (cNode.type().isPrimitive()) {
				aliasName = VariableGenerator.nextAlias();
			} else {
//				if(!(cNode instanceof CSBaseType)){
//					EasyDebugger.message(cNode.position());
//				}
				aliasName = this.getAliasName(cNode.type());
			}

			/*
			 * set target
			 */
			ID target = null;
			// special process with static method
			if (cNode.methodInstance().flags().isStatic()) {
				// set target for static method
				String className = ((ClassType) cNode.target().type())
						.fullName();

				target = new StaticID(className, className + "." + cNode.name()
						+ "'s target ", cNode.target().position().toString());
			} else {
				// set target for non-static method
				target = CSUtil.getID(cNode.target().type());
			}

			ID attachID = null;
			CallConstraint call = null;
			if ((attachID = ReflectionHandler.captureForName(aliasName, cNode)) != null) {
				/*
				 * no need to add a CallConstraint for "forName", because it
				 * only represents a new object
				 */
			} else if ((attachID = ReflectionHandler
					.captureGetMethodOrConstructor(aliasName, target, cNode)) != null) {
				/*
				 * no need to add a CallConstraint for "getMethod" or
				 * "getConstructor", because it only represents a new object
				 */
			} else {
				// reflection
				if ((call = ReflectionHandler.captureReflection(aliasName,
						target, cNode)) != null) {
				}
				// regular call
				else {
					// get methodName
					String signature = ConstraintContainer
							.buildMethodName(((JL5MethodInstance) cNode
									.methodInstance().declaration()));

					// set arguments
					List<ID> args = CSUtil.getArguments(cNode.arguments());

					if (ThreadHandler.isStartMethod(cNode)) {
						signature = "run";
					}

					call = new CallConstraint(aliasName, target,
							((CSObjectType) cNode.target().type())
									.fullClassName(), signature, args,
							(Context_c) context());

					// process the Thread feature
					call = ThreadHandler.processThreadMethod(
							(Context_c) context, cNode, call);
				}

				wholeSet.addCallConstraint(call, (Context_c) context, vh);
				attachID = new LocalID(aliasName, cNode.target().toString()
						+ "." + cNode.name(), cNode.position().toString() + "*");
			}

			// attach the ID to the type
			CSUtil.setID(cNode, attachID);

		} else if (node instanceof ConstructorCall) {
			ConstructorCall cNode = (ConstructorCall) node;

			SpecialID.TYPE type = null;

			if (cNode.kind().equals(ConstructorCall.SUPER))
				type = SpecialID.TYPE.SUPER;
			else if (cNode.kind().equals(ConstructorCall.THIS))
				type = SpecialID.TYPE.THIS;
			else
				assert false;
			
			// target
			ID target = new SpecialID(((CSContext_c) context).currentThisAlias,
					type, context.currentClass().fullName(),
					node.position() + "*");

			// methodName
			String methodName = ConstraintContainer.buildMethodName(cNode
					.procedureInstance());

			List<ID> args = CSUtil.getArguments(cNode.arguments());

			wholeSet.addCallConstraint(target, context.currentClass()
					.fullName(), methodName, args, (Context_c) context, vh);
		}

		/*
		 * not processed yet
		 */
		else if (node instanceof NewArray) {
			NewArray nArray = (NewArray) node;

			String aliasName = this.getAliasName(nArray.type());
			ID id = null;
			if (nArray.init() == null) {
				id = new ArrayInstID(aliasName, nArray.type().toString(),
						CSUtil.EMPTY_LIST, node.position().toString());
			} else {
				id = CSUtil.getID(nArray.init().type());
			}

			CSUtil.setID((Expr) node, id);

		} else if (node instanceof ArrayInit) {
			ArrayInit arrayInit = (ArrayInit) node;

			String aliasName = this.getAliasName(arrayInit.type());
			List<ID> init = CSUtil.getArguments(arrayInit.elements());

			CSUtil.setID((Expr) node, new ArrayInstID(aliasName, arrayInit.type()
					.toString(), init, arrayInit.position().toString()));
		} else if (node instanceof SourceFile) {
			// EasyDebugger.message(wholeSet.toString(((csTypeSystem_c) ts)
			// .getValueHolder()));

			// ((csTypeSystem_c) ts).getValueHolder().printPreValues();
		}

		return node;
	}

	// return the aliasName of the giving type
	protected String getAliasName(Type type) {
		if (!(type instanceof CSBaseType)) {
			EasyDebugger.exit(type.getClass());
		}
		String ret = ((CSBaseType) type).getAliasName();
		assert ret != null;
		return ret;
	}

	// set a LocalID to LocalInstances
	protected void setLocalInstance(LocalInstance li) {
		CSBaseType type = (CSBaseType) li.type();
		type.setID(new LocalID(type.getAliasName(), li.name(), li.position()
				.toString()));
	}

	// check if the current field is written
	protected FieldID.Ops getFieldOps(Type fType, Node parent) {
		// return FieldID.Ops.READ;
		if (parent instanceof Field) {
			return FieldID.Ops.READ;
		} else if (parent instanceof Assign
				&& ((Assign) parent).left().type().typeEquals(fType)) {
			return FieldID.Ops.WRITTEN;
		} else if (parent instanceof Assign
				&& ((Assign) parent).right().type().typeEquals(fType)) {
			return FieldID.Ops.READ;
		} else if (parent instanceof LocalDecl) {
			return FieldID.Ops.READ;
		} else if (parent instanceof FieldDecl
				&& ((FieldDecl) parent).type().type().typeEquals(fType)) {
			return FieldID.Ops.WRITTEN;
		} else if (parent instanceof FieldDecl
				&& ((FieldDecl) parent).init().type().typeEquals(fType)) {
			return FieldID.Ops.READ;
		} else if (parent instanceof ProcedureCall) {
			return FieldID.Ops.READ;
		} else if (parent instanceof CompoundStmt) {
			return FieldID.Ops.READ;
		} else /*
				 * if (parent instanceof Unary || parent instanceof Binary ||
				 * parent instanceof NewArray || parent instanceof ArrayAccess
				 * || parent instanceof Case || parent instanceof Cast || parent
				 * instanceof Return || parent instanceof Conditional || parent
				 * instanceof ArrayInit || parent instanceof Instanceof ||
				 * parent instanceof ElementValuePair_c || parent instanceof
				 * Throw || parent instanceof Assert)
				 */{
			return FieldID.Ops.READ;
		}

	}
}
