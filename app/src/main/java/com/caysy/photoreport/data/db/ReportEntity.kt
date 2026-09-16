package com.caysy.photoreport.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A photo report: one object/site that groups many photos.
 *
 * Only metadata lives here — the image bytes stay in files on disk.
 */
@Entity(tableName = "reports")
data class ReportEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  /** Human-readable name, e.g. "Обход дома №5". */
  val title: String,
  /** Optional free-form note about the whole report. */
  val note: String = "",
  /** Epoch millis. */
  val createdAt: Long,
  /** Epoch millis, bumped whenever the report or its photos change. */
  val updatedAt: Long,
)
