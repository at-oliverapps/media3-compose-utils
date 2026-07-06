pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("shared") { from(files("gradle/shared.versions.toml")) }
        create("mobile") { from(files("gradle/mobile.versions.toml")) }
        create("watch") { from(files("gradle/watch.versions.toml")) }
    }
}

rootProject.name = "Media3 Compose Sample"
include(":mobile")
include(":watch")
include(":shared")
