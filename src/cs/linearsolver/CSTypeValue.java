package cs.linearsolver;

public abstract class CSTypeValue {
	protected String var;

	protected CSTypeValue(String var) {
		this.var = var;
	}

	public String getVar() {
		return var;
	}
}
