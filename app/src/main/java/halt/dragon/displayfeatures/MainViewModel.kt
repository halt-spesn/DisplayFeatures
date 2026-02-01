package halt.dragon.displayfeatures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import halt.dragon.displayfeatures.data.DisplayFeatureManager
import halt.dragon.displayfeatures.utils.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _hbmState = MutableStateFlow(false)
    val hbmState: StateFlow<Boolean> = _hbmState.asStateFlow()

    private val _dcDimmingState = MutableStateFlow(false)
    val dcDimmingState: StateFlow<Boolean> = _dcDimmingState.asStateFlow()

    private val _lowFlashlightState = MutableStateFlow(false)
    val lowFlashlightState: StateFlow<Boolean> = _lowFlashlightState.asStateFlow()

    private val _hbmStatusValue = MutableStateFlow("")
    val hbmStatusValue: StateFlow<String> = _hbmStatusValue.asStateFlow()

    private val _dcDimmingStatusValue = MutableStateFlow("")
    val dcDimmingStatusValue: StateFlow<String> = _dcDimmingStatusValue.asStateFlow()

    private val _lowFlashlightStatusValue = MutableStateFlow("")
    val lowFlashlightStatusValue: StateFlow<String> = _lowFlashlightStatusValue.asStateFlow()

    // Track if root access is granted/checked
    private val _hasRoot = MutableStateFlow(false)
    val hasRoot: StateFlow<Boolean> = _hasRoot.asStateFlow()

    init {
        checkRootAndRefresh()
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

    fun refreshStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_hasRoot.value) {
                _hbmState.value = DisplayFeatureManager.isHbmEnabled()
                _hbmStatusValue.value = DisplayFeatureManager.getHbmStatus()

                _dcDimmingState.value = DisplayFeatureManager.isDcDimmingEnabled()
                _dcDimmingStatusValue.value = DisplayFeatureManager.getDcDimmingStatus()

                _lowFlashlightState.value = DisplayFeatureManager.isLowFlashlightEnabled()
                _lowFlashlightStatusValue.value = DisplayFeatureManager.getLowFlashlightStatus()
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

    fun toggleLowFlashlight(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_hasRoot.value) {
                if (DisplayFeatureManager.setLowFlashlight(enabled)) {
                    _lowFlashlightState.value = enabled
                }
                refreshStatus()
            }
        }
    }
}
