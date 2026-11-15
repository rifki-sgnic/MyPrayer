package com.rfksgn.myprayer.core.database

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Immutable
@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameLatin: String,
    val nameTranslation: String,
    val revelationType: String,
    val versesCount: Int
)

@Immutable
@Entity(
    tableName = "ayahs",
    indices = [
        Index(value = ["surahNumber", "ayahNumber"], unique = true),
        Index(value = ["isBookmarked"])
    ]
)
data class AyahEntity(
    @PrimaryKey val id: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textLatin: String,
    val translationId: String,
    val isBookmarked: Boolean = false
)

@Dao
interface QuranDao {
    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahsFlow(): Flow<List<SurahEntity>>

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahCount(): Int

    @Query("SELECT COUNT(*) FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun getAyahCountForSurah(surahNumber: Int): Int

    @Query("SELECT * FROM surahs WHERE number = :surahNumber LIMIT 1")
    suspend fun getSurahByNumber(surahNumber: Int): SurahEntity?

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun getAyahsBySurahFlow(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE isBookmarked = 1 ORDER BY id ASC")
    fun getBookmarkedAyahsFlow(): Flow<List<AyahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Transaction
    suspend fun insertSurahWithAyahs(surah: SurahEntity, ayahs: List<AyahEntity>) {
        insertSurahs(listOf(surah))
        insertAyahs(ayahs)
    }

    @Query("UPDATE ayahs SET isBookmarked = :bookmarked WHERE id = :ayahId")
    suspend fun setBookmark(ayahId: Int, bookmarked: Boolean)

    @Query("SELECT id FROM ayahs WHERE isBookmarked = 1")
    suspend fun getBookmarkedIds(): List<Int>

    @Query("SELECT id FROM ayahs WHERE surahNumber = :surahNumber AND isBookmarked = 1")
    suspend fun getBookmarkedIdsForSurah(surahNumber: Int): List<Int>
}

@Database(
    entities = [SurahEntity::class, AyahEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getInstance(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "myprayer_quran_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}