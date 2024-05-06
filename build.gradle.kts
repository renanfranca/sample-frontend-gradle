import com.github.gradle.node.npm.task.NpmTask
// jhipster-needle-gradle-imports

plugins {
  java
  jacoco
  alias(libs.plugins.sonarqube)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.node.gradle)
  // jhipster-needle-gradle-plugins
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

jacoco {
  toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
  dependsOn("test", "integrationTest")
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
  executionData.setFrom(fileTree(buildDir).include("**/jacoco/test.exec", "**/jacoco/integrationTest.exec"))
}

tasks.jacocoTestCoverageVerification {
  dependsOn("jacocoTestReport")
  violationRules {

      rule {
          element = "CLASS"

          limit {
              counter = "LINE"
              value = "COVEREDRATIO"
              minimum = "1.00".toBigDecimal()
          }

          limit {
              counter = "BRANCH"
              value = "COVEREDRATIO"
              minimum = "1.00".toBigDecimal()
          }
      }
  }
  executionData.setFrom(fileTree(buildDir).include("**/jacoco/test.exec", "**/jacoco/integrationTest.exec"))
}


fun loadSonarProperties(): Map<String, List<String>> {
    val properties = mutableMapOf<String, List<String>>()
    File("sonar-project.properties").forEachLine { line ->
        if (!line.startsWith("#") && line.contains("=")) {
            val (key, value) = line.split("=", limit = 2)
            properties[key.trim()] = value.split(",").map { it.trim() }
        }
    }
    return properties
}

sonarqube {
  properties {
    loadSonarProperties().forEach { (key, value) ->
      property(key, value)
    }
    property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    property("sonar.junit.reportPaths", "build/test-results/test,build/test-results/integrationTest")
  }
}

node {
  version.set("v20.12.2")
  npmVersion.set("10.5.2")
  npmWorkDir.set(file("build"))
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
  description = "Build the frontend project using NPM"
  group = "Build"
  dependsOn("npmInstall")
  npmCommand.set(listOf("run", "build"))
  environment.set(mapOf("APP_VERSION" to project.version.toString()))
}

val testTaskUsingNpm = tasks.register<NpmTask>("testNpm") {
  description = "Test the frontend project using NPM"
  group = "verification"
  dependsOn("npmInstall", "buildNpm")
  npmCommand.set(listOf("run", "test"))
  ignoreExitValue.set(false)
  workingDir.set(projectDir)
  execOverrides {
    standardOutput = System.out
  }
}

tasks.bootJar {
  dependsOn("buildNpm")
  from("build/classes/static") {
      into("BOOT-INF/classes/static") // This will place the content under 'static' directory in the JAR
  }
}

defaultTasks("bootRun")

springBoot {
  mainClass = "com.mycompany.myapp.JhipsterSampleApplicationApp"
}

// jhipster-needle-gradle-plugins-configurations

repositories {
  mavenCentral()
  // jhipster-needle-gradle-repositories
}

group = "com.mycompany.myapp"
version = "0.0.1-SNAPSHOT"

// jhipster-needle-gradle-properties

val profiles = (project.findProperty("profiles") as String? ?: "")
  .split(",")
  .map { it.trim() }
  .filter { it.isNotEmpty() }
// jhipster-needle-profile-activation

dependencies {
  implementation(platform(libs.spring.boot.dependencies))
  implementation(libs.spring.boot.starter)
  implementation(libs.spring.boot.configuration.processor)
  implementation(libs.commons.lang3)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.web)
  // jhipster-needle-gradle-implementation-dependencies
  // jhipster-needle-gradle-compile-dependencies
  // jhipster-needle-gradle-runtime-dependencies
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.reflections)

  // jhipster-needle-gradle-test-dependencies
}

// jhipster-needle-gradle-free-configuration-blocks

tasks.test {
  filter {
    includeTestsMatching("**Test*")
    excludeTestsMatching("**IT*")
    excludeTestsMatching("**CucumberTest*")
  }
  useJUnitPlatform()
  finalizedBy("jacocoTestCoverageVerification")
  dependsOn("testNpm")
  // jhipster-needle-gradle-tasks-test
}

val integrationTest = task<Test>("integrationTest") {
  description = "Runs integration tests."
  group = "verification"
  shouldRunAfter("test")
  filter {
    includeTestsMatching("**IT*")
    includeTestsMatching("**CucumberTest*")
    excludeTestsMatching("**Test*")
  }
  useJUnitPlatform()
}
