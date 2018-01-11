package com.therandomlabs.curseapi.minecraft.modpack;

public class FileInfo implements Cloneable {
	public String path;
	public Side side;

	public FileInfo() {}

	public FileInfo(String path, Side side) {
		this.path = path;
		this.side = side;
	}

	@Override
	public FileInfo clone() {
		return new FileInfo(path, side);
	}
}
