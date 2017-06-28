package io.toast.tk.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import io.toast.tk.runtime.parse.TestPlanParser;
import io.toast.tk.dao.domain.impl.test.block.ITestPlan;
import io.toast.tk.maven.plugin.run.TestPlanRunner;
import io.toast.tk.plugin.IAgentPlugin;
import io.toast.tk.runtime.ToastRuntimeException;
import io.toast.tk.runtime.parse.FileHelper;

@Mojo(name = "report",
        defaultPhase = LifecyclePhase.VERIFY,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresOnline = true)
public class RunTestPlanMojo extends AbstractScriptExecutionMojo<ITestPlan> {

    @Parameter(required = false, alias = "pluginsDirectory")
    private String pluginDir;

    @Parameter(required = true, alias = "outputDirectory")
    private String outputDirectory;

    @Parameter(required = false, alias = "scripts")
    private FileSet[] scripts;

    @Parameter(required = true, alias = "apiKey")
    private String apiKey;
    
    @Parameter(required = true, alias = "mongoHost")
    private String mongoHost;
    
    @Parameter(required = true, alias = "mongoPort")
    private Integer mongoPort;
    
    @Parameter(required = false, alias = "useRemoteRepository", defaultValue = "false")
    private Boolean useRemoteRepository;
    
    @Parameter(required = true, alias = "mongoDb")
    private String mongoDb;
    
    @Parameter(required = true, defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
        	super.execute();
        	
            for (int i =0; i < scripts.length; i++){
                List<File> files = toFileList(scripts[i]);
                final List<ITestPlan> plans = getScripts(files);
                execute(plans);
            }
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    @Override
    protected List<ITestPlan> getScripts(List<File> files) {
        List<ITestPlan> plans = new ArrayList<>();
        TestPlanParser parser = new TestPlanParser();
        files.forEach(p -> {
            try{
                List<String> lines = FileHelper.getScript(new FileInputStream(p));
                ITestPlan plan = parser.parse(lines, p.getName());
                plans.add(plan);
            }catch(Exception e){
                getLog().error("Unable to parse " + p.getName() + " !", e);
            }
        });
        return plans;
    }

    private void execute(List<ITestPlan> plans) {
    	plans.forEach(plan -> {
            try {
                run(plan);
            } catch (Exception e) {
                getLog().error("Failed to execute " + plan.getName() + " !", e);
            }
        });
    }

    protected void run(ITestPlan plan) throws IOException, ToastRuntimeException {
        getLog().info("Agent plugin class loader: " + IAgentPlugin.class.getClassLoader());
        TestPlanRunner runner = new TestPlanRunner(this.outputDirectory, 
        		this.mongoHost,
        		this.mongoPort,
        		this.mongoDb,
        		this.pluginModules);
        runner.testAndStore(apiKey, plan, useRemoteRepository.booleanValue());
    }

	@Override
	public MavenProject getProject() {
		return this.project;
	}


	@Override
	protected String getPluginDir() {
		return this.pluginDir;
	}


	@Override
	protected String getOutputDirectory() {
		return this.outputDirectory;
	}
}
