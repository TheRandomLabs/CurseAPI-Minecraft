package com.therandomlabs.curseapi.minecraft.caformat;

/*
 * Variables are parsed
 * - Custom variables can set different values in the manifests
 *
 * Preprocessors are parsed
 * - Preprocessors can directly modify the code
 *
 * Groups are parsed
 *
 * Lines are pruned
 * - Comments and empty lines are removed
 * - Comments at the ends of lines
 *
 * Mods and additional files are parsed
 * - Any line that doesn't start with the mod removal character or the postprocessor character
 * - Mod data stored in a private ModData class
 *   - Boolean 'isAlternative' - defines whether it should be stored in alternativeMods or files
 *   - Side should determine whether it should be stored in files or serverOnlyMods
 * - Additional files are stored as FileInfos
 * - URLs stored in separate list
 *
 * URLs are converted to ModDatas
 * - Multithreaded
 *
 * Mods are pruned
 * - Duplicates are removed
 * - Removed mods are parsed
 *
 * Postprocessors are run
 * - Postprocessors can only replace their own line
 *
 * Mods and additional files are parsed and pruned again
 *
 * ModDatas are converted to Mods
 * - Multithreaded
 *
 * Manifest is created
 * - Variable.apply is called for each variable
 * - Mods are put into files, serverOnlyMods and alternativeMods
 * - Additional files
 * - ExtendedCurseManifest.sort
 */

public class CAManifest {
	private CAManifest() {}


}
