package armadillo.studio.ui.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armadillo.studio.model.sys.Notice
import armadillo.studio.model.sys.TaskInfo
import armadillo.studio.model.sys.Ver
import armadillo.studio.ui.compose.repository.AppRepository
import armadillo.studio.ui.compose.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 *
 * Exposes three independent state flows:
 *  - [taskState]    – task status for a given uuid
 *  - [noticeState]  – latest system notice
 *  - [versionState] – latest app version info
 */
class HomeViewModel : ViewModel() {

    private val _taskState = MutableStateFlow<UiState<TaskInfo>>(UiState.Empty)
    val taskState: StateFlow<UiState<TaskInfo>> = _taskState.asStateFlow()

    private val _noticeState = MutableStateFlow<UiState<Notice>>(UiState.Empty)
    val noticeState: StateFlow<UiState<Notice>> = _noticeState.asStateFlow()

    private val _versionState = MutableStateFlow<UiState<Ver>>(UiState.Empty)
    val versionState: StateFlow<UiState<Ver>> = _versionState.asStateFlow()

    /** Loads task status for the given [uuid]. */
    fun loadTasks(uuid: String) {
        viewModelScope.launch {
            _taskState.value = UiState.Loading
            try {
                val result = AppRepository.getTaskInfo(uuid)
                _taskState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _taskState.value = UiState.Error(e.message ?: "获取任务信息失败")
            }
        }
    }

    /** Loads the latest system notice. */
    fun loadNotice() {
        viewModelScope.launch {
            _noticeState.value = UiState.Loading
            try {
                val result = AppRepository.getNotice()
                _noticeState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _noticeState.value = UiState.Error(e.message ?: "获取公告失败")
            }
        }
    }

    /** Loads the latest app version info. */
    fun loadVersion() {
        viewModelScope.launch {
            _versionState.value = UiState.Loading
            try {
                val result = AppRepository.getVersion()
                _versionState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _versionState.value = UiState.Error(e.message ?: "获取版本信息失败")
            }
        }
    }
}
