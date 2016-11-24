package io.toast.tk.maven.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;

import io.toast.tk.core.rest.RestUtils;

@Mojo(name = "download", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class DownloadScriptsMojo extends AbstractMojo {

	@Parameter(defaultValue = "${basedir}/src/main/resources/settings", required = true)
	private File outputResourceDirectory;

	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private String buildDir;

	@Parameter(required = false, defaultValue = "toast_adapters.json")
	private String adaptersFileName;

	@Parameter(required = true, alias = "apiKey")
	private String apiKey;

	@Parameter(required = false, alias = "output-package")
	private String outputPackage;

	@Parameter(required = false, alias = "includePatternSentences", defaultValue = "false")
	private Boolean includePatternSentences;

	@Parameter(required = true, alias = "webAppUrl", defaultValue = "9000")
	private String host;

	private static final String TAOST_OFFLINE_WEB_REPO_FILE = "toast_web_repository.md";
	private static final String TAOST_OFFLINE_SWING_REPO_FILE = "toast_swing_repository.md";
	private static final String SCENARIO_EXTENSION = ".md";
	private static final String SCENARIO_FOLDER = "scenarios";

	public void execute() throws MojoExecutionException {
		if (outputResourceDirectory != null && !outputResourceDirectory.exists()) {
			outputResourceDirectory.mkdir();
		} else if (outputResourceDirectory == null) {
			throw new MojoExecutionException("Can't find /src/main/resources directory, please create it!");
		}
		getLog().info("Toast Tk Maven Plugin - Files will be generated in package " + outputPackage);
		getLog().info("Toast Tk Maven Plugin - Connecting to -> " + host);
		try {
			String repository = RestUtils.downloadRepository(host + "/repository/swing/" + apiKey);
			File scenarioImplFile = new File(outputResourceDirectory, TAOST_OFFLINE_SWING_REPO_FILE);
			writeFile(scenarioImplFile, repository);

			String webrepository = RestUtils.downloadRepository(host + "/repository/web/" + apiKey);
			File destFile = new File(outputResourceDirectory, TAOST_OFFLINE_WEB_REPO_FILE);
			writeFile(destFile, webrepository);

			downloadScenarii(host + "/scenario/wiki/" + apiKey);
			StringBuilder builder = new StringBuilder();
			try {
				File driverJson = new File(outputResourceDirectory, adaptersFileName);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				String v = builder.toString().replace(",]}", "]}");
				JsonElement je = jp.parse(v);
				String prettyJsonString = gson.toJson(je);
				writeFile(driverJson, prettyJsonString);
			} catch (MojoExecutionException e) {
				getLog().error(e);
			}
			getLog().info("Toast Tk Maven Plugin - Update completed !");
		} catch (Exception e) {
			getLog().error("Toast Tk Maven Plugin - Update cancelled !");
			getLog().error(e);
		}
	}

	private void downloadScenarii(String uri) throws MojoExecutionException {
		Client httpClient = Client.create();
		String jsonResponse = RestUtils.getJsonResponseAsString(uri, httpClient);
		JSONArray jsonResult;
		File scenariiOutputFolder = new File(
				outputResourceDirectory.getAbsolutePath() 
				+ File.separator + SCENARIO_FOLDER);
		scenariiOutputFolder.mkdir();
		try {
			jsonResult = new JSONArray(jsonResponse);
			getLog().info("Copying " + jsonResult.length() + " scenarios");
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < jsonResult.length(); i++) {
				String scenarioHeader = "#include " + File.separator + SCENARIO_FOLDER
										+ File.separator + TAOST_OFFLINE_WEB_REPO_FILE+ "\n\n";
				String scenario = scenarioHeader + jsonResult.getString(i);
				Pattern pattern1 = Pattern.compile("(scenario driver):(\\w*)");
				Pattern pattern2 = Pattern.compile("(\\|\\| scenario \\|\\| )(\\w*)( \\|\\|)");
				Pattern pattern3 = Pattern.compile("(Name):(\\w*)");
				Matcher matcher = pattern1.matcher(scenario);
				matcher = pattern3.matcher(scenario);
				String scenarioName = "";
				while (matcher.find()) {
					scenarioName = matcher.group(2);
				}
				builder.append(scenario);
				File scenarioFile = new File(scenariiOutputFolder, scenarioName + SCENARIO_EXTENSION);
				writeFile(scenarioFile, scenario);
			}
		} catch (JSONException e) {
			getLog().error(e);
		}
	}

	private void writeFile(File file, String content) throws MojoExecutionException {
		BufferedWriter out = null;
		try(OutputStream stream = new FileOutputStream(file)) {
			out = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
			out.write(content);
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoExecutionException("Error creating file " + file, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
					getLog().error(e);
				}
			}
		}
	}
}
