package com.caysy.photoreport.data.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A report together with its photos, assembled by Room from two queries.
 */
data class ReportWithPhotos(
  @Embedded val report: ReportEntity,
  @Relation(parentColumn = "id", entityColumn = "reportId") val photos: List<PhotoEntity>,
)
