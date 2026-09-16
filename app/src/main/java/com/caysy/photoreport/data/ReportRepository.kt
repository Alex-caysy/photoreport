package com.caysy.photoreport.data

import com.caysy.photoreport.data.db.PhotoEntity
import com.caysy.photoreport.data.db.ReportDao
import com.caysy.photoreport.data.db.ReportEntity
import com.caysy.photoreport.data.db.ReportSummary
import com.caysy.photoreport.data.db.ReportWithPhotos
import kotlinx.coroutines.flow.Flow

/**
 * Everything the UI needs to read and write reports.
 *
 * Kept as an interface so screens depend on behaviour, not on Room — tests and
 * future backends (a server, an export format) can substitute their own
 * implementation.
 */
interface ReportRepository {
  fun observeSummaries(): Flow<List<ReportSummary>>

  fun observeReport(id: Long): Flow<ReportEntity?>

  fun observeReportWithPhotos(id: Long): Flow<ReportWithPhotos?>

  /** Creates a report and returns its new id. */
  suspend fun createReport(title: String, note: String = ""): Long

  suspend fun renameReport(id: Long, title: String, note: String)

  /** Deletes the report. Callers must also remove its photo files. */
  suspend fun deleteReport(id: Long)

  suspend fun photosOf(reportId: Long): List<PhotoEntity>

  suspend fun addPhoto(
    reportId: Long,
    filePath: String,
    caption: String = "",
    latitude: Double? = null,
    longitude: Double? = null,
    takenAt: Long = System.currentTimeMillis(),
  ): Long

  suspend fun updatePhoto(photo: PhotoEntity)

  /** Deletes the photo row and reports the file path so the caller can unlink it. */
  suspend fun deletePhoto(photoId: Long): String?
}

class DefaultReportRepository(
  private val dao: ReportDao,
  private val now: () -> Long = System::currentTimeMillis,
) : ReportRepository {

  override fun observeSummaries(): Flow<List<ReportSummary>> = dao.observeSummaries()

  override fun observeReport(id: Long): Flow<ReportEntity?> = dao.observeReport(id)

  override fun observeReportWithPhotos(id: Long): Flow<ReportWithPhotos?> =
    dao.observeReportWithPhotos(id)

  override suspend fun createReport(title: String, note: String): Long {
    val timestamp = now()
    return dao.insertReport(
      ReportEntity(title = title.trim(), note = note.trim(), createdAt = timestamp, updatedAt = timestamp)
    )
  }

  override suspend fun renameReport(id: Long, title: String, note: String) {
    val current = dao.getReport(id) ?: return
    dao.updateReport(
      current.copy(title = title.trim(), note = note.trim(), updatedAt = now())
    )
  }

  override suspend fun deleteReport(id: Long) {
    dao.deleteReport(id)
  }

  override suspend fun photosOf(reportId: Long): List<PhotoEntity> = dao.getPhotos(reportId)

  override suspend fun addPhoto(
    reportId: Long,
    filePath: String,
    caption: String,
    latitude: Double?,
    longitude: Double?,
    takenAt: Long,
  ): Long {
    val position = dao.maxPosition(reportId) + 1
    val id =
      dao.insertPhoto(
        PhotoEntity(
          reportId = reportId,
          filePath = filePath,
          caption = caption,
          takenAt = takenAt,
          latitude = latitude,
          longitude = longitude,
          position = position,
        )
      )
    // The list is sorted by updatedAt, so adding a photo moves its report up.
    dao.touchReport(reportId, now())
    return id
  }

  override suspend fun updatePhoto(photo: PhotoEntity) {
    dao.updatePhoto(photo)
    dao.touchReport(photo.reportId, now())
  }

  override suspend fun deletePhoto(photoId: Long): String? {
    val photo = dao.getPhoto(photoId) ?: return null
    dao.deletePhoto(photoId)
    dao.touchReport(photo.reportId, now())
    return photo.filePath
  }
}
