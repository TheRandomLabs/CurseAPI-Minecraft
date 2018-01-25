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
import com.therandomlabs.utils.wrapper.Wrapper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public final class ModpackZipper {
	private ModpackZipper() {}

	public static void zip(String directory, String zip, String... extensionsWithVariables)
			throws IOException, ZipException {
		zip(Paths.get(directory), Paths.get(zip), extensionsWithVariables);
	}

	public static void zip(Path directory, Path zip, String... extensionsWithVariables)
			throws IOException, ZipException {
		final Path manifestPath = directory.resolve("manifest.json");
		if(!Files.exists(manifestPath)) {
			throw new FileNotFoundException("A manifest is required: " + manifestPath.toString());
		}

		Files.deleteIfExists(zip);

		final ExtendedCurseManifest manifest = ExtendedCurseManifest.from(manifestPath);
		final Path overrides = directory.resolve(manifest.overrides);
		//Has to be an absolute path or ZipEngine will throw an NPE (if the path is relative)
		final ZipFile zipFile = new ZipFile(zip.toAbsolutePath().toFile());
		final Wrapper<ZipException> exception = new Wrapper<>();

		Files.walkFileTree(overrides, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
					throws IOException {
				final ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
				parameters.setRootFolderInZip(directory.relativize(file.getParent()).toString());
				parameters.setFileNameInZip(directory.relativize(file).toString());
				parameters.setSourceExternalStream(true);

				boolean shouldReplaceVariables = false;
				for(String extension : extensionsWithVariables) {
					if(NIOUtils.getName(file).endsWith('.' + extension)) {
						shouldReplaceVariables = true;
						break;
					}
				}

				if(shouldReplaceVariables) {
					final Path tempPath = ModpackInstaller.tempPath();
					ModpackInstaller.replaceVariablesAndCopy(file, tempPath, manifest);
					file = tempPath;
				}

				try {
					zipFile.addFile(file.toFile(), parameters);
				} catch(ZipException ex) {
					exception.set(ex);
				}

				return FileVisitResult.CONTINUE;
			}
		});

		if(exception.hasValue()) {
			throw exception.get();
		}

		final ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		zipFile.addFile(manifestPath.toFile(), parameters);

		ModpackInstaller.deleteTemporaryFiles();
	}
}
