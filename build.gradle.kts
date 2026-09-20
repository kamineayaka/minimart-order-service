import org.gradle.api.tasks.bundling.Jar

plugins {
	java
	id("org.springframework.boot") version "4.0.8"
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
		mavenBom("com.minimart:minimart-bom:0.1.0")
	}
}

dependencies {
	implementation("com.minimart:minimart-member-api")
	implementation("com.minimart:minimart-product-api")
	implementation("com.minimart:minimart-payment-api")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
	implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
	implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.wiremock:wiremock-standalone")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Jar>("jar").configure {
	enabled = false
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}
