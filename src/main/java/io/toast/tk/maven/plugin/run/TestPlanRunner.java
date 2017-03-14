package io.toast.tk.maven.plugin.run;

import com.google.inject.Module;
import io.toast.tk.runtime.AbstractTestPlanRunner;

public class TestPlanRunner extends AbstractTestPlanRunner {

	private String reportPath;

	public TestPlanRunner(String reportPath, Module[] pluginModules) {
		super(pluginModules);
		this.reportPath = reportPath;
	}

	@Override
	public String getReportsOutputPath(){
		return reportPath;
	}

	@Override
	public void beginTest() {
		//NO-OP
	}

	@Override
	public void endTest() {
		//NO-OP
	}

	@Override
	public void initEnvironment() {
		//NO-OP
	}

	@Override
	public void tearDownEnvironment() {
		//NO-OP	
	}

}
