package com.caysy.photoreport.ui.reportdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caysy.photoreport.data.ReportRepository
import com.caysy.photoreport.data.db.PhotoEntity
import com.caysy.photoreport.data.db.ReportEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Content of one report screen. */
data class ReportDetailUiState(
  val report: ReportEntity,
  val photos: List<PhotoEntity>,
)

sealed interface ReportDetailState {
  data object Loading : ReportDetailState

  data object NotFound : ReportDetailState

  data class Error(val message: String) : ReportDetailState

  data class Success(val content: ReportDetailUiState) : ReportDetailState
}

class ReportDetailViewModel(
  private val repository: ReportRepository,
  private val reportId: Long,
) : ViewModel() {

  val state: StateFlow<ReportDetailState> =
    repository
      .observeReportWithPhotos(reportId)
      .map<_, ReportDetailState> { combined ->
        if (combined == null) ReportDetailState.NotFound
        else ReportDetailState.Success(ReportDetailUiState(combined.report, combined.photos))
      }
      .catch { emit(ReportDetailState.Error(it.message ?: "Ошибка загрузки отчёта")) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportDetailState.Loading)

  fun deletePhoto(photoId: Long) {
    viewModelScope.launch { repository.deletePhoto(photoId) }
  }

  fun updateCaption(photo: PhotoEntity, caption: String) {
    viewModelScope.launch { repository.updatePhoto(photo.copy(caption = caption)) }
  }

  fun renameReport(title: String, note: String) {
    viewModelScope.launch { repository.renameReport(reportId, title, note) }
  }
}
