package halt.dragon.displayfeatures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import halt.dragon.displayfeatures.data.DisplayFeatureManager
import halt.dragon.displayfeatures.utils.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _hbmState = MutableStateFlow(false)
    val hbmState: StateFlow<Boolean> = _hbmState.asStateFlow()

    private val _dcDimmingState = MutableStateFlow(false)
    val dcDimmingState: StateFlow<Boolean> = _dcDimmingState.asStateFlow()

    private val _flashlightBrightness = MutableStateFlow(0)
    val flashlightBrightness: StateFlow<Int> = _flashlightBrightness.asStateFlow()

    // Track if root access is granted/checked
    private val _hasRoot = MutableStateFlow(false)
    val hasRoot: StateFlow<Boolean> = _hasRoot.asStateFlow()

    private val flashlightChannel = Channel<Int>(Channel.CONFLATED)

    init {
        checkRootAndRefresh()
        processFlashlightUpdates()
    }

    private fun checkRootAndRefresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val root = ShellUtils.checkRootAccess()
            _hasRoot.value = root
            if (root) {
                refreshStatus()
            }
        }
    }

    private fun processFlashlightUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            for (value in flashlightChannel) {
                if (_hasRoot.value) {
                    DisplayFeatureManager.setFlashlightBrightness(value)
                }
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_hasRoot.value) {
                _hbmState.value = DisplayFeatureManager.isHbmEnabled()
                _dcDimmingState.value = DisplayFeatureManager.isDcDimmingEnabled()
                _flashlightBrightness.value = DisplayFeatureManager.getFlashlightBrightness()
            }
        }
    }

    fun toggleHbm(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_hasRoot.value) {
                if (DisplayFeatureManager.setHbm(enabled)) {
                    _hbmState.value = enabled
                } else {
                    // Revert or show error (for now just refresh)
                    refreshStatus()
                }
            }
        }
    }

    fun toggleDcDimming(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_hasRoot.value) {
                if (DisplayFeatureManager.setDcDimming(enabled)) {
                    _dcDimmingState.value = enabled
                }
                // Refresh to confirm and sync other states
                refreshStatus()
            }
        }
    }

    fun setFlashlightBrightness(value: Int) {
        if (_hasRoot.value) {
            // Optimistic update for UI responsiveness
            _flashlightBrightness.value = value
            flashlightChannel.trySend(value)
        }
    }
}
