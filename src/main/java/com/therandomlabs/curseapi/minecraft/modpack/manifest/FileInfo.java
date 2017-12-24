package com.therandomlabs.curseapi.minecraft.modpack.manifest;

import com.therandomlabs.curseapi.minecraft.modpack.FileSide;

public class FileInfo implements Cloneable {
	public String path;
	public FileSide side;

	public FileInfo() {}

	public FileInfo(String path, FileSide side) {
		this.path = path;
		this.side = side;
	}

	@Override
	public FileInfo clone() {
		return new FileInfo(path, side);
	}
}
