object This {
    const val version = "7.5.0-SNAPSHOT"
    const val artifactId = "testng"
    const val groupId = "org.testng"
    const val description = "Testing framework for Java"
    const val url = "https://testng.org"
    const val scm = "github.com/cbeust/testng"

    // Should not need to change anything below
    const val name = "TestNG"
    const val vendor = name
}

allprojects {
    group = This.groupId
    version = This.version
    apply<MavenPublishPlugin>()
    tasks.withType<Javadoc> {
        excludes.add("org/testng/internal/**")
    }
}

buildscript {
    repositories {
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2") }
    }
    dependencies {
        classpath("org.hibernate.build.gradle:version-injection-plugin:1.0.0")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    // use gradle feature
    // in order to optionally exposed transitive dependency

    registerFeature("ant") {
        usingSourceSet(sourceSets["main"])
    }

    registerFeature("guice") {
        usingSourceSet(sourceSets["main"])
    }

    registerFeature("junit") {
        usingSourceSet(sourceSets["main"])
    }

    registerFeature("yaml") {
        usingSourceSet(sourceSets["main"])
    }
}

repositories {
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2") }
}

plugins {
    java
    `java-library`
    `maven-publish`
    signing
    groovy
    id("org.sonarqube").version("2.8")
    // Improves Gradle Test logging
    // See https://github.com/vlsi/vlsi-release-plugins/tree/master/plugins/gradle-extensions-plugin
    id("com.github.vlsi.gradle-extensions") version "1.74"
}

dependencies {

    listOf("org.apache.ant:ant:1.10.9").forEach {
        "antApi"(it)
    }

    listOf("com.google.inject:guice:4.2.3:no_aop").forEach {
        "guiceApi"(it)
    }

    listOf("junit:junit:4.12").forEach {
        "junitApi"(it)
    }

    listOf("org.yaml:snakeyaml:1.21").forEach {
        "yamlApi"(it)
    }

    listOf("com.google.code.findbugs:jsr305:3.0.1").forEach {
        compileOnly(it)
    }

    listOf("com.beust:jcommander:1.78").forEach {
        api(it)
    }

    listOf("org.webjars:jquery:3.5.1").forEach {
        api(it)
    }

    listOf("org.apache.ant:ant-testutil:1.10.9",
            "org.assertj:assertj-core:3.10.0",
            "org.codehaus.groovy:groovy-all:2.4.7",
            "org.spockframework:spock-core:1.0-groovy-2.4",
            "org.apache-extras.beanshell:bsh:2.0b6",
            "org.mockito:mockito-core:2.12.0",
            "org.jboss.shrinkwrap:shrinkwrap-api:1.2.6",
            "org.jboss.shrinkwrap:shrinkwrap-impl-base:1.2.6",
            "org.xmlunit:xmlunit-assertj:2.8.2").forEach {
        testImplementation(it)
    }
}

tasks.jar {
    manifest {
        attributes(
            // Basic JAR manifest attributes
            "Specification-Title" to This.name,
            "Specification-Version" to project.version,
            "Specification-Vendor" to This.vendor,
            "Implementation-Title" to This.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to This.vendor,
            "Implementation-Vendor-Id" to project.group,
            "Implementation-Url" to This.url,

            // Java 9 module name
            "Automatic-Module-Name" to project.group,

            // BND Plugin instructions (for OSGi)
            "Bundle-Name" to This.name,
            "Bundle-SymbolicName" to project.group,
            "Bundle-Vendor" to This.vendor,
            "Bundle-License" to "https://apache.org/licenses/LICENSE-2.0",
            "Bundle-Description" to This.description,
            "Bundle-Version" to project.version,
            "Import-Package" to """
                "bsh.*;version="[2.0.0,3.0.0)";resolution:=optional",
                "com.beust.jcommander.*;version="[1.7.0,3.0.0)";resolution:=optional",
                "com.google.inject.*;version="[1.2,1.3)";resolution:=optional",
                "junit.framework;version="[3.8.1, 5.0.0)";resolution:=optional",
                "org.junit.*;resolution:=optional",
                "org.apache.tools.ant.*;version="[1.7.0, 2.0.0)";resolution:=optional",
                "org.yaml.*;version="[1.6,2.0)";resolution:=optional",
                "!com.beust.testng",
                "!org.testng.*",
                "!com.sun.*",
                "*"
            """
        )
    }
}

tasks.test {
    useTestNG() {
        suites("src/test/resources/testng.xml")
        listeners.add("org.testng.reporters.FailedInformationOnConsoleReporter")
        testLogging.showStandardStreams = true
        systemProperty("test.resources.dir", "build/resources/test")
        fun passProperty(name: String, default: String? = null) {
            val value = System.getProperty(name) ?: default
            value?.let { systemProperty(name, it) }
        }
        // Default verbose is 0, however, it can be adjusted vi -Dtestng.default.verbose=2
        passProperty("testng.default.verbose", "0")
        // Allow running tests in a custom locale with -Duser.language=...
        passProperty("user.language")
        passProperty("user.country")
        val props = System.getProperties()
        // Pass testng.* properties to the test JVM
        for (e in props.propertyNames() as `java.util`.Enumeration<String>) {
            if (e.startsWith("testng.") || e.startsWith("java")) {
                passProperty(e)
            }
        }
        maxHeapSize = "1500m"
    }
}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io/")
        property("sonar.organization", "testng-team")
        property("sonar.github.repository", "cbeust/testng")
        property("sonar.github.login", "testng-bot")
    }
}

//
// Releases:
// ./gradlew publish (to Sonatype, then go to https://oss.sonatype.org/index.html#stagingRepositories to publish)
//

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

with(publishing) {
    publications {
        create<MavenPublication>("custom") {
            groupId = project.group.toString()
            artifactId = This.artifactId
            version = project.version.toString()
            afterEvaluate {
                from(components["java"])
            }
            suppressAllPomMetadataWarnings()
            artifact(sourcesJar)
            artifact(javadocJar)
            pom {
                name.set(This.artifactId)
                description.set(This.description)
                url.set(This.url)
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://${This.scm}/issues")
                }
                developers {
                    developer {
                        id.set("cbeust")
                        name.set("Cedric Beust")
                        email.set("cedric@beust.com")
                    }
                    developer {
                        id.set("juherr")
                        name.set("Julien Herr")
                        email.set("julien@herr.fr")
                    }
                    developer {
                        id.set("krmahadevan")
                        name.set("Krishnan Mahadevan")
                        email.set("krishnan.mahadevan1978@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://${This.scm}.git")
                    url.set("https://${This.scm}")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = if (project.version.toString().contains("SNAPSHOT"))
                uri("https://oss.sonatype.org/content/repositories/snapshots/") else
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("sonatypeUser")?.toString() ?: System.getenv("SONATYPE_USER")
                password = project.findProperty("sonatypePassword")?.toString() ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
        maven {
            name = "myRepo"
            url = uri("file://$buildDir/repo")
        }
    }
}

with(signing) {
    sign(publishing.publications.getByName("custom"))
}
