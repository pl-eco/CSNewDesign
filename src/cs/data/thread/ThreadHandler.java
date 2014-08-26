package cs.data.thread;

import cs.data.constraint.CallConstraint;
import cs.data.id.FieldID;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.data.id.LocalID;
import cs.data.id.SpecialID;
import cs.data.id.VariableGenerator;
import cs.data.id.FieldID.Ops;
import cs.util.EasyDebugger;
import polyglot.ast.Call;
import polyglot.ast.Special;
import polyglot.types.ClassType;
import polyglot.types.Context_c;
import polyglot.types.MethodInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;

/*
 * The class processes thread related issues
 */
public class ThreadHandler {
	public static ID processRunMethod(Context_c context, SpecialID specID) {
		ID ret = specID;

		/*
		 * NOTE: type().toString() should be changed to
		 * (ClassType)type().fullName()
		 */

		// // method name must be "run"
		// if (!(context.currentCode() instanceof MethodInstance)
		// || !((MethodInstance) context.currentCode()).name().equals(
		// "run")) {
		// return ret;
		// }
		//
		// // must be in an object extends from Thread
		// if (!context.currentClass().superType().toString()
		// .equals("java.lang.Thread")) {
		// return ret;
		// }
		//
		// // the id should be changed from "this" to "this.data"
		// ret = new FieldID(specID, fieldId, "threadD", Ops.READ,
		// specID.getPosition() + "threadD");

		return ret;
	}

	/*
	 * Check if the give call AST is the start() method from a thread
	 */
	public static boolean isStartMethod(Call call) {
		if (!(call.target().type() instanceof ClassType)
				|| !call.name().equals("start"))
			return false;

		TypeSystem ts = call.type().typeSystem();

		Type type = call.target().type();
		while (type != null) {
			if (type.toString().equals("java.lang.Thread"))
				return true;

			// if the type is not referenceType, break the loop and it should
			// return false;
			type = ts.superType((ReferenceType) type);
		}

		return false;
	}

	/*
	 * if the method is from thread/runnable and method named "start", change to
	 * run
	 * 
	 * assign a field as the threadDate
	 * 
	 * Create a new object representing the thread
	 */
	public static CallConstraint processThreadMethod(Context_c context,
			Call call, CallConstraint callCons) {
		if (!isStartMethod(call))
			return callCons;

		/*
		 * NOTE: type().toString() should be changed to
		 * (ClassType)type().fullName()
		 */
		// // create the user Thread
		// // target of the run call should be the user thread
		// InstID newThread = new InstID(VariableGenerator.nextAlias(), call
		// .target().type().toString(), call.target().position()
		// + "~Thread");
		//
		// FieldID threadData = new FieldID(newThread, fieldId, "threadD",
		// Ops.WRITTEN, call.target().position() + "threadD");
		//
		// // add th.data = s;
		// ConstraintContainer.getInstance().addConstraint(
		// new FlowConstraint(threadData, callCons.target(),
		// FlowConstraint.FLOWTYPE.ASSIGN), context);
		//
		// /*
		// * change the call (name and target)
		// */
		// // target of the th.data
		// ID target = newThread;
		// callCons = new CallConstraint(callCons.invocationPoint(), target,
		// callCons.targetClass(), "run", callCons.arguments(),
		// (Context_c) callCons.getStaticContext());

		return callCons;
	}

	// create a fieldID represents the data field of a thread
	private static String fieldId = VariableGenerator.nextAlias();
}
