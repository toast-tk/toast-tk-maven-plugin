# Toast TK Maven Plugin

A Maven plugin to downloads scenario and upload reusable sentences to  [toast-tk-webapp](https://github.com/toast-tk/toast-tk-webapp).   
It helps collecting scenarios for local execution.  
How to use the Toast Tk Maven Plugin can be found on the [example project](https://github.com/toast-tk/toast-tk-examples).  

## Goals overview 
* __generates-sources:download__ to download scenarios related to the user token.
* __install:upload__ to push the sentences you want to re-use on the webapp.
* __verify:run__ to execute a set of scripts and displays the execution report.
* __integration-test:report__ to execute a set of test plans and store related reports.

## Examples

* Include maven snapshot repository
```xml
<repository>
	<id>snapshots-repo</id>
	<url>https://oss.sonatype.org/content/repositories/snapshots</url>
	<releases>
		<enabled>false</enabled>
	</releases>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```

* Download Mojo - example

```xml
<plugin>
	<groupId>io.toast-tk</groupId>
	<artifactId>toast-tk-maven-plugin</artifactId>
	<version>0.1.5-SNAPSHOT</version>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>download</goal>
			</goals>
			<configuration>
				<!-- Change with your webapp url:port -->
				<webAppUrl>http://localhost:9000</webAppUrl>
				<!-- Change to the user token (in the user profile) -->
				<apiKey>4fYDkIAL0qrHXNxRKuK8yzUZAgNr9Ywf</apiKey>
			</configuration>
		/execution>
	</executions>
</plugin>
```


* Run Mojo - example
```xml
<plugin>
	<groupId>io.toast-tk</groupId>
	<artifactId>toast-tk-maven-plugin</artifactId>
	<version>0.1.5-SNAPSHOT</version>
	<executions>
		<execution>
			<phase>verify</phase>
			<goals>
				<goal>run</goal>
			</goals>
			<configuration>
				<scripts>
					<fileset>
						<directory>${basedir}/src/main/resources</directory>
						<includes>
							<include>/**/*.*.md</include>
						</includes>
					</fileset>
				</scripts>
				<pluginsDirectory>${basedir}/plugins</pluginsDirectory>
				<outputDirectory>${basedir}/reports</outputDirectory>
			</configuration>
		</execution>
	</executions>
</plugin>
```

* Report Mojo - example
```xml
<plugin>
	<groupId>io.toast-tk</groupId>
	<artifactId>toast-tk-maven-plugin</artifactId>
	<version>0.1.5-SNAPSHOT</version>
	<executions>
		<execution>
			<phase>integration-test</phase>
			<goals>
				<goal>report</goal>
			</goals>
			<configuration>
				<apiKey>4fYDkIAL0qrHXNxRKuK8yzUZAgNr9Ywf</apiKey>
				<mongoHost>localhost</mongoHost>
				<mongoPort>27017</mongoPort>
				<mongoDb>play_db</mongoDb>
				<scripts>
					<fileset>
						<directory>${basedir}/src/main/resources</directory>
						<includes>
							<include>/**/*.*.md</include>
						</includes>
					</fileset>
				</scripts>
				<pluginsDirectory>${basedir}/plugins</pluginsDirectory>
				<outputDirectory>${basedir}/reports</outputDirectory>
			</configuration>
		</execution>
	</executions>
</plugin>
```

# Contribution

Toast TK is a young ![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=103) project.  

For contribution rules and guidelines, See [CONTRIBUTING.md](https://github.com/toast-tk/toast-tk-engine/blob/snapshot/CONTRIBUTING.md)

If you'd like to help, [get in touch](https://gitter.im/toast-tk/toast-tk-engine) and let us know how you'd like to help. We love contributors!! 

# Licence
See [Toast-tk Apache License 2.0](https://github.com/toast-tk/toast-tk-engine/blob/snapshot/LICENSE.md)
