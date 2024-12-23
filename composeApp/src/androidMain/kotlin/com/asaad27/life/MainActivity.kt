package com.asaad27.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asaad27.life.model.SpendingCategory
import com.asaad27.life.model.Transaction
import com.asaad27.life.repository.FakeSpendingCategoryRepository
import com.asaad27.life.repository.FakeTransactionRepository
import com.asaad27.life.state.LastTransactionsState
import com.asaad27.life.ui.component.DonutChart
import com.asaad27.life.ui.component.LastTransactions
import com.asaad27.life.ui.component.TransactionsShimmer
import com.asaad27.life.ui.component.shimmer.CardShimmerLayout
import com.asaad27.life.ui.component.shimmer.ShimmerBox
import com.asaad27.life.ui.component.shimmer.ShimmerCircle
import com.asaad27.life.ui.component.shimmer.ShimmerEffect
import com.asaad27.life.ui.component.shimmer.ShimmerLine
import com.asaad27.life.ui.theme.AppTheme
import com.asaad27.life.util.IconResource
import com.asaad27.life.utils.AndroidDevicesPreview
import com.asaad27.life.viewmodel.DonutChartViewModel
import com.asaad27.life.viewmodel.LastTransactionsViewModel

class MainActivity : ComponentActivity() {
    private val donutChartViewModel: DonutChartViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DonutChartViewModel(FakeSpendingCategoryRepository()) as T
            }
        }
    }

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
    donutChartViewModel: DonutChartViewModel,
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
    viewModel: DonutChartViewModel,
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
                data = viewModel.getDonutItems(),
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


//todo: find a cleaner way to put the previews in the common main

@AndroidDevicesPreview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            donutChartViewModel = DonutChartViewModel(FakeSpendingCategoryRepository()),
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
            viewModel = DonutChartViewModel(FakeSpendingCategoryRepository())
        )
    }
}

@AndroidDevicesPreview
@Composable
fun LastTransactionsPreview() {
    val state = LastTransactionsState(
        transactions = listOf(
            Transaction(
                id = "1",
                timestampMs = System.currentTimeMillis(),
                amount = -10.0,
                description = "Transaction 1",
                spendingCategory = SpendingCategory(
                    id = "category1",
                    name = "Category 1",
                    icon = IconResource(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Category 1"
                    ),
                    totalAmount = 10.0,
                    color = Color.Red
                )
            ),
            Transaction(
                id = "2",
                timestampMs = System.currentTimeMillis(),
                amount = 20.0,
                description = "Transaction 2",
                spendingCategory = SpendingCategory(
                    id = "category2",
                    name = "Category 2",
                    icon = IconResource(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Category 2"
                    ),
                    totalAmount = 20.0,
                    color = Color.Blue
                )
            )
        ),
        isLoading = false,
        isLoadingMore = false,
        error = null,
        hasMore = true
    )
    LastTransactions(
        state = state,
        onEvent = {}
    )
}
@AndroidDevicesPreview
@Composable
fun LastTransactionsLoadingPreview() {
    val state = LastTransactionsState(
        isLoading = true
    )
    LastTransactions(
        state = state,
        onEvent = {}
    )
}
@AndroidDevicesPreview
@Composable
fun LastTransactionsErrorPreview() {
    val state = LastTransactionsState(
        error = "Error loading transactions"
    )
    LastTransactions(
        state = state,
        onEvent = {}
    )
}
@AndroidDevicesPreview
@Composable
fun LastTransactionsEmptyPreview() {
    val state = LastTransactionsState(
        transactions = emptyList()
    )
    LastTransactions(
        state = state,
        onEvent = {}
    )
}


@AndroidDevicesPreview
@Composable
fun TransactionsShimmerPreview() {
    TransactionsShimmer()
}


@AndroidDevicesPreview
@Composable
fun CardShimmerLayoutPreview() {
    CardShimmerLayout(
        leadingContent = {
            Box(modifier = Modifier.size(48.dp))
        },
        middleContent = {
            Text("Middle Content")
        },
        trailingContent = {
            Text("Trailing Content")
        }
    )
}

@AndroidDevicesPreview
@Composable
fun ShimmerBoxPreview() {
    val brush = Brush.linearGradient(
        colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.8f), Color.Gray.copy(alpha = 0.2f)),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )
    ShimmerBox(brush = brush, modifier = Modifier.size(100.dp))
}
@AndroidDevicesPreview
@Composable
fun ShimmerCirclePreview() {
    val brush = Brush.linearGradient(
        colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.8f), Color.Gray.copy(alpha = 0.2f)),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )
    ShimmerCircle(brush = brush, size = 50.dp, modifier = Modifier)
}
@AndroidDevicesPreview
@Composable
fun ShimmerLinePreview() {
    val brush = Brush.linearGradient(
        colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.8f), Color.Gray.copy(alpha = 0.2f)),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )
    ShimmerLine(brush = brush, height = 10.dp, modifier = Modifier)
}


@AndroidDevicesPreview
@Composable
fun ShimmerEffectPreview() {
    ShimmerEffect(modifier = Modifier.fillMaxWidth().padding(16.dp)) { brush ->
        Spacer(modifier = Modifier.fillMaxWidth().height(100.dp).background(brush))
    }
}
