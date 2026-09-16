package com.caysy.photoreport.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caysy.photoreport.data.ReportRepository
import com.caysy.photoreport.data.db.ReportSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the report list screen renders. */
sealed interface ReportsUiState {
  data object Loading : ReportsUiState

  data class Error(val message: String) : ReportsUiState

  data class Success(val reports: List<ReportSummary>) : ReportsUiState
}

class ReportsViewModel(private val repository: ReportRepository) : ViewModel() {

  val uiState: StateFlow<ReportsUiState> =
    repository
      .observeSummaries()
      .map<List<ReportSummary>, ReportsUiState> { ReportsUiState.Success(it) }
      .catch { emit(ReportsUiState.Error(it.message ?: "Не удалось загрузить отчёты")) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState.Loading)

  fun createReport(title: String) {
    val name = title.trim()
    if (name.isEmpty()) return
    viewModelScope.launch { repository.createReport(name) }
  }

  fun deleteReport(id: Long) {
    viewModelScope.launch { repository.deleteReport(id) }
  }
}
