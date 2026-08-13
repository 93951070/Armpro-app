/*
 * Copyright (c) 2021. Armadillo
 * 控制台首页（任务列表）
 */

package armadillo.studio.ui.compose.screen

import androidx.compose.animation.core.animate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import armadillo.studio.ui.compose.state.UiState
import armadillo.studio.ui.compose.theme.StatusFail
import armadillo.studio.ui.compose.theme.StatusRunning
import armadillo.studio.ui.compose.theme.StatusSuccess
import armadillo.studio.ui.compose.theme.StatusWaiting
import kotlin.math.roundToInt

/** 任务状态 */
enum class TaskStatus { SUCCESS, FAIL, WAITING, RUNNING }

/** 任务 UI 模型 */
data class TaskItem(
    val name: String,
    val status: TaskStatus,
    val time: String,
    val progress: Float = 0f,
)

/**
 * 控制台首页。
 *
 * @param uiState     任务列表状态
 * @param onRefresh   下拉刷新 / 顶栏刷新回调
 * @param onAddTask   新增任务回调（FAB）
 * @param onRetry     错误重试回调
 * @param onTaskClick 任务点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState<List<TaskItem>>,
    onRefresh: () -> Unit,
    onAddTask: () -> Unit,
    onRetry: () -> Unit,
    onTaskClick: (TaskItem) -> Unit = {},
) {
    // 保留最近一次成功的数据，刷新时仍展示原列表 + 顶部刷新指示器
    var lastTasks by remember { mutableStateOf<List<TaskItem>>(emptyList()) }
    LaunchedEffect(uiState) {
        (uiState as? UiState.Success)?.let { lastTasks = it.data }
    }

    val isRefreshing = uiState is UiState.Loading
    val isError = uiState is UiState.Error
    val errorMessage = (uiState as? UiState.Error)?.message
    val hasTasks = lastTasks.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("控制台") },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = "新增任务")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                isError -> ErrorStateView(
                    message = errorMessage ?: "加载失败，请重试",
                    onRetry = onRetry,
                )

                !hasTasks && isRefreshing -> LoadingStateView()

                !hasTasks -> EmptyStateView(message = "暂无任务，点击右下角添加")

                else -> PullToRefreshLayout(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(lastTasks) { task ->
                            TaskCard(task = task, onClick = { onTaskClick(task) })
                        }
                    }
                }
            }
        }
    }
}

// ---- 下拉刷新 ----

/**
 * 简易下拉刷新容器。Material3 1.0.1 尚未提供官方 PullToRefresh，
 * 这里基于 [NestedScrollConnection] 自行实现：列表滑到顶部后继续下拉
 * 即出现刷新指示器，松手超过阈值则触发 [onRefresh]。
 */
@Composable
private fun PullToRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    val loadingPx = with(density) { 56.dp.toPx() }
    val maxPx = with(density) { 180.dp.toPx() }

    var pullDistance by remember { mutableStateOf(0f) }
    val refreshingState = rememberUpdatedState(isRefreshing)
    val onRefreshState = rememberUpdatedState(onRefresh)

    // 刷新状态变化时，指示器滑到对应位置
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            animate(initialValue = pullDistance, targetValue = loadingPx) { value, _ ->
                pullDistance = value
            }
        } else {
            animate(initialValue = pullDistance, targetValue = 0f) { value, _ ->
                pullDistance = value
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // 列表已在顶部且继续下拉（available.y > 0）时累积拉伸距离
                if (available.y > 0f && !refreshingState.value) {
                    pullDistance = (pullDistance + available.y * 0.5f).coerceIn(0f, maxPx)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (pullDistance >= thresholdPx && !refreshingState.value) {
                    onRefreshState.value()
                } else if (!refreshingState.value && pullDistance > 0f) {
                    animate(initialValue = pullDistance, targetValue = 0f) { value, _ ->
                        pullDistance = value
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = pullDistance.roundToInt()) }
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isRefreshing || pullDistance > 1f) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp,
                )
            }
        }
    }
}

// ---- 任务卡片 ----

@Composable
private fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    val (statusColor, statusLabel) = statusInfo(task.status)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(color = statusColor, label = statusLabel)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = task.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (task.status == TaskStatus.RUNNING || task.status == TaskStatus.WAITING) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = task.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(color: Color, label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun statusInfo(status: TaskStatus): Pair<Color, String> = when (status) {
    TaskStatus.SUCCESS -> StatusSuccess to "成功"
    TaskStatus.FAIL -> StatusFail to "失败"
    TaskStatus.WAITING -> StatusWaiting to "等待中"
    TaskStatus.RUNNING -> StatusRunning to "运行中"
}

// ---- 公共状态视图 ----

@Composable
private fun LoadingStateView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorStateView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("重试") }
    }
}

@Composable
private fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
