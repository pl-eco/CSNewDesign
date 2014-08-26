package cs.linearsolver;

public class Factor {
	private int coefficient;
	private String variable;

	public Factor(String variable, int coef) {
		this.variable = variable;
		this.coefficient = coef;
	}

	public int getCoefficient() {
		return coefficient;
	}

	public String getVariable() {
		return variable;
	}

	public void increaseCoef() {
		coefficient++;
	}
}