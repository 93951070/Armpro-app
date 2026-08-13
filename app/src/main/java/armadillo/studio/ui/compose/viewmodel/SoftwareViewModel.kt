package armadillo.studio.ui.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armadillo.studio.common.enums.SoftEnums
import armadillo.studio.model.Basic
import armadillo.studio.model.soft.UserSoft
import armadillo.studio.ui.compose.repository.AppRepository
import armadillo.studio.ui.compose.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Software management screen.
 *
 * Exposes:
 *  - [softListState]   – paginated list of the user's applications
 *  - [softDetailState] – result of module info load / save / delete operations
 */
class SoftwareViewModel : ViewModel() {

    private val _softListState = MutableStateFlow<UiState<UserSoft>>(UiState.Empty)
    val softListState: StateFlow<UiState<UserSoft>> = _softListState.asStateFlow()

    private val _softDetailState = MutableStateFlow<UiState<Basic>>(UiState.Empty)
    val softDetailState: StateFlow<UiState<Basic>> = _softDetailState.asStateFlow()

    /** Loads a paginated list of the user's applications. */
    fun loadSoftList(offset: Int, limit: Int) {
        viewModelScope.launch {
            _softListState.value = UiState.Loading
            try {
                val result = AppRepository.getSoftList(offset, limit)
                _softListState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _softListState.value = UiState.Error(e.message ?: "获取应用列表失败")
            }
        }
    }

    /** Deletes the application identified by [appkey]. */
    fun deleteSoft(appkey: String) {
        viewModelScope.launch {
            _softDetailState.value = UiState.Loading
            try {
                val result = AppRepository.deleteSoft(appkey)
                _softDetailState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _softDetailState.value = UiState.Error(e.message ?: "删除应用失败")
            }
        }
    }

    /** Loads module-specific configuration for [appkey] / [softEnums]. */
    fun loadSoftModelInfo(appkey: String, softEnums: SoftEnums) {
        viewModelScope.launch {
            _softDetailState.value = UiState.Loading
            try {
                val result = AppRepository.getSoftModelInfo(appkey, softEnums)
                _softDetailState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _softDetailState.value = UiState.Error(e.message ?: "获取模块信息失败")
            }
        }
    }

    /** Saves module-specific configuration for [appkey] / [softEnums]. */
    fun saveSoftModelInfo(appkey: String, softEnums: SoftEnums, info: String) {
        viewModelScope.launch {
            _softDetailState.value = UiState.Loading
            try {
                val result = AppRepository.saveSoftModelInfo(appkey, softEnums, info)
                _softDetailState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _softDetailState.value = UiState.Error(e.message ?: "保存模块信息失败")
            }
        }
    }
}
