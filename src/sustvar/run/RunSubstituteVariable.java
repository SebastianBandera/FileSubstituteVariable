package sustvar.run;

import sustvar.main.MainSubstituteVariable;

public class RunSubstituteVariable {

	public static void main(String[] args) {
		Runnable runnable = new MainSubstituteVariable(args);
		runnable.run();
	}
}
