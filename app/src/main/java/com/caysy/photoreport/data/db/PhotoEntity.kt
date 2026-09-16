package com.caysy.photoreport.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One photo belonging to a [ReportEntity].
 *
 * [filePath] points at a file inside the app's private storage, so no storage
 * permission is needed to read it back. [latitude]/[longitude] are nullable:
 * a photo is still valid without a GPS fix.
 */
@Entity(
  tableName = "photos",
  foreignKeys =
    [
      ForeignKey(
        entity = ReportEntity::class,
        parentColumns = ["id"],
        childColumns = ["reportId"],
        // Deleting a report deletes its photos, so no orphans accumulate.
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("reportId")],
)
data class PhotoEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val reportId: Long,
  val filePath: String,
  /** Caption typed by the user. */
  val caption: String = "",
  /** Epoch millis when the photo was taken (or added). */
  val takenAt: Long,
  val latitude: Double? = null,
  val longitude: Double? = null,
  /** Manual ordering inside the report. */
  val position: Int = 0,
)
