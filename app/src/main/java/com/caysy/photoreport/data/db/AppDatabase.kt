package com.caysy.photoreport.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [ReportEntity::class, PhotoEntity::class],
  version = 1,
  exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun reportDao(): ReportDao

  companion object {
    private const val DB_NAME = "photoreport.db"

    @Volatile private var instance: AppDatabase? = null

    /** Single database instance for the whole process. */
    fun get(context: Context): AppDatabase =
      instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
              // Photos reference rows by id; losing the DB would orphan files,
              // so destructive migration is not acceptable once shipped.
              .build()
              .also { instance = it }
        }
  }
}
