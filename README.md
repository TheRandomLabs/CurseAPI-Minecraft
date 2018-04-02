# CurseAPI-Minecraft
An extension of CurseAPI that deals with the parsing of Curse manifests. Used by several other
projects relating to modpack management and installation.

Example Gradle buildscript:

	apply plugin: "java"

	repositories {
		jcenter()
		maven {
			url "https://jitpack.io"
		}
	}

	dependencies {
		compile "com.github.TheRandomLabs:CurseAPI-Minecraft:master-SNAPSHOT"
	}

	task sourcesJar(type: Jar, dependsOn: classes) {
		classifier = "sources"
		from sourceSets.main.allSource
	}

	artifacts {
		archives sourcesJar
	}

	jar {
		manifest {
			attributes "Main-Class": "com.example.MainClass"
		}

		from {
			configurations.compile.collect {
				it.isDirectory() ? it : zipTree(it)
			}
		}
	}


Features:
* The extended Curse manifest format. You can see an example
[here](https://github.com/TheRandomLabs/LightChocolate) at manifest.json
* Parsing Curse manifests
* Manifest comparisons (which can be used to
[create changelogs](https://github.com/TheRandomLabs/ChangelogGenerator))
* Basic event handling

Planned features:
* Move CAManifest and ForgeInstaller to separate projects/repos
* Documentation and a wiki
