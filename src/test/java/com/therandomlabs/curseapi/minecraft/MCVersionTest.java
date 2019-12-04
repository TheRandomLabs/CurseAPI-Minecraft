package com.therandomlabs.curseapi.minecraft;

import static org.assertj.core.api.Assertions.assertThat;

import com.therandomlabs.curseapi.CurseException;
import org.junit.jupiter.api.Test;

public class MCVersionTest {
	@Test
	public void mcVersionsShouldNotBeEmpty() throws CurseException {
		assertThat(MCVersions.getAll()).isNotEmpty();
	}
}
