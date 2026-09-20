pluginManagement {
	repositories {
		maven("https://maven.aliyun.com/repository/gradle-plugin")
		maven("https://maven.aliyun.com/repository/public")
		gradlePluginPortal()
		mavenCentral()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/spring")
		mavenCentral()
	}
}

rootProject.name = "order-service"
