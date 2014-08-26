package cs.linearsolver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import cs.data.constraint.CallConstraint;
import cs.graph.GraphElement;
import cs.util.EasyDebugger;

import ilog.concert.*;
import ilog.cplex.*;

// Make into variables when integers (PCInfo.var + "#P" or "#C")
public abstract class CPlexWrapper {
	protected IloCplex cplex;
	protected Map<String, IloIntVar> vars;
	protected List<IloRange> equations;
	protected IloObjective objective;
	protected Map<String, Integer> solutions;

	protected ValueHolder valueHolder;

	/* Ignores all attempts to write */
	class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
		}
	}

	public CPlexWrapper(ValueHolder valueHolder) {
		try {
			cplex = new IloCplex();
			vars = new HashMap<String, IloIntVar>();
			equations = new LinkedList<IloRange>();
			cplex.setOut(new NullOutputStream());

			this.valueHolder = valueHolder;
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		}
	}
	
	public ValueHolder getValueHolder(){
		return this.valueHolder;
	}

	/* Converts a variable name into its internal representation */
	protected IloIntVar getExpr(String name) {
		if (vars.containsKey(name))
			return vars.get(name);
		IloIntVar temp = null;
		try {
			temp = cplex.intVar(0, Integer.MAX_VALUE, name);
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		}
		vars.put(name, temp);
		return temp;
	}

	public enum Operator {
		EQ, GE, LE
	}

	public void addEquation(String left, Operator op, String right) {
		assert left != null && right != null : "The parameters cannot be null";

		if (left.trim().equals(right.trim()))
			return;

		IloConstraint cons = null;

		IloNumExpr leftN = getExpr(left);
		IloNumExpr rightN = getExpr(right);

		try {
			switch (op) {
			case EQ:
				cons = cplex.addEq(leftN, rightN);
				break;
			case GE:
				cons = cplex.addGe(leftN, rightN);
				break;
			case LE:
				cons = cplex.addLe(leftN, rightN);
				break;
			default:
				assert false;
			}
		} catch (IloException e) {
			e.printStackTrace();
		}

		equations.add((IloRange) cons);
	}

	// /* Adds an Equation as a constraint */
	// public void addEquation(Equation eq)
	// {
	// IloNumExpr[] terms = new IloNumExpr[eq.getFactors().size()];
	// try
	// {
	// for(int i = 0; i < terms.length; i++)
	// {
	// Factor factor = eq.getFactors().get(i);
	// terms[i] = cplex.prod(factor.coefficient, getExpr(factor.variable));
	// }
	// switch(eq.getRelationship())
	// {
	// case Equation.EQ:
	// equations.add(cplex.addEq(cplex.sum(terms), eq.getValue()));
	// break;
	// case Equation.GEQ:
	// equations.add(cplex.addGe(cplex.sum(terms), eq.getValue()));
	// break;
	// case Equation.LEQ:
	// equations.add(cplex.addLe(cplex.sum(terms), eq.getValue()));
	// break;
	// default:
	// EasyDebugger.exit("error! symbol is not recognizable in equation");
	// }
	// }
	// catch(IloException e)
	// {
	// System.err.println("Concert exception `" + e + "` caught");
	// }
	//
	// }

	public abstract void addFlow(GraphElement from, GraphElement to);

	public abstract void addExtraEq();

	public void addCall(CallConstraint call) {

	}

	// /* Builds a set of constraints from a FlowConstraint */
	// public abstract void addLocalFlow(GraphElement from, GraphElement to);
	//
	// /* Builds a set of constraints from a FlowConstraint */
	// public abstract void addCrossFlow(GraphElement inner, GraphElement
	// target,
	// GraphElement outter);

	/*
	 * Runs the solver Returns true if a solution was found, false if no
	 * solution exists, in which case the reason will be printed.
	 */
	public boolean solve() {
		try {
			if (!(cplex.solve())) {
				EasyDebugger.message("Can't solve: "
						+ cplex.getStatus().toString());
				return false;
			}
			// Can change type if needed
			IloIntVar[] variables = (IloIntVar[]) vars.values().toArray(
					new IloIntVar[0]);
			double[] vals = cplex.getValues(variables);
			solutions = new HashMap<String, Integer>();
			for (int i = 0; i < vals.length; i++) {
				solutions.put(variables[i].getName(), (int) vals[i]);
			}

			System.out.println("# of var: " + vars.size());
			System.out.println("# of equation: " + equations.size());
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		}

		return true;
	}

	/*
	 * Simply uses the sum of all used variables as the objective function,
	 * taking whether it should be minimized or maximized
	 */
	protected void addObjectiveFunction(CPlexMinMax max) {
		if (max == CPlexMinMax.MAX) {
			try {
				objective = cplex.addMaximize(cplex.sum((IloIntVar[]) vars
						.values().toArray(new IloIntVar[0])));
			} catch (IloException e) {
				System.err.println("Concert exception `" + e + "` caught");
			}
		} else {
			try {
				objective = cplex.addMinimize(cplex.sum((IloIntVar[]) vars
						.values().toArray(new IloIntVar[0])));
			} catch (IloException e) {
				System.err.println("Concert exception `" + e + "` caught");
			}
		}
	}

	/*
	 * Uses the sum of the given array of factors as the objective function,
	 * also taking whether it should be minimized or maximized
	 */
	protected void addObjectiveFunction(Collection<Factor> factors,
			CPlexMinMax max) {
		IloNumExpr[] terms = new IloNumExpr[factors.size()];
		try {
			int i = 0;
			for (Factor factor : factors) {
				terms[i++] = cplex.prod((double) factor.getCoefficient(),
						getExpr(factor.getVariable()));
				// System.out.println(terms[i - 1]);
			}
			if (max == CPlexMinMax.MAX)
				objective = cplex.addMaximize(cplex.sum(terms));
			else
				objective = cplex.addMinimize(cplex.sum(terms));
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		}
	}

	/* Returns a map of strings (variable names) to integers (their solutions) */
	public Map<String, Integer> getSolution() {
		if (solutions == null) {
			EasyDebugger
					.exit("Cannot get the solution. The equation set is not solved yet or there is no solution.");
		}
		return solutions;
	}

	/* Prints the objective function */
	public void printObjective() {
		try {
			EasyDebugger.message(convert(objective.getExpr()));
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		}
	}

	/* Prints the constraints to EasyDebugger */
	public void printEquations() {
		StringBuilder line = new StringBuilder();

		try {
			for (int i = 0; i < equations.size(); i++) {
				line.setLength(0);
				line.append(convert(equations.get(i).getExpr()));
				if (equations.get(i).getLB() == equations.get(i).getUB())
					line.append(" = ").append(equations.get(i).getLB());
				else if (equations.get(i).getLB() == -1 * Double.MAX_VALUE)
					line.append(" <= ").append(equations.get(i).getUB());
				else
					line.append(" >= ").append(equations.get(i).getLB());
				EasyDebugger.message(line.toString());
			}
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		}
	}

	/*
	 * Prints the constraints to a file named by the the parameter, as well as
	 * to EasyDebugger
	 */
	public void printEquations(String fileName) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(fileName)));
			StringBuilder line;
			for (int i = 0; i < equations.size(); i++) {
				line = new StringBuilder();
				line.append(convert(equations.get(i).getExpr()));
				if (equations.get(i).getLB() == equations.get(i).getUB())
					line.append(" = " + equations.get(i).getLB());
				else if (equations.get(i).getLB() == -1 * Double.MAX_VALUE)
					line.append(" <= " + equations.get(i).getUB());
				else
					line.append(" >= " + equations.get(i).getLB());
				out.println(line.toString());
			}
			out.close();
		} catch (IloException e) {
			System.err.println("Concert exception `" + e + "` caught");
		} catch (IOException e) {
			System.err.println("File error: " + e);
		}
	}

	/* Prepares a string as a clear representation of the variable */
	private String convert(IloNumExpr expr) {
		StringBuilder str = new StringBuilder(expr.toString());
		str.deleteCharAt(0);
		str.deleteCharAt(str.length() - 1);
		if (str.length() >= 4 && str.substring(0, 4).equals("1.0*"))
			str.delete(0, 4);
		else if (str.length() > 5 && str.substring(0, 5).equals("-1.0*"))
			str.delete(1, 5);
		for (int i = 1; i < str.length() - 4; i++) {
			if (str.substring(i, i + 5).equals(" 1.0*"))
				str.delete(i + 1, i + 5);
		}
		return str.toString();
	}
}
