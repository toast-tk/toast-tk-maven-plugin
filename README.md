# Toast TK Maven Plugin

This plugin downloads scenario files and uploads reusable sentences to toast-tk-webapp. 
It helps collecting scenarios for local execution.
How to use the Toast Tk Maven Plugin can be found on the [example project(https://github.com/toast-tk/toast-tk-example).

## Goals overview 
* generates-sources:download to download scenarios related to the user token.
* install:upload to push the sentences you want to re-use on the webapp.

## POM configuration example

* Include maven snapshot repository
```
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

* Plugin configuration

```
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

# Contribution

Toast TK is a young ![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=103) project.  

For contribution rules and guidelines, See [CONTRIBUTING.md](https://github.com/toast-tk/toast-tk-engine/blob/snapshot/CONTRIBUTING.md)

If you'd like to help, [get in touch](https://gitter.im/toast-tk/toast-tk-engine) and let us know how you'd like to help. We love contributors!! 

# Licence
See [Toast-tk Apache License 2.0](https://github.com/toast-tk/toast-tk-engine/blob/snapshot/LICENSE.md)
