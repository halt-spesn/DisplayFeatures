package halt.dragon.displayfeatures.data

import halt.dragon.displayfeatures.utils.ShellUtils

object DisplayFeatureManager {

    private const val HBM_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/hbm"
    private const val DC_DIMMING_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/msm_fb_ea_enable"
    private const val LOW_FLASHLIGHT_NODE = "/sys/class/leds/led:torch/brightness"

    // Values to write. Some devices use "1" for on, others might differ.
    private const val ENABLE_VAL = "1"
    private const val DISABLE_VAL = "0"

    private var internalLowFlashlightState = false

    fun isHbmEnabled(): Boolean {
        val result = ShellUtils.readFromFile(HBM_NODE)
        return result.trim() == ENABLE_VAL
    }

    fun setHbm(enabled: Boolean): Boolean {
        val value = if (enabled) ENABLE_VAL else DISABLE_VAL
        return ShellUtils.writeToFile(HBM_NODE, value)
    }

    fun isDcDimmingEnabled(): Boolean {
        val result = ShellUtils.readFromFile(DC_DIMMING_NODE)
        return result.trim() == ENABLE_VAL
    }

    fun setDcDimming(enabled: Boolean): Boolean {
        val value = if (enabled) ENABLE_VAL else DISABLE_VAL
        return ShellUtils.writeToFile(DC_DIMMING_NODE, value)
    }

    fun isLowFlashlightEnabled(): Boolean {
        val result = ShellUtils.readFromFile(LOW_FLASHLIGHT_NODE)
        val fileState = try {
            // Try to parse as integer, seeing if it's > 0
            val intValue = result.trim().toInt()
            intValue > 0
        } catch (e: NumberFormatException) {
            // Fallback: literal checks
            result.trim() == "1"
        }

        return internalLowFlashlightState || fileState
    }

    fun setLowFlashlight(enabled: Boolean): Boolean {
        // If it's a brightness node, we might want '1' for low, '0' for off.
        val value = if (enabled) "1" else "0"
        val success = ShellUtils.writeToFile(LOW_FLASHLIGHT_NODE, value)
        if (success) {
            internalLowFlashlightState = enabled
        }
        return success
    }

    fun getHbmStatus(): String = ShellUtils.readFromFile(HBM_NODE).trim()
    fun getDcDimmingStatus(): String = ShellUtils.readFromFile(DC_DIMMING_NODE).trim()
    fun getLowFlashlightStatus(): String = ShellUtils.readFromFile(LOW_FLASHLIGHT_NODE).trim()
}
