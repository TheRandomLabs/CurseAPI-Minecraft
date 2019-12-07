package com.therandomlabs.curseapi.minecraft;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.SortedSet;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MCVersionTest {
	@Test
	public void mcVersionsShouldNotBeEmpty() throws CurseException {
		final Optional<SortedSet<MCVersion>> optionalVersions =
				CurseAPI.gameVersions(CurseAPIMinecraft.MINECRAFT_ID);
		assertThat(optionalVersions).isPresent();
		assertThat(MCVersions.getAll()).isNotEmpty().isEqualTo(optionalVersions.get());
	}

	@Test
	public void mcVersionComparisonsShouldBeCorrect() {
		assertThat(MCVersions.V1_14_4.newerThan(MCVersions.V1_14_3)).isTrue();
		assertThat(MCVersions.V1_0.olderThan(MCVersions.V1_2_1)).isTrue();
		assertThat(MCVersions.V1_1.olderThan(MCVersions.V1_0)).isFalse();
	}

	@BeforeAll
	public static void setup() {
		CurseAPIMinecraft.initialize();
	}
}
