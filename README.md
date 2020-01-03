# CurseAPI-Minecraft

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Build](https://jitci.com/gh/TheRandomLabs/CurseAPI-Minecraft/svg)](https://jitci.com/gh/TheRandomLabs/CurseAPI-Minecraft)
[![Dependabot](https://badgen.net/dependabot/TheRandomLabs/CurseAPI-Minecraft/?icon=dependabot)](https://dependabot.com/)

[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/TheRandomLabs/CurseAPI-Minecraft.svg)](http://isitmaintained.com/project/TheRandomLabs/CurseAPI-Minecraft "Average time to resolve an issue")

<!-- [![Maven Central](https://img.shields.io/maven-central/v/com.therandomlabs.curseapi.minecraft/curseapi-minecraft.svg?style=shield)](https://maven-badges.herokuapp.com/maven-central/com.therandomlabs.curseapi.minecraft/curseapi-minecraft/)

[comment]: # [![Javadoc](https://javadoc.io/badge/com.therandomlabs.curseapi.minecraft/curseapi-minecraft.svg?color=blue)](https://javadoc.io/doc/com.therandomlabs.curseapi.minecraft/curseapi-minecraft)-->

An extension of [CurseAPI](https://github.com/TheRandomLabs/CurseAPI) for parsing CurseForge
modpack manifests and for performing more Minecraft-specific operations.

All public-facing code is documented with Javadoc and (mostly) tested with JUnit.

## Usage

Before using CurseAPI-Minecraft, `CurseAPIMinecraft#initialize()` should be called so that
CurseAPI-Minecraft can perform any necessary initialization and register itself with CurseAPI.

* `MCVersion` represents a Minecraft version supported by CurseForge.
`MCVersion` instances can be retrieved by accessing the constants and methods in the
`MCVersions` class.
* `MCVersionGroups` contains `CurseGameVersionGroup` constants that represent Minecraft version
groups.
* `CurseModpack#fromJSON(String)` and `CurseModpack#fromJSON(Path)` can be used to parse CurseForge
modpack manifest JSONs.
* `CurseModpack#createEmpty()` can be used to create a new `CurseModpack` instance.
* `CurseModpack` instances can be converted back to JSONs by calling `CurseModpack#toJSON()` or
`CurseModpack#toJSON(Path)`.

## Using with Gradle

To use CurseAPI-Minecraft with
[CurseAPI](https://github.com/TheRandomLabs/CurseAPI#using-with-gradle),
add this to your dependencies:

```groovy
api "com.github.TheRandomLabs:CurseAPI-Minecraft:master-SNAPSHOT"
```
