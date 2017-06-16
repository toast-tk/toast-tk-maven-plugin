package io.toast.tk.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.google.inject.Module;

import io.toast.tk.adapter.cache.ToastCache;
import io.toast.tk.plugin.IAgentPlugin;
import io.toast.tk.plugin.PluginLoader;

public abstract class AbstractScriptExecutionMojo<E> extends AbstractMojo {

    protected Module[] pluginModules;
    
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			extendPluginClassPath();
			initReportOutputDir();
			initPluginModules();
		} catch (IOException | IllegalAccessException | DependencyResolutionRequiredException e) {
			getLog().error(e);
		}
	}

	private void initPluginModules() throws IllegalAccessException {
		PluginLoader loader = new PluginLoader(getPluginDir());
		this.pluginModules = getPluginDir() == null ? new Module[] {}
				: loader.collectGuiceModules(loader.loadPlugins(IAgentPlugin.class.getClassLoader()));
	}


	protected abstract MavenProject getProject();
	
	protected abstract String getPluginDir();

	protected abstract String getOutputDirectory();

	private void initReportOutputDir() throws IOException {
		File file = new File(getOutputDirectory());
		if (!file.exists()) {
			file.mkdir();
		} else if (!file.isDirectory()) {
			throw new IOException(getOutputDirectory() + "isn't a valid directory !");
		}
	}


	public static void addURL(URL u, ClassLoader classLoader) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) classLoader;
		Class sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Exception e) {
			throw new IOException("Error, could not add URL to system classloader", e);
		}
	}

	private void extendPluginClassPath() throws IOException, DependencyResolutionRequiredException {
		List<String> elements = getProject().getRuntimeClasspathElements();
		getLog().info("Extending plugin classpath with project runtime dependencies... ");
		ClassLoader classLoader = ToastCache.class.getClassLoader();
		for (String element : elements) {
			try {
				addURL(new File(element).toURI().toURL(), classLoader);
				getLog().info("- " + new File(element).toURI().toURL());
			} catch (Exception e) {
				getLog().error(e);
			}
		}
	}
	
    protected List<File> toFileList(FileSet scriptSet) throws IOException {
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

	protected abstract List<E> getScripts(List<File> files);


}
