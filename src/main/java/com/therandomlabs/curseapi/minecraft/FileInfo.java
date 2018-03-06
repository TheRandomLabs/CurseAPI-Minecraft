package com.therandomlabs.curseapi.minecraft;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import com.therandomlabs.utils.collection.TRLList;

public final class FileInfo implements Cloneable, Comparable<FileInfo>, Serializable {
	private static final long serialVersionUID = -3418209854129685785L;

	public String path;
	public Side side;

	public FileInfo() {}

	public FileInfo(String path, Side side) {
		this.path = path;
		this.side = side;
	}

	public static TRLList<String> getExcludedPaths(FileInfo[] files, Side side) {
		final String[] paths;
		if(side == Side.CLIENT) {
			paths = getPaths(files, Side.SERVER);
		} else if(side == Side.SERVER) {
			paths = getPaths(files, Side.CLIENT);
		} else {
			paths = new String[0];
		}
		return new TRLList<>(paths);
	}

	@Override
	public FileInfo clone() {
		try {
			return (FileInfo) super.clone();
		} catch(CloneNotSupportedException ignored) {}

		return null;
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

	@Override
	public String toString() {
		return "[path=\"" + path + ",side=" + side + "]";
	}

	@Override
	public int compareTo(FileInfo file) {
		return path.toLowerCase(Locale.ENGLISH).compareTo(file.path.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public int hashCode() {
		return path.hashCode() + side.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof FileInfo && object.hashCode() == hashCode();
	}
}
