package io.toast.tk.maven.plugin.run;

import com.google.inject.Module;
import io.toast.tk.runtime.AbstractScenarioRunner;

public class TestPageRunner extends AbstractScenarioRunner{

	private String reportPath;

	public TestPageRunner(String reportPath, Module[] pluginModules) {
		super(pluginModules);
		this.reportPath = reportPath;
	}

	@Override
	public String getReportsOutputPath(){
		return reportPath;
	}

	@Override
	public void tearDownEnvironment() {
		// NO-OP
	}

	@Override
	public void beginTest() {
		// NO-OP
	}

	@Override
	public void endTest() {
		// NO-OP
	}

	@Override
	public void initEnvironment() {
		// NO-OP
	}


}
