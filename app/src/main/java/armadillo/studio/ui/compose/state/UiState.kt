package armadillo.studio.ui.compose.state

/**
 * Generic UI state used by ViewModels to expose loading / success / error / empty
 * states to Jetpack Compose screens.
 *
 * @param T the type of data carried by [Success]
 */
sealed class UiState<out T> {

    /** Represents an ongoing operation. */
    object Loading : UiState<Nothing>()

    /** Represents a successful result carrying [data]. */
    data class Success<T>(val data: T) : UiState<T>()

    /** Represents a failed operation with a human-readable [message]. */
    data class Error(val message: String) : UiState<Nothing>()

    /** Represents the initial / empty state before any data has been loaded. */
    object Empty : UiState<Nothing>()
}
