package com.caysy.photoreport.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Database access for reports and photos.
 *
 * Read methods return [Flow] so the UI updates itself whenever data changes —
 * no manual refresh calls anywhere in the app.
 */
@Dao
interface ReportDao {

  // ---- reports ------------------------------------------------------------

  @Query(
    """
    SELECT r.*, (SELECT COUNT(*) FROM photos p WHERE p.reportId = r.id) AS photoCount
    FROM reports r
    ORDER BY r.updatedAt DESC
    """
  )
  fun observeSummaries(): Flow<List<ReportSummary>>

  @Query("SELECT * FROM reports WHERE id = :id")
  fun observeReport(id: Long): Flow<ReportEntity?>

  @Query("SELECT * FROM reports WHERE id = :id")
  suspend fun getReport(id: Long): ReportEntity?

  @Transaction
  @Query("SELECT * FROM reports WHERE id = :id")
  fun observeReportWithPhotos(id: Long): Flow<ReportWithPhotos?>

  @Insert suspend fun insertReport(report: ReportEntity): Long

  @Update suspend fun updateReport(report: ReportEntity)

  @Query("DELETE FROM reports WHERE id = :id")
  suspend fun deleteReport(id: Long)

  @Query("UPDATE reports SET updatedAt = :timestamp WHERE id = :id")
  suspend fun touchReport(id: Long, timestamp: Long)

  // ---- photos -------------------------------------------------------------

  @Insert suspend fun insertPhoto(photo: PhotoEntity): Long

  @Update suspend fun updatePhoto(photo: PhotoEntity)

  @Query("DELETE FROM photos WHERE id = :id")
  suspend fun deletePhoto(id: Long)

  @Query("SELECT * FROM photos WHERE id = :id")
  suspend fun getPhoto(id: Long): PhotoEntity?

  @Query("SELECT * FROM photos WHERE reportId = :reportId ORDER BY position ASC, takenAt ASC")
  suspend fun getPhotos(reportId: Long): List<PhotoEntity>

  @Query("SELECT COALESCE(MAX(position), -1) FROM photos WHERE reportId = :reportId")
  suspend fun maxPosition(reportId: Long): Int
}
