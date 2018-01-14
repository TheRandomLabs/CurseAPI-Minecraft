# CurseAPI-Minecraft
An extension of CurseAPI that deals with the creation, parsing, and installation of Minecraft modpacks.

Features:
* The CurseAPI Manifest Format, which CurseAPI can generate Curse manifests from. I haven't written any documentation for it yet, but you can see an example [here](https://github.com/TheRandomLabs/LightChocolate)
* The extended Curse manifest format
* Modpack installation, which doesn't redownload files that are already there (or in other places)
* Parsing Curse manifests
* Changelog generation with mod-specific support (feed it an old manifest and a new manifest, and it will create a Changelog object, which comes with a toString method)
* Replacing "variables" in configuration files - both in installation and creating a zip file
* Basic event handling
* And more.

Planned features:
* Documentation and a wiki.

Modpack installation example:

	Logging.<ToggleableLogger>getLogger().disableDebug();
	new ModpackInstaller().
			installTo("LightChocolate Server").
			side(Side.SERVER).
			install(ModpackInstaller.LIGHTCHOCOLATE);
