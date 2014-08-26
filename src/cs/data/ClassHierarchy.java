package cs.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import cs.data.id.VariableGenerator;

/*
 * stores the class hierarchical information. Like who is whose supper class
 */

public class ClassHierarchy {
	private Map<String, String> extendsRoute = new HashMap();

	private Map<String, Set> implementsRoute = new HashMap();

	private Map<String, Boolean> isConcreteClass = new HashMap();

	// thread class names
	// public static Map threadClass = new HashMap();
	// static {
	// threadClass.put("java.lang.Runnable", VariableGenerator.nextVar());
	// threadClass.put("java.lang.Thread", VariableGenerator.nextVar());
	// }

	// refreshed aliasNames
	public static Set threadNames = new HashSet();

	public void putClassInfo(String className, String supClassName,
			boolean isConcrete) {
		extendsRoute.put(className, supClassName);

		if (!isConcreteClass.containsKey(className)) {
			isConcreteClass.put(className, isConcrete);
		}
	}

	public void putInterfaceInfo(String className, String interfaze,
			boolean isConcrete) {
		if (!implementsRoute.containsKey(className)) {
			implementsRoute.put(className, new HashSet());
		}
		implementsRoute.get(className).add(interfaze);

		if (!isConcreteClass.containsKey(className)) {
			isConcreteClass.put(className, isConcrete);
		}

	}

	public String getSuperName(String className) {
		return extendsRoute.get(className);
	}

	// return the name of classes from super to sub classes
	// the class names include the current class name
	public Stack superToSub(String className) {
		Stack stack = new Stack();

		stack.push(className);
		while (true) {
			String supName = this.getSuperName(className);
			if (supName == null)
				break;
			stack.push(supName);
			className = supName;
		}

		return stack;
	}

	// public static void addThreadClass(String className) {
	// threadClass.put(className, VariableGenerator.nextVar());
	// }

	public Set<String> getConcreteClasses(String interfaceName) {
		Set<String> concreteSet = new TreeSet<String>();

		// check implements route
		for (Iterator iter = implementsRoute.keySet().iterator(); iter
				.hasNext();) {
			String implClass = (String) iter.next();
			if (!isConcreteClass.get(implClass).booleanValue())
				continue;

			Queue<String> queue = new LinkedList<String>();
			queue.add(implClass);
			while (queue.size() > 0) {
				String interClass = queue.poll();
				if (interClass.equals(interfaceName)) {
					concreteSet.add(implClass);
					break;
				}

				if (implementsRoute.containsKey(interClass)) {
					Set interSet = implementsRoute.get(interClass);
					for (Iterator iterator = interSet.iterator(); iterator
							.hasNext();) {
						queue.add((String) iterator.next());
					}
				}
			}

		}

		// check extends route
		for (Iterator iter = extendsRoute.keySet().iterator(); iter.hasNext();) {
			String subClass = (String) iter.next();
			if (!isConcreteClass.get(subClass).booleanValue())
				continue;

			String superClass = getSuperName(subClass);
			while (superClass != null) {
				if (superClass.equals(interfaceName)) {
					concreteSet.add(subClass);
					break;
				} else {
					subClass = superClass;
					superClass = getSuperName(subClass);
				}
			}
		}

		return concreteSet;
	}
}
