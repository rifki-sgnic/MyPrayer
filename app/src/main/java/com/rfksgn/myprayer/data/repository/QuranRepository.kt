package com.rfksgn.myprayer.data.repository

import com.rfksgn.myprayer.core.database.AyahEntity
import com.rfksgn.myprayer.core.database.QuranDao
import com.rfksgn.myprayer.core.database.SurahEntity
import com.rfksgn.myprayer.data.mapper.toEntity
import com.rfksgn.myprayer.data.mapper.toSurahEntity
import com.rfksgn.myprayer.data.remote.api.QuranApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuranRepository(
    private val dao: QuranDao,
    private val apiService: QuranApiService
) {
    val allSurahsFlow: Flow<List<SurahEntity>> = dao.getAllSurahsFlow()
    val bookmarkedAyahsFlow: Flow<List<AyahEntity>> = dao.getBookmarkedAyahsFlow()

    fun getAyahsFlow(surahNumber: Int): Flow<List<AyahEntity>> =
        dao.getAyahsBySurahFlow(surahNumber)

    suspend fun getSurahByNumber(surahNumber: Int): SurahEntity? =
        dao.getSurahByNumber(surahNumber)

    suspend fun toggleBookmark(ayah: AyahEntity) {
        dao.setBookmark(ayah.id, !ayah.isBookmarked)
    }

    /**
     * Memastikan data awal selalu tersedia secara instan meskipun HP sedang 100% offline,
     * lalu mengambil daftar lengkap 114 Surah di latar belakang.
     */
    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        if (dao.getSurahCount() == 0) {
            seedInitialOfflineQuran()
        }
        // Coba lengkapi daftar 114 metadata surah jika masih kurang dari 114
        if (dao.getSurahCount() < 114) {
            fetchAllSurahMetadataFromKemenagApi()
        }
    }

    /**
     * Memastikan ayat-ayat untuk surah tertentu sudah tersimpan di Room Database.
     */
    suspend fun ensureSurahAyahsLoaded(surahNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val surah = dao.getSurahByNumber(surahNumber)
        val currentCount = dao.getAyahCountForSurah(surahNumber)
        if (surah != null && currentCount >= surah.versesCount) {
            return@withContext true
        }
        fetchAndCacheSingleSurah(surahNumber)
    }

    /**
     * Mengunduh seluruh 114 Surah (6.236 ayat) ke dalam Room Database untuk pemakaian 100% Offline.
     */
    suspend fun downloadFullQuranOffline(onProgress: (Int, Int) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            fetchAllSurahMetadataFromKemenagApi()
            for (surahNum in 1..114) {
                coroutineContext.ensureActive()
                onProgress(surahNum, 114)
                val surah = dao.getSurahByNumber(surahNum)
                val localAyahCount = dao.getAyahCountForSurah(surahNum)
                if (surah == null || localAyahCount < surah.versesCount) {
                    val success = fetchAndCacheSingleSurah(surahNum)
                    if (!success) {
                        return@withContext false
                    }
                }
            }
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            false
        }
    }

    private suspend fun fetchAllSurahMetadataFromKemenagApi() {
        try {
            val response = apiService.getAllSurahs()
            if (response.code == 200) {
                val list = response.data.map { it.toEntity() }
                dao.insertSurahs(list)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    private suspend fun fetchAndCacheSingleSurah(surahNumber: Int): Boolean {
        return try {
            val response = apiService.getSurahDetail(surahNumber)
            if (response.code != 200) return false

            val detail = response.data
            val existingBookmarks = dao.getBookmarkedIdsForSurah(surahNumber).toSet()
            val ayahs = detail.ayahs.map { aDto ->
                aDto.toEntity(
                    surahNumber = surahNumber,
                    isBookmarked = existingBookmarks.contains(surahNumber * 1000 + aDto.ayahNumber)
                )
            }
            dao.insertSurahWithAyahs(detail.toSurahEntity(), ayahs)
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            false
        }
    }

    private suspend fun seedInitialOfflineQuran() {
        val initialSurahs = listOf(
            SurahEntity(1, "الفاتحة", "Al-Fatihah", "Pembukaan", "Makkiyah", 7),
            SurahEntity(112, "الإخلاص", "Al-Ikhlas", "Ikhlas", "Makkiyah", 4),
            SurahEntity(113, "الفلق", "Al-Falaq", "Subuh", "Makkiyah", 5),
            SurahEntity(114, "الناس", "An-Nas", "Manusia", "Makkiyah", 6)
        )
        dao.insertSurahs(initialSurahs)

        val initialAyahs = listOf(
            // Al-Fatihah (1..7)
            AyahEntity(1001, 1, 1, "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ", "Bismillāhir-raḥmānir-raḥīm(i).", "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang."),
            AyahEntity(1002, 1, 2, "اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَۙ", "Al-ḥamdu lillāhi rabbil-‘ālamīn(a).", "Segala puji bagi Allah, Tuhan seluruh alam,"),
            AyahEntity(1003, 1, 3, "الرَّحْمٰنِ الرَّحِيْمِۙ", "Ar-raḥmānir-raḥīm(i).", "Yang Maha Pengasih, Maha Penyayang,"),
            AyahEntity(1004, 1, 4, "مٰلِكِ يَوْمِ الدِّيْنِۗ", "Māliki yaumid-dīn(i).", "Pemilik hari pembalasan."),
            AyahEntity(1005, 1, 5, "اِيَّاكَ نَعْبُدُ وَاِيَّاكَ نَسْتَعِيْنُۗ", "Iyyāka na‘budu wa iyyāka nasta‘īn(u).", "Hanya kepada Engkaulah kami menyembah dan hanya kepada Engkaulah kami mohon pertolongan."),
            AyahEntity(1006, 1, 6, "اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَۙ", "Ihdinaṣ-ṣirāṭal-mustaqīm(a).", "Tunjukilah kami jalan yang lurus,"),
            AyahEntity(1007, 1, 7, "صِرَاطَ الَّذِيْنَ اَنْعَمْتَ عَلَيْهِمْ ۙ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّاۤلِّيْنَ ࣖ", "Ṣirāṭal-lażīna an‘amta ‘alaihim gairil-magḍūbi ‘alaihim wa laḍ-ḍāllīn(a).", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat."),
            // Al-Ikhlas (1..4)
            AyahEntity(112001, 112, 1, "قُلْ هُوَ اللّٰهُ اَحَدٌۚ", "Qul huwallāhu aḥad(un).", "Katakanlah (Muhammad), ”Dialah Allah, Yang Maha Esa."),
            AyahEntity(112002, 112, 2, "اَللّٰهُ الصَّمَدُۚ", "Allāhuṣ-ṣamad(u).", "Allah tempat meminta segala sesuatu."),
            AyahEntity(112003, 112, 3, "لَمْ يَلِدْ وَلَمْ يُوْلَدْۙ", "Lam yalid wa lam yūlad.", "(Allah) tidak beranak dan tidak pula diperanakkan."),
            AyahEntity(112004, 112, 4, "وَلَمْ يَكُنْ لَّهٗ كُفُوًا اَحَدٌ ࣖ", "Wa lam yakul lahū kufuwan aḥad(un).", "Dan tidak ada sesuatu yang setara dengan Dia.”"),
            // Al-Falaq (1..5)
            AyahEntity(113001, 113, 1, "قُلْ اَعُوْذُ بِرَبِّ الْفَلَقِۙ", "Qul a‘ūżu birabbil-falaq(i).", "Katakanlah, “Aku berlindung kepada Tuhan yang menguasai subuh (fajar),"),
            AyahEntity(113002, 113, 2, "مِنْ شَرِّ مَا خَلَقَۙ", "Min syarri mā khalaq(a).", "dari kejahatan (makhluk yang) Dia ciptakan,"),
            AyahEntity(113003, 113, 3, "وَمِنْ شَرِّ غَاسِقٍ اِذَا وَقَبَۙ", "Wa min syarri gāsiqin iżā waqab(a).", "dan dari kejahatan malam apabila telah gelap gulita,"),
            AyahEntity(113004, 113, 4, "وَمِنْ شَرِّ النَّفّٰثٰتِ فِى الْعُقَدِۙ", "Wa min syarrin-naffāṡāti fil-‘uqad(i).", "dan dari kejahatan (perempuan-perempuan) penyihir yang meniup pada buhul-buhul (talinya),"),
            AyahEntity(113005, 113, 5, "وَمِنْ شَرِّ حَاسِدٍ اِذَا حَسَدَ ࣖ", "Wa min syarri ḥāsidin iżā ḥasad(a).", "dan dari kejahatan orang yang dengki apabila dia dengki.”"),
            // An-Nas (1..6)
            AyahEntity(114001, 114, 1, "قُلْ اَعُوْذُ بِرَبِّ النَّاسِۙ", "Qul a‘ūżu birabbin-nās(i).", "Katakanlah, “Aku berlindung kepada Tuhannya manusia,"),
            AyahEntity(114002, 114, 2, "مَلِكِ النَّاسِۙ", "Malikin-nās(i).", "Raja manusia,"),
            AyahEntity(114003, 114, 3, "اِلٰهِ النَّاسِۙ", "Ilāhin-nās(i).", "sembahan manusia,"),
            AyahEntity(114004, 114, 4, "مِنْ شَرِّ الْوَسْوَاسِ ەۙ الْخَنَّاسِۖ", "Min syarril-waswāsil-khannās(i).", "dari kejahatan (bisikan) setan yang bersembunyi,"),
            AyahEntity(114005, 114, 5, "الَّذِيْ يُوَسْوِسُ فِيْ صُدُوْرِ النَّاسِۙ", "Allażī yuwaswisu fī ṣudūrin-nās(i).", "yang membisikkan (kejahatan) ke dalam dada manusia,"),
            AyahEntity(114006, 114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ ࣖ", "Minal-jinnati wan-nās(i).", "dari (golongan) jin dan manusia.”")
        )
        dao.insertAyahs(initialAyahs)
    }
}
