package com.rfksgn.myprayer.data.mapper

import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.SurahEntity
import com.rfksgn.myprayer.data.remote.dto.AyahDto
import com.rfksgn.myprayer.data.remote.dto.SurahDetailDto
import com.rfksgn.myprayer.data.remote.dto.SurahDto

fun SurahDto.toEntity(): SurahEntity = SurahEntity(
    number = number,
    nameArabic = nameArabic,
    nameLatin = nameLatin,
    nameTranslation = nameTranslation,
    revelationType = revelationType,
    versesCount = versesCount
)

fun SurahDetailDto.toSurahEntity(): SurahEntity = SurahEntity(
    number = number,
    nameArabic = nameArabic,
    nameLatin = nameLatin,
    nameTranslation = nameTranslation,
    revelationType = revelationType,
    versesCount = versesCount
)

fun AyahDto.toEntity(surahNumber: Int, isBookmarked: Boolean): AyahEntity = AyahEntity(
    id = surahNumber * 1000 + ayahNumber,
    surahNumber = surahNumber,
    ayahNumber = ayahNumber,
    textArabic = textArabic,
    textLatin = textLatin,
    translationId = translationId,
    isBookmarked = isBookmarked
)
