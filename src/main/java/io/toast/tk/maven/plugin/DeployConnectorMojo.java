package io.toast.tk.maven.plugin;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.toast.tk.core.rest.HttpRequest;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.google.gson.Gson;

import io.toast.tk.core.annotation.Action;
import io.toast.tk.core.annotation.ActionAdapter;
import io.toast.tk.core.rest.RestUtils;
import io.toast.tk.maven.plugin.data.ActionAdapterDescriptor;
import io.toast.tk.maven.plugin.data.ActionAdapterDescriptorLine;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;

@Mojo(name = "upload", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE)
public class DeployConnectorMojo extends AbstractMojo {

    @Parameter(required = true, alias = "webAppUrl", defaultValue = "9000")
    private String host;
    
    @Parameter(required = true, alias = "apiKey")
    private String apiKey;

    @Parameter(required = true, defaultValue = "${project}", readonly = true)
    MavenProject project;

    @Override
    public void execute()
            throws MojoExecutionException {
        getLog().info("Toast Tk Maven Plugin - Files will be posted to: " + host);
        try {
            publishAvailableSentencesFromClass();
        } catch (Exception e) {
            getLog().error(e);
        }
        getLog().info("Toast Tk Maven Plugin - deploy completed !");
    }

    private void publishAvailableSentencesFromClass()
            throws NotFoundException {
        ClassPool cp = initClassPath();
        File file = new File(project.getBuild().getOutputDirectory());
        Iterator<File> iterateFiles = FileUtils.iterateFiles(file, new String[]{"class"}, true);
        while (iterateFiles.hasNext()) {
            try {
                processClassAndPostConnector(cp, iterateFiles.next());
            } catch (Exception e) {
                getLog().error(e);
            }
        }
    }

    private ClassPool initClassPath()
            throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        URLClassLoader contextClassLoader = (URLClassLoader) project.getClass().getClassLoader();
        cp.insertClassPath(new LoaderClassPath(contextClassLoader));
        cp.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        cp.appendClassPath(project.getBuild().getOutputDirectory());
        return cp;
    }

    private void processClassAndPostConnector(ClassPool cp, File file)
            throws IOException, NotFoundException, ClassNotFoundException {
        final List<ActionAdapterDescriptorLine> sentences = new ArrayList<>();

        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        ClassFile classFile = new ClassFile(dataInputStream);

        CtClass cc = cp.get(classFile.getName());
        if (cc.hasAnnotation(ActionAdapter.class) && !Modifier.isAbstract(cc.getModifiers())) {
            ActionAdapter adapter = (ActionAdapter) cc.getAnnotation(ActionAdapter.class);
            String adapterKind = adapter.value().name();
            String adapterName = adapter.name();
            CtMethod[] methods = cc.getMethods();
            for (CtMethod ctMethod : methods) {
                if (ctMethod.hasAnnotation(Action.class)) {
                    sentences.add(buildActionLineDescriptor(adapterKind, adapterName, ctMethod));
                }
            }
            getLog().info("Adapter: " + adapterName + " -> Posting " + sentences.size() + " actions");
            postConnector(sentences);
        }
    }

    private ActionAdapterDescriptorLine buildActionLineDescriptor(
            String adapterKind,
            String adapterName,
            CtMethod ctMethod)
            throws ClassNotFoundException {
        Action action = (Action) ctMethod.getAnnotation(Action.class);
        return new ActionAdapterDescriptorLine(adapterName, adapterKind, action.description(), action.action());
    }

    private void postConnector(final List<ActionAdapterDescriptorLine> sentences) {
        Gson gson = new Gson();
        ActionAdapterDescriptor descriptor = new ActionAdapterDescriptor(project.getName(), sentences);
        String postUri = host + "/actionadapter";
        String json = gson.toJson(descriptor);
        HttpRequest request = HttpRequest.Builder.create()
                                            .uri(postUri).json(json)
                                            .withKey(apiKey).build();
        RestUtils.post(request);
    }

}
