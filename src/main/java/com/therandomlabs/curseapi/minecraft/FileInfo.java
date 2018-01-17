package com.therandomlabs.curseapi.minecraft;

import java.util.List;
import com.therandomlabs.utils.collection.TRLList;

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

	public static String[] getPaths(FileInfo[] files, Side side) {
		final List<String> paths = new TRLList<>(files.length);

		for(FileInfo file : files) {
			if(file.side == Side.CLIENT) {
				if(side == Side.CLIENT || side == Side.BOTH) {
					paths.add(file.path);
				}
				continue;
			}

			if(file.side == Side.SERVER) {
				if(side == Side.SERVER || side == Side.BOTH) {
					paths.add(file.path);
				}
				continue;
			}

			paths.add(file.path);
		}

		return paths.toArray(new String[0]);
	}

	public static TRLList<String> getExcludedPaths(FileInfo[] files, Side side) {
		if(side == Side.CLIENT) {
			return new TRLList<>(getPaths(files, Side.SERVER));
		}
		if(side == Side.SERVER) {
			return new TRLList<>(getPaths(files, Side.CLIENT));
		}
		return new TRLList<>();
	}
}
