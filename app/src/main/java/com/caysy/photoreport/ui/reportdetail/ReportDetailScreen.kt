package com.caysy.photoreport.ui.reportdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caysy.photoreport.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
  viewModel: ReportDetailViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  val title =
    when (val s = state) {
      is ReportDetailState.Success -> s.content.report.title
      else -> stringResource(R.string.app_name)
    }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
          }
        },
      )
    },
  ) { innerPadding ->
    Box(Modifier.fillMaxSize().padding(innerPadding)) {
      when (val current = state) {
        ReportDetailState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

        ReportDetailState.NotFound ->
          Text(stringResource(R.string.report_not_found), Modifier.align(Alignment.Center))

        is ReportDetailState.Error ->
          Text(
            text = current.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
          )

        is ReportDetailState.Success ->
          if (current.content.photos.isEmpty()) {
            EmptyPhotos(Modifier.align(Alignment.Center))
          } else {
            PhotoList(current.content)
          }
      }
    }
  }
}

@Composable
private fun PhotoList(content: ReportDetailUiState) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(content.photos, key = { it.id }) { photo ->
      Text(
        text = photo.caption.ifBlank { photo.filePath.substringAfterLast('/') },
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@Composable
private fun EmptyPhotos(modifier: Modifier = Modifier) {
  Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = stringResource(R.string.report_detail_empty),
      style = MaterialTheme.typography.titleMedium,
    )
  }
}
