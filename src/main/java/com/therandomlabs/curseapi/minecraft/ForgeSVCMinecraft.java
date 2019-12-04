package com.therandomlabs.curseapi.minecraft;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

interface ForgeSVCMinecraft {
	@GET("api/v2/minecraft/version")
	Call<List<MCVersion>> getVersions();
}
