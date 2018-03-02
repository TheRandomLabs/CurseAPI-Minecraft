# CurseAPI-Minecraft
An extension of CurseAPI that deals with the creation, parsing, and installation of Minecraft
modpacks. Ironically, CurseAPI-Minecraft is larger than CurseAPI.

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


Features (not all done yet - this is way more WIP than CurseAPI):
* The CurseAPI Manifest Format (CAManifest), which CurseAPI can generate Curse manifests from
I haven't written any documentation for it yet, but you can see an example
[here](https://github.com/TheRandomLabs/LightChocolate) at manifest.txt
* The extended Curse manifest format. You can see an example at the above link (look in manifest.json)
* Modpack installation, which doesn't redownload files that are already there (or in other places)
* Parsing Curse manifests
* Manifest comparisons (which can be used to
[create changelogs](https://github.com/TheRandomLabs/ChangelogGenerator))
* Replace variables in configuration files (e.g. modpack name, version) -
both in installation and in packaging
* Basic event handling

Planned features:
* Documentation and a wiki

Modpack installation example:

	Logging.<ToggleableLogger>getLogger().disableDebug();
	new ModpackInstaller().
			installTo("LightChocolate Server").
			side(Side.SERVER).
			install(ModpackInstaller.LIGHTCHOCOLATE);
