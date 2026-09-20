import org.gradle.api.tasks.bundling.Jar

plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.minimart"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
		mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2025.1.0.0")
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.3")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
	implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
	implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Jar>("jar").configure {
	enabled = false
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}
