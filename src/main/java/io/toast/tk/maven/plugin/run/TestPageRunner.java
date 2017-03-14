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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beginTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initEnvironment() {
		// TODO Auto-generated method stub
		
	}


}
