package com.rfksgn.myprayer.data.remote.api

import com.rfksgn.myprayer.data.remote.dto.QuranApiResponse
import com.rfksgn.myprayer.data.remote.dto.SurahDetailDto
import com.rfksgn.myprayer.data.remote.dto.SurahDto
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiService {

    @GET("api/v2/surat")
    suspend fun getAllSurahs(): QuranApiResponse<List<SurahDto>>

    @GET("api/v2/surat/{number}")
    suspend fun getSurahDetail(
        @Path("number") surahNumber: Int
    ): QuranApiResponse<SurahDetailDto>
}
