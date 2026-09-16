package com.caysy.photoreport.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.caysy.photoreport.data.db.AppDatabase
import com.caysy.photoreport.data.db.PhotoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises the real Room database in memory.
 *
 * Robolectric lets these run on the JVM, so no device or emulator is needed.
 */
@RunWith(RobolectricTestRunner::class)
class ReportRepositoryTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: ReportRepository

  private var clock = 1_000L

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    repository = DefaultReportRepository(db.reportDao()) { clock }
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun createReport_appearsInList_withZeroPhotos() = runTest {
    repository.createReport("Дом №5")

    val summaries = repository.observeSummaries().first()

    assertEquals(1, summaries.size)
    assertEquals("Дом №5", summaries.first().report.title)
    assertEquals(0, summaries.first().photoCount)
  }

  @Test
  fun createReport_trimsWhitespace() = runTest {
    repository.createReport("   Объект А   ")

    assertEquals("Объект А", repository.observeSummaries().first().first().report.title)
  }

  @Test
  fun addPhoto_incrementsCount_andOrdersByPosition() = runTest {
    val reportId = repository.createReport("Объект")
    repository.addPhoto(reportId, "/tmp/a.jpg", caption = "первое")
    clock += 10
    repository.addPhoto(reportId, "/tmp/b.jpg", caption = "второе")

    val summary = repository.observeSummaries().first().first()
    assertEquals(2, summary.photoCount)

    val photos = repository.photosOf(reportId)
    assertEquals(listOf("/tmp/a.jpg", "/tmp/b.jpg"), photos.map { it.filePath })
  }

  @Test
  fun addPhoto_storesCoordinates_butTheyAreOptional() = runTest {
    val reportId = repository.createReport("Объект")
    repository.addPhoto(reportId, "/tmp/geo.jpg", latitude = 55.75, longitude = 37.61)
    repository.addPhoto(reportId, "/tmp/nogeo.jpg")

    val photos = repository.photosOf(reportId)
    assertEquals(55.75, photos[0].latitude!!, 0.0001)
    assertEquals(37.61, photos[0].longitude!!, 0.0001)
    assertNull(photos[1].latitude)
    assertNull(photos[1].longitude)
  }

  @Test
  fun deleteReport_alsoRemovesItsPhotos() = runTest {
    val reportId = repository.createReport("Объект")
    repository.addPhoto(reportId, "/tmp/a.jpg")
    repository.addPhoto(reportId, "/tmp/b.jpg")

    repository.deleteReport(reportId)

    assertTrue(repository.observeSummaries().first().isEmpty())
    // CASCADE on the foreign key must have removed the children too.
    assertTrue(repository.photosOf(reportId).isEmpty())
  }

  @Test
  fun deletePhoto_returnsFilePath_forFileCleanup() = runTest {
    val reportId = repository.createReport("Объект")
    val photoId = repository.addPhoto(reportId, "/tmp/remove-me.jpg")

    val path = repository.deletePhoto(photoId)

    assertEquals("/tmp/remove-me.jpg", path)
    assertNull(repository.deletePhoto(photoId))
    assertTrue(repository.photosOf(reportId).isEmpty())
  }

  @Test
  fun renameReport_updatesTitleAndNote() = runTest {
    val reportId = repository.createReport("Старое")
    clock += 50

    repository.renameReport(reportId, "Новое", "примечание")

    val report = repository.observeReport(reportId).first()
    assertNotNull(report)
    assertEquals("Новое", report!!.title)
    assertEquals("примечание", report.note)
    assertEquals(clock, report.updatedAt)
  }

  @Test
  fun addingPhoto_bumpsReportToTopOfList() = runTest {
    val first = repository.createReport("Первый")
    clock += 10
    val second = repository.createReport("Второй")

    // "Второй" is newest, so it leads.
    assertEquals("Второй", repository.observeSummaries().first().first().report.title)

    // Adding a photo to the older report should move it back to the top.
    clock += 10
    repository.addPhoto(first, "/tmp/new.jpg")
    assertEquals("Первый", repository.observeSummaries().first().first().report.title)

    assertTrue(second > 0)
  }

  @Test
  fun updateCaption_persists() = runTest {
    val reportId = repository.createReport("Объект")
    val photoId = repository.addPhoto(reportId, "/tmp/a.jpg")
    val photo = PhotoEntity(id = photoId, reportId = reportId, filePath = "/tmp/a.jpg", takenAt = 0)

    repository.updatePhoto(photo.copy(caption = "трещина в стене"))

    assertEquals("трещина в стене", repository.photosOf(reportId).first().caption)
  }
}
