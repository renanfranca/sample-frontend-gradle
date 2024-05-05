plugins {
  java
  jacoco
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
  // jhipster-needle-gradle-implementation-dependencies
  // jhipster-needle-gradle-compile-dependencies
  // jhipster-needle-gradle-runtime-dependencies
  testImplementation(libs.junit.engine)
  testImplementation(libs.junit.params)
  testImplementation(libs.assertj)
  testImplementation(libs.mockito)
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
