package com.caysy.photoreport.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caysy.photoreport.R
import com.caysy.photoreport.data.db.ReportSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
  viewModel: ReportsViewModel,
  onReportClick: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  var showCreateDialog by remember { mutableStateOf(false) }
  var pendingDelete by remember { mutableStateOf<ReportSummary?>(null) }

  Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text(stringResource(R.string.reports_title)) }) },
    floatingActionButton = {
      FloatingActionButton(onClick = { showCreateDialog = true }) {
        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.reports_new))
      }
    },
  ) { innerPadding ->
    Box(Modifier.fillMaxSize().padding(innerPadding)) {
      when (val current = state) {
        ReportsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

        is ReportsUiState.Error ->
          Text(
            text = current.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
          )

        is ReportsUiState.Success ->
          if (current.reports.isEmpty()) {
            EmptyState(Modifier.align(Alignment.Center))
          } else {
            ReportList(
              reports = current.reports,
              onReportClick = onReportClick,
              onReportDelete = { pendingDelete = it },
            )
          }
      }
    }
  }

  if (showCreateDialog) {
    CreateReportDialog(
      onDismiss = { showCreateDialog = false },
      onCreate = { title ->
        viewModel.createReport(title)
        showCreateDialog = false
      },
    )
  }

  pendingDelete?.let { summary ->
    AlertDialog(
      onDismissRequest = { pendingDelete = null },
      title = { Text(stringResource(R.string.report_delete_title)) },
      text = { Text(stringResource(R.string.report_delete_message, summary.report.title)) },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.deleteReport(summary.report.id)
            pendingDelete = null
          }
        ) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
      },
    )
  }
}

@Composable
private fun ReportList(
  reports: List<ReportSummary>,
  onReportClick: (Long) -> Unit,
  onReportDelete: (ReportSummary) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(reports, key = { it.report.id }) { summary ->
      ReportCard(
        summary = summary,
        onClick = { onReportClick(summary.report.id) },
        onDelete = { onReportDelete(summary) },
      )
    }
  }
}

@Composable
private fun ReportCard(summary: ReportSummary, onClick: () -> Unit, onDelete: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = summary.report.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (summary.report.note.isNotBlank()) {
          Text(
            text = summary.report.note,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Text(
          text =
            stringResource(R.string.report_subtitle, summary.photoCount, formatDate(summary.report.updatedAt)),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      IconButton(onClick = onDelete) {
        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
      }
    }
  }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
  Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = stringResource(R.string.reports_empty_title),
      style = MaterialTheme.typography.titleMedium,
    )
    Text(
      text = stringResource(R.string.reports_empty_hint),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun CreateReportDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
  var title by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.reports_new)) },
    text = {
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text(stringResource(R.string.report_name_label)) },
        singleLine = true,
      )
    },
    confirmButton = {
      TextButton(onClick = { onCreate(title) }, enabled = title.isNotBlank()) {
        Text(stringResource(R.string.action_create))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

private fun formatDate(epochMillis: Long): String = dateFormat.format(Date(epochMillis))
