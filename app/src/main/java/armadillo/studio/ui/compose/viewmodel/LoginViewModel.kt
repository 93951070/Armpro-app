package armadillo.studio.ui.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armadillo.studio.model.Basic
import armadillo.studio.ui.compose.repository.AppRepository
import armadillo.studio.ui.compose.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for authentication flows: account/password login,
 * registration, and QQ third-party login.
 *
 * The UI observes [loginState] to render loading / success / error states.
 */
class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Basic>>(UiState.Empty)
    val loginState: StateFlow<UiState<Basic>> = _loginState.asStateFlow()

    /** Account + password login. */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val result = AppRepository.login(username, password)
                _loginState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "登录失败")
            }
        }
    }

    /** Account + password + email registration. */
    fun register(username: String, password: String, email: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val result = AppRepository.register(username, password, email)
                _loginState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "注册失败")
            }
        }
    }

    /** QQ third-party login via openid. */
    fun loginWithQQ(openid: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val result = AppRepository.loginWithQQ(openid)
                _loginState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "QQ登录失败")
            }
        }
    }
}
