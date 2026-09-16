package com.caysy.photoreport.data.db

import androidx.room.Embedded

/**
 * Row for the report list: the report plus how many photos it holds.
 *
 * The count is computed by SQL, so the list screen never has to load the
 * photos themselves just to show "12 фото".
 */
data class ReportSummary(
  @Embedded val report: ReportEntity,
  val photoCount: Int,
)
