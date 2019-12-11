package com.therandomlabs.curseapi.minecraft.modpack;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Optional;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.minecraft.MCVersions;
import com.therandomlabs.utils.io.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CurseModpackTest {
	@Test
	public void atm4ShouldBeValid(@TempDir Path tempDirectory) throws Exception {
		final Optional<Path> optionalPath =
				CurseAPI.downloadFileToDirectory(316059, 2839369, tempDirectory);
		assertThat(optionalPath).isPresent();

		final Path path = optionalPath.get();
		assertThat(path).isRegularFile();

		try (ZipFile zipFile = new ZipFile(path)) {
			final CurseModpack modpack = CurseModpack.fromJSON(zipFile.getEntry("manifest.json"));
			assertThat(modpack.mcVersion()).isEqualTo(MCVersions.V1_14_4);
			assertThat(modpack.forgeVersion()).isEqualTo("1.14.4-28.1.104");
			assertThat(modpack.name()).isEqualTo("All the Mods 4");
			assertThat(modpack.version()).isEqualTo("0.2.5");
			assertThat(modpack.author()).isEqualTo("ATMTeam");
			assertThat(modpack.files()).isNotEmpty();
			assertThat(modpack.toJSON()).isNotNull();
		}
	}
}
