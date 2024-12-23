package com.asaad27.life.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.asaad27.life.model.Transaction
import com.asaad27.life.state.LastTransactionsEvent
import com.asaad27.life.state.LastTransactionsState
import com.asaad27.life.ui.component.shimmer.CardShimmerLayout
import com.asaad27.life.ui.component.shimmer.ShimmerBox
import com.asaad27.life.ui.component.shimmer.ShimmerCircle
import com.asaad27.life.ui.component.shimmer.ShimmerEffect
import com.asaad27.life.ui.component.shimmer.ShimmerLine
import com.asaad27.life.util.IconResource
import com.asaad27.life.util.currencyFormat
import com.asaad27.life.util.format

@Composable
fun LastTransactions(
    modifier: Modifier = Modifier,
    state: LastTransactionsState,
    onEvent: (LastTransactionsEvent) -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when {
            state.isLoading -> TransactionsShimmer(
                modifier = Modifier.fillMaxWidth()
            )
            state.error != null -> ErrorState(
                error = state.error,
                onRetry = { onEvent(LastTransactionsEvent.LoadTransactions) },
                modifier = Modifier.align(Alignment.Center)
            )
            state.transactions.isEmpty() -> EmptyState(
                modifier = Modifier.align(Alignment.Center)
            )
            else -> TransactionsList(
                transactions = state.transactions,
                isLoadingMore = state.isLoadingMore,
                hasMore = state.hasMore,
                onLoadMore = { onEvent(LastTransactionsEvent.LoadMoreTransactions) },
                onTransactionClick = { transaction ->
                    onEvent(LastTransactionsEvent.TransactionClicked(transaction))
                }
            )
        }
    }
}

@Composable
private fun TransactionsList(
    transactions: List<Transaction>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(
            items = transactions,
            key = { it.id }
        ) { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) }
            )
        }

        if (isLoadingMore) {
            item { LoadingIndicator() }
        }

        // Trigger load more when reaching the end
        if (hasMore && !isLoadingMore) {
            item {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryIcon(
                iconResource = transaction.spendingCategory?.icon,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.timestampMs.format(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AmountText(
                amount = transaction.amount,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun TransactionsShimmer(modifier: Modifier = Modifier) {
    ShimmerEffect { brush ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) {
                CardShimmerLayout(
                    leadingContent = {
                        ShimmerCircle(brush = brush, size = 40.dp)
                    },
                    middleContent = {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ShimmerLine(
                                brush = brush,
                                height = 20.dp,
                                widthFraction = 0.7f
                            )
                            ShimmerLine(
                                brush = brush,
                                height = 16.dp,
                                widthFraction = 0.4f
                            )
                        }
                    },
                    trailingContent = {
                        ShimmerBox(
                            brush = brush,
                            modifier = Modifier
                                .width(80.dp)
                                .height(24.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Add your first transaction to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun CategoryIcon(
    iconResource: IconResource?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            iconResource?.let { icon ->
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = icon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AmountText(
    amount: Double,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = amount.currencyFormat(),
        style = style,
        color = if (amount >= 0) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        }
    )
}