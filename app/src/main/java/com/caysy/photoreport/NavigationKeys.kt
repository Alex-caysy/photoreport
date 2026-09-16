package com.caysy.photoreport

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Report list — the start destination. */
@Serializable data object Reports : NavKey

/** A single report with its photos. */
@Serializable data class ReportDetail(val reportId: Long) : NavKey
