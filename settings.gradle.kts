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
		mavenLocal()
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/spring")
		mavenCentral()
		val githubToken = providers.environmentVariable("GITHUB_TOKEN")
		if (githubToken.isPresent) {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/kamineayaka/minimart-infra")
				credentials {
					username = providers.environmentVariable("GITHUB_ACTOR").orElse("github").get()
					password = githubToken.get()
				}
			}
		}
	}
}

rootProject.name = "order-service"

listOf(file("../minimart-infra"), file("/workspace/minimart-infra"))
	.firstOrNull { it.resolve("settings.gradle.kts").isFile }
	?.let { includeBuild(it) }
