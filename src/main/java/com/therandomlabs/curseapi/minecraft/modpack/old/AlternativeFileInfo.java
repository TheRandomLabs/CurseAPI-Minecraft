package com.therandomlabs.curseapi.minecraft.modpack.old;

public final class AlternativeFileInfo implements Cloneable {
	public String title = "Unknown";
	public int projectID;
	public int fileID;
	public FileType type = FileType.NORMAL;
	public String[] relatedFiles = new String[0];

	@Override
	public ModInfo clone() {
		final ModInfo info = new ModInfo();

		info.title = title;
		info.projectID = projectID;
		info.fileID = fileID;
		info.type = type;
		info.relatedFiles = relatedFiles.clone();

		return info;
	}

	static AlternativeFileInfo[] fromArray(ModInfo[] files) {
		final AlternativeFileInfo[] alternatives = new AlternativeFileInfo[files.length];

		for(int i = 0; i < files.length; i++) {
			final AlternativeFileInfo info = new AlternativeFileInfo();

			info.title = files[i].title;
			info.projectID = files[i].projectID;
			info.fileID = files[i].fileID;
			info.type = files[i].type;
			info.relatedFiles = files[i].relatedFiles;

			alternatives[i] = info;
		}

		return alternatives;
	}
}
