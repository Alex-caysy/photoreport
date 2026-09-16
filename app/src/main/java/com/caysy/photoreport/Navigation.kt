package com.caysy.photoreport

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.caysy.photoreport.data.DefaultReportRepository
import com.caysy.photoreport.data.ReportRepository
import com.caysy.photoreport.data.db.AppDatabase
import com.caysy.photoreport.ui.reportdetail.ReportDetailScreen
import com.caysy.photoreport.ui.reportdetail.ReportDetailViewModel
import com.caysy.photoreport.ui.reports.ReportsScreen
import com.caysy.photoreport.ui.reports.ReportsViewModel

/**
 * Builds the repository once per process.
 *
 * Deliberately simple for now: when the app grows, this is the single place to
 * swap in dependency injection without touching any screen.
 */
@Composable
private fun rememberRepository(): ReportRepository {
  val context = LocalContext.current
  return remember(context) {
    DefaultReportRepository(AppDatabase.get(context).reportDao())
  }
}

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Reports)
  val repository = rememberRepository()

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Reports> {
          val viewModel: ReportsViewModel =
            viewModel(factory = viewModelFactory { initializer { ReportsViewModel(repository) } })
          ReportsScreen(
            viewModel = viewModel,
            onReportClick = { id -> backStack.add(ReportDetail(id)) },
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<ReportDetail> { key ->
          val viewModel: ReportDetailViewModel =
            viewModel(
              key = "report-${key.reportId}",
              factory = viewModelFactory { initializer { ReportDetailViewModel(repository, key.reportId) } },
            )
          ReportDetailScreen(
            viewModel = viewModel,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}
