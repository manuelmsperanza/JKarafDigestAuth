# JKarafDigestAuth
# Create a new project
	mvn archetype:generate -Dfilter="org.apache.karaf.archetypes:karaf-bundle-archetype" -DgroupId="com.hoffnungland" -DartifactId=JKarafDigestAuth -Dpackage="com.hoffnungland.jKarafDigestAuth" -Dversion="0.0.1-SNAPSHOT"
# Build settings
## Change archetype and name
	
	<artifactId>jkarafdigestauth</artifactId>
	<name>Apache Karaf :: Digest Auth :: Passthrough</name>

## Add dependencies

    <dependencies>
      <dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>javax.servlet-api</artifactId>
			<version>3.1.0</version>
			<scope>provided</scope>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.apache.karaf.features/standard -->
		<dependency>
			<groupId>org.apache.karaf.features</groupId>
			<artifactId>standard</artifactId>
			<version>4.3.2</version>
			<type>pom</type>
			<scope>provided</scope>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-api -->
		<dependency>
		    <groupId>org.slf4j</groupId>
		    <artifactId>slf4j-api</artifactId>
		    <version>1.7.31</version>
            <scope>provided</scope>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient-osgi -->
		<dependency>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpclient-osgi</artifactId>
			<version>4.5.13</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.apache.httpcomponents/httpcore-osgi -->
		<dependency>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpcore-osgi</artifactId>
			<version>4.4.14</version>
		</dependency>
    </dependencies>

## Configure plugins
### maven-bundle-plugin

	<_wab>src/main/webapp/</_wab>
	<Web-ContextPath>digest-passthrough</Web-ContextPath>

# Download [Karaf](http://karaf.apache.org/download.html)

# Setup Karaf for http and war

	feature:install http
	feature:install http-whiteboard
	feature:install war

# Install dependencies on Karaf


	install -s mvn:org.apache.httpcomponents/httpcore-osgi/4.4.14
	install -s mvn:org.apache.httpcomponents/httpclient-osgi/4.5.13

	
# Karaf logger configuration

Add the below entry to $KARAF_HOME\etc\org.ops4j.pax.logging.cfg

	# com.hoffnungland logger
	log4j2.logger.hoffnungland.name = com.hoffnungland
	log4j2.logger.hoffnungland.level = TRACE
	log4j2.logger.hoffnungland.additivity = false
	log4j2.logger.hoffnungland.appenderRef.Console.ref = ConsoleCustom
	
	log4j2.appender.consoleCustom.name = ConsoleCustom
	log4j2.appender.consoleCustom.type = Console
	log4j2.appender.consoleCustom.layout.type = PatternLayout
	log4j2.appender.consoleCustom.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p [%c{1}::%M %L]  %m%n

# Test it

Run a Soap request to http://localhost:8181/digest-passthrough/passthrough/jhonsmith

# add .gitignore to mandatory empty directory

	# Ignore everything in this directory
	*
	# Except this file
	!.gitignore

# Configure the Package Clean UP Automation with GitHub Action

The Action run during the release phase of package (or you can run it manually).
Leave only the latest package version into the repository.
Create the .github/workflows/cleanupPackages.yml file.


# Support

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/K3K441XSO)

[![Support the development of these features](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/donate/?business=VU48PTCSF93A2&no_recurring=0&item_name=Support+the+development+of+these+features.&currency_code=USD)