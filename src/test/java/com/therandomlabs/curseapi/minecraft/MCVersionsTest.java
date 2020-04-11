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

package com.therandomlabs.curseapi.minecraft;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.NavigableSet;
import java.util.Optional;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MCVersionsTest {
	@Test
	public void mcVersionsShouldNotBeEmpty() throws CurseException {
		final Optional<NavigableSet<MCVersion>> optionalVersions =
				CurseAPI.gameVersions(CurseAPIMinecraft.MINECRAFT_ID);
		assertThat(optionalVersions).isPresent();
		assertThat(MCVersions.getAll()).isNotEmpty().isEqualTo(optionalVersions.get());
	}

	@Test
	public void mcVersionComparisonsShouldBeCorrect() {
		MCVersions.getAll();
		assertThat(MCVersions.V1_14_4.newerThan(MCVersions.V1_14_3)).isTrue();
		assertThat(MCVersions.V1_8_SNAPSHOT.olderThan(MCVersions.V1_8)).isTrue();
		assertThat(MCVersions.V1_0.olderThan(MCVersions.V1_2_1)).isTrue();
		assertThat(MCVersions.V1_1.olderThan(MCVersions.V1_0)).isFalse();
	}

	@Test
	public void mcVersionsShouldBeValid() {
		assertThat(MCVersions.V1_0.gameID()).isEqualTo(CurseAPIMinecraft.MINECRAFT_ID);
		assertThat(MCVersions.V1_0.toString()).isEqualTo(MCVersions.V1_0.versionString());
	}

	@BeforeAll
	public static void setup() {
		CurseAPIMinecraft.initialize();
	}
}
