package com.therandomlabs.curseapi.minecraft.modpack.installer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import com.therandomlabs.curseapi.minecraft.modpack.manifest.ExtendedCurseManifest;
import com.therandomlabs.utils.io.NIOUtils;
import com.therandomlabs.utils.io.ZipFile;

public final class ModpackZipper {
	private ModpackZipper() {}

	public static void zip(String directory, String zip, String... extensionsWithVariables)
			throws IOException {
		zip(Paths.get(directory), Paths.get(zip), extensionsWithVariables);
	}

	public static void zip(Path directory, Path zip, String... extensionsWithVariables)
			throws IOException {
		final Path manifestPath = directory.resolve("manifest.json");
		if(!Files.exists(manifestPath)) {
			throw new FileNotFoundException("A manifest is required: " + manifestPath.toString());
		}

		final ExtendedCurseManifest manifest = ExtendedCurseManifest.from(manifestPath);
		final Path overrides = directory.resolve(manifest.overrides);

		try(final ZipFile zipFile = new ZipFile(zip, true)) {
			Files.walkFileTree(overrides, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
						throws IOException {
					boolean shouldReplaceVariables = false;
					for(String extension : extensionsWithVariables) {
						if(NIOUtils.getName(file).endsWith('.' + extension)) {
							shouldReplaceVariables = true;
							break;
						}
					}

					if(shouldReplaceVariables) {
						ModpackInstaller.replaceVariablesAndCopy(file, zipFile.getEntryAsPath(file),
								manifest);
					} else {
						zipFile.addEntry(file, file);
					}

					return FileVisitResult.CONTINUE;
				}
			});

			zipFile.addEntry(manifestPath, "manifest.json");
		}

		ModpackInstaller.deleteTemporaryFiles();
	}
}
