package com.therandomlabs.curseapi.minecraft;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import com.therandomlabs.utils.collection.CollectionUtils;
import com.therandomlabs.utils.collection.TRLList;

public class FileInfo implements Cloneable, Comparable<FileInfo>, Serializable {
	private static final long serialVersionUID = -3418209854129685785L;

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

	@Override
	public int compareTo(FileInfo file) {
		return path.toLowerCase(Locale.ENGLISH).compareTo(file.path.toLowerCase(Locale.ENGLISH));
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

	public static TRLList<Path> getExcludedPaths(FileInfo[] files, Side side) {
		final String[] paths;
		if(side == Side.CLIENT) {
			paths = getPaths(files, Side.SERVER);
		} else if(side == Side.SERVER) {
			paths = getPaths(files, Side.CLIENT);
		} else {
			paths = new String[0];
		}
		return CollectionUtils.convert(new TRLList<>(paths), Paths::get);
	}
}
