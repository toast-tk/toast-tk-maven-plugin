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

import io.toast.tk.dao.domain.impl.test.block.ITestPage;
import io.toast.tk.maven.plugin.run.TestPageRunner;
import io.toast.tk.plugin.IAgentPlugin;
import io.toast.tk.runtime.parse.FileHelper;
import io.toast.tk.runtime.parse.TestParser;



@Mojo(name = "run",
        defaultPhase = LifecyclePhase.VERIFY,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RunScriptsMojo extends AbstractScriptExecutionMojo<ITestPage> {

    @Parameter(required = false, alias = "pluginsDirectory")
    private String pluginDir;

    @Parameter(required = true, alias = "outputDirectory")
    private String outputDirectory;

    @Parameter(required = false, alias = "scripts")
    private FileSet[] scripts;

    @Parameter(required = true, defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
        	super.execute();
        	
            for (int i =0; i < scripts.length; i++){
                List<File> files = toFileList(scripts[i]);
                final List<ITestPage> testScripts = getScripts(files);
                execute(testScripts);
            }
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    @Override
    protected List<ITestPage> getScripts(List<File> files) {
        List<ITestPage> testScripts = new ArrayList<>();
        TestParser parser = new TestParser();
        files.forEach(p -> {
            try{
                List<String> scriptLines = FileHelper.getScript(new FileInputStream(p));
                ITestPage testScript = parser.parse(scriptLines, p.getName());
                testScripts.add(testScript);
            }catch(Exception e){
                getLog().error("Unable to parse " + p.getName() + " !", e);
            }
        });
        return testScripts;
    }

    private void execute(List<ITestPage> testScripts) {
        testScripts.forEach(script -> {
            try {
                run(script);
            } catch (Exception e) {
                getLog().error("Failed to execute " + script.getName() + " !", e);
            }
        });
    }

    protected ITestPage run(ITestPage testPage) throws IOException {
        getLog().info("Agent plugin class loader: " + IAgentPlugin.class.getClassLoader());
        TestPageRunner testPageRunner = new TestPageRunner(this.outputDirectory, this.pluginModules);
        return testPageRunner.runTestPage(testPage);
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
