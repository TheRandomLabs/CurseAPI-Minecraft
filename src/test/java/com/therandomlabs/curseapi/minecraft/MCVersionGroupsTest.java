package com.therandomlabs.curseapi.minecraft;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class MCVersionGroupsTest {
	@Test
	public void mcVersionGroupsShouldContainCorrectVersions() {
		assertThat(MCVersionGroups.V1_10.containsAnyStrings("1.10.1")).isTrue();
		assertThat(MCVersionGroups.V1_7.containsAny(MCVersions.V1_7_2)).isTrue();
	}
}
