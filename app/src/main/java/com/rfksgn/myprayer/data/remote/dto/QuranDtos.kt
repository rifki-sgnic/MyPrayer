package com.rfksgn.myprayer.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuranApiResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T
)

data class SurahDto(
    @SerializedName("nomor") val number: Int,
    @SerializedName("nama") val nameArabic: String,
    @SerializedName("namaLatin") val nameLatin: String,
    @SerializedName("jumlahAyat") val versesCount: Int,
    @SerializedName("tempatTurun") val revelationType: String,
    @SerializedName("arti") val nameTranslation: String
)

data class SurahDetailDto(
    @SerializedName("nomor") val number: Int,
    @SerializedName("nama") val nameArabic: String,
    @SerializedName("namaLatin") val nameLatin: String,
    @SerializedName("jumlahAyat") val versesCount: Int,
    @SerializedName("tempatTurun") val revelationType: String,
    @SerializedName("arti") val nameTranslation: String,
    @SerializedName("ayat") val ayahs: List<AyahDto>
)

data class AyahDto(
    @SerializedName("nomorAyat") val ayahNumber: Int,
    @SerializedName("teksArab") val textArabic: String,
    @SerializedName("teksLatin") val textLatin: String,
    @SerializedName("teksIndonesia") val translationId: String
)
