package io.toast.tk.maven.plugin;

import com.google.inject.Module;
import io.toast.tk.adapter.cache.ToastCache;
import io.toast.tk.core.annotation.Action;
import io.toast.tk.core.annotation.ActionAdapter;
import io.toast.tk.dao.domain.impl.test.block.ITestPage;
import io.toast.tk.maven.plugin.run.TestPageRunner;
import io.toast.tk.plugin.IAgentPlugin;
import io.toast.tk.plugin.PluginLoader;
import io.toast.tk.runtime.parse.FileHelper;
import io.toast.tk.runtime.parse.TestParser;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.ClassFile;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


@Mojo(name = "run",
        defaultPhase = LifecyclePhase.VERIFY,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RunScriptsMojo extends AbstractMojo {

    @Parameter(required = false, alias = "pluginsDirectory")
    private String pluginDir;

    @Parameter(required = true, alias = "outputDirectory")
    private String outputDirectory;

    @Parameter(required = false, alias = "scripts")
    private FileSet[] scripts;

    @Parameter(required = true, defaultValue = "${project}", readonly = true)
    private MavenProject project;

    private TestPageRunner testPageRunner;
    private Module[] pluginModules;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File file = new File(this.outputDirectory);

            if(!file.exists()){
                file.mkdir();
            } else if(!file.isDirectory()){
                throw new IOException(this.outputDirectory + "isn't a valid directory !");
            }

            PluginLoader loader = new PluginLoader(this.pluginDir);
            this.pluginModules = pluginDir == null ? new Module[]{} : loader.collectGuiceModules(loader.loadPlugins(IAgentPlugin.class.getClassLoader()));
            addURL(new File(project.getBuild().getOutputDirectory()).toURI().toURL());

            try {
                extendPluginClassPath();
            } catch (DependencyResolutionRequiredException e) {
                getLog().error(e);
            }

            for (int i =0; i < scripts.length; i++){
                List<File> files = toFileList(scripts[i]);
                final List<ITestPage> testScripts = getScripts(files);
                executeScripts(testScripts);
            }
        } catch (IOException e) {
            getLog().error(e);
        }
    }


    public static List<File> toFileList(FileSet scriptSet) throws IOException {
        File directory = new File(scriptSet.getDirectory());
        String includes = toString(scriptSet.getIncludes());
        String excludes = toString(scriptSet.getExcludes());
        return FileUtils.getFiles(directory, includes, excludes);
    }

    private static String toString(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(string);
        }
        return sb.toString();
    }

    private List<ITestPage> getScripts(List<File> files) {
        List<ITestPage> testScripts = new ArrayList<>();
        TestParser parser = new TestParser();
        files.forEach(p -> {
            try{
                List<String> scriptLines = FileHelper.getScript(new FileInputStream(p));
                ITestPage testScript = parser.parse(scriptLines, p.getName().toString());
                testScripts.add(testScript);
            }catch(Exception e){
                getLog().error("Unable to parse " + p.getName() + " !", e);
            }
        });
        return testScripts;
    }

    private void executeScripts( List<ITestPage> testScripts) {
        testScripts.forEach(script -> {
            try {
                run(script);
            } catch (Exception e) {
                getLog().error("Failed to execute " + script.getName() + " !", e);
            }
        });
    }

    public ITestPage run(ITestPage testPage) throws IOException {
        getLog().info("Agent plugin class loader: " + IAgentPlugin.class.getClassLoader());
        this.testPageRunner = new TestPageRunner(this.outputDirectory, this.pluginModules);
        return testPageRunner.runTestPage(testPage);
    }

    private static final Class[] parameters = new Class[]{URL.class};
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }


    private void extendPluginClassPath()
            throws IOException,
            DependencyResolutionRequiredException {
        List<String> elements = project.getRuntimeClasspathElements();
        getLog().info("Extending plugin classpath... " );
        for (String element : elements) {
            addURL(new File(element).toURI().toURL());
            getLog().info("- " + new File(element).toURI().toURL());
        }

        ClassPool cp= ClassPool.getDefault();
        File directory = new File(project.getBuild().getOutputDirectory());
        Iterator<File> iterateFiles = org.apache.commons.io.FileUtils.iterateFiles(directory, new String[]{"class"}, true);
        while (iterateFiles.hasNext()) {
            try {
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(iterateFiles.next()));
                ClassFile classFile = new ClassFile(dataInputStream);
                CtClass c = cp.get(classFile.getName());
                getLog().info("Adding adapters from project classpath... " + c.getName());
                if (c.hasAnnotation(ActionAdapter.class) && !Modifier.isAbstract(c.getModifiers())) {
                    getLog().info("- " + c.getName());
                    ToastCache.getInstance().addActionAdapter(c.toClass());
                }
            } catch (Exception e) {
                getLog().error(e);
            }
        }
    }
}
