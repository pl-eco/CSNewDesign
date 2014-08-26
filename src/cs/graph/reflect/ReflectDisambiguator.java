package cs.graph.reflect;

import java.util.LinkedList;

import cs.data.constraint.CallConstraint;
import cs.data.id.ID;
import cs.data.id.InstID;
import cs.data.id.LocalID;
import cs.data.reflect.ReflectGetMethodOrConstructor;
import cs.data.reflect.ReflectID;
import cs.data.reflect.ReflectMethodInvoke;
import cs.data.reflect.ReflectionHandler;
import cs.graph.CSGraph;
import cs.graph.GraphBuilder;
import cs.util.CSUtil;
import cs.util.EasyDebugger;

public class ReflectDisambiguator implements GraphBuilder {
	// target of the reflection method
	private InstID clazzInstID;
	private ReflectID reflectID;

	public ReflectDisambiguator(InstID instID, ReflectID reflectID) {
		this.clazzInstID = instID;
		this.reflectID = reflectID;
	}

	@Override
	public void buildGraph(CSGraph graph) {
		// 1. get the class instance and the method name
		String className = null;
		String methodName = null;
		if (clazzInstID instanceof ReflectGetMethodOrConstructor) {
			ReflectGetMethodOrConstructor ref = (ReflectGetMethodOrConstructor) clazzInstID;

			// get className
			className = ref.className();
			methodName = ref.methodName();
		} else {
			className = clazzInstID.className();
			methodName = CallConstraint.CONSTRUCTOR_NAME;
		}

		/*
		 * if the class name or the method name is not clear, the process is
		 * done
		 */
		if (className.equals(ReflectionHandler.UNKNOWN_NAME)
				|| methodName.equals(ReflectionHandler.UNKNOWN_NAME)) {
			graph.addInstance(new InstID(reflectID.invocationPoint(),
					ReflectionHandler.UNKNOWN_NAME,
					reflectID.invocationPoint(), false, reflectID
							.getAnalysisContext()));
			return;
		}

		ID callTarget = null;
		String callTargetClass = null;
		// newInstance. the target is the inst
		if (methodName.startsWith(CallConstraint.CONSTRUCTOR_NAME)) {
			callTarget = new InstID(reflectID.invocationPoint(), className,
					reflectID.invocationPoint(), false,
					reflectID.getAnalysisContext());
			callTargetClass = className;

			graph.addInstance((InstID) callTarget);
		}

		// method invoke. the target is the first argument
		else {
			callTarget = reflectID.arguments().get(0);
			callTargetClass = ((ReflectMethodInvoke) reflectID)
					.callTargetClass();
		}

		// 3. generate call
		CallConstraint call = new CallConstraint(reflectID.invocationPoint(),
				callTarget, callTargetClass, methodName,
				reflectID.callArguments(), null);

		graph.addCall(call.target().uniqueID(), call);
	}
}
