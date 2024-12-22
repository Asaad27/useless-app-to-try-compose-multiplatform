package com.asaad27.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asaad27.life.dal.FakeTransactionRepository
import com.asaad27.life.model.DonutItem
import com.asaad27.life.ui.component.DonutChart
import com.asaad27.life.ui.component.LastTransactions
import com.asaad27.life.ui.theme.AppTheme
import com.asaad27.life.utils.AndroidDevicesPreview
import com.asaad27.life.viewmodel.DonutChartViewModel
import com.asaad27.life.viewmodel.LastTransactionsViewModel

class MainActivity : ComponentActivity() {
    private val donutChartViewModel: DonutChartViewModel<String> by viewModels()
    private val transactionsViewModel: LastTransactionsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LastTransactionsViewModel(FakeTransactionRepository()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                HomeScreen(
                    donutChartViewModel = donutChartViewModel,
                    transactionsViewModel = transactionsViewModel
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    donutChartViewModel: DonutChartViewModel<String>,
    transactionsViewModel: LastTransactionsViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DonutChartSection(
                viewModel = donutChartViewModel,
                modifier = Modifier.align(Alignment.CenterHorizontally).weight(0.4f)
            )
            LastTransactionsSection(
                viewModel = transactionsViewModel,
                modifier = Modifier.weight(0.6f)
            )
        }
    }
}

@Composable
fun DonutChartSection(
    viewModel: DonutChartViewModel<String>,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DonutChart(
                modifier = Modifier.widthIn(max = 400.dp),
                data = listOf(
                    DonutItem("groceries", Color(0xFF7986CB), 45, "Groceries"),
                    DonutItem("entertainment", Color(0xFF4FC3F7), 20, "Entertainment"),
                    DonutItem("transport", Color(0xFF4DB6AC), 15, "Transport"),
                    DonutItem("savings", Color(0xFFAED581), 20, "Savings")
                ),
                clickedIndex = uiState.clickedIndex,
                isScaled = uiState.isScaled,
                shouldAnimate = uiState.shouldAnimate,
                onEvent = viewModel::onEvent
            )
        }
    }
}

@Composable
fun LastTransactionsSection(
    viewModel: LastTransactionsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        LastTransactions(
            state = state,
            onEvent = viewModel::onEvent
        )
    }
}

@AndroidDevicesPreview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            donutChartViewModel = DonutChartViewModel(),
            transactionsViewModel = LastTransactionsViewModel(FakeTransactionRepository())
        )
    }
}

@AndroidDevicesPreview
@Composable
private fun LastTransactionsSectionPreview() {
    AppTheme {
        LastTransactionsSection(
            viewModel = LastTransactionsViewModel(FakeTransactionRepository())
        )
    }
}

@AndroidDevicesPreview
@Composable
private fun DonutChartSectionPreview() {
    AppTheme {
        DonutChartSection(
            viewModel = DonutChartViewModel()
        )
    }
}