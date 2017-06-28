package io.toast.tk.maven.plugin.run;

import com.google.inject.Module;
import io.toast.tk.runtime.AbstractTestPlanRunner;

public class TestPlanRunner extends AbstractTestPlanRunner {

	private String reportPath;

	public TestPlanRunner(final String reportPath, 
			final String host, 
			final int port, 
			final String db,
			final Module... pluginModules) {
		super(host, port, db, pluginModules);
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
