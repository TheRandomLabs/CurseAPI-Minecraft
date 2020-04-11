/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.curseapi.minecraft.modpack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.BasicCurseFile;
import com.therandomlabs.curseapi.minecraft.MCVersions;
import com.therandomlabs.utils.io.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CurseModpackTest {
	@Test
	public void invalidManifestShouldThrowCurseException(@TempDir Path tempDirectory)
			throws IOException {
		final Path manifest = tempDirectory.resolve("manifest.json");
		Files.write(manifest, Collections.singletonList("{\"invalid\":\"manifest\"}"));
		assertThatThrownBy(() -> CurseModpack.fromJSON(manifest)).
				isInstanceOf(CurseException.class).
				hasMessageContaining("Invalid modpack manifest");
	}

	@Test
	public void atm4ShouldBeValid(@TempDir Path tempDirectory) throws CurseException, IOException {
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
			assertThat(modpack.toJSON()).isNotEmpty();
		}
	}

	@Test
	public void createdModpackShouldBeValid() throws CurseException {
		final CurseModpack modpack = CurseModpack.createEmpty();

		modpack.mcVersion(MCVersions.V1_14_4);
		modpack.forgeVersion("1.14.4-28.1.107");
		modpack.name("Test Modpack");
		modpack.version("1.0.0");
		modpack.author("CurseAPI-Minecraft");
		modpack.files(Collections.singletonList(new BasicCurseFile.Immutable(285612, 2803612)));

		final String json = modpack.toJSON();
		assertThat(json).isNotEmpty();
		System.out.println(json);

		final CurseModpack readModpack = CurseModpack.fromJSON(json);
		assertThat(readModpack).isNotNull();
		assertThat(readModpack.mcVersion()).isEqualTo(MCVersions.V1_14_4);
		assertThat(readModpack.forgeVersion()).isEqualTo("1.14.4-28.1.107");
		assertThat(readModpack.name()).isEqualTo("Test Modpack");
		assertThat(readModpack.version()).isEqualTo("1.0.0");
		assertThat(readModpack.author()).isEqualTo("CurseAPI-Minecraft");
		assertThat(readModpack.files()).isNotEmpty();
		assertThat(readModpack.files().first()).isEqualTo(
				new BasicCurseFile.Immutable(285612, 2803612)
		);
		assertThat(readModpack.toJSON()).isEqualTo(json);
	}
}
