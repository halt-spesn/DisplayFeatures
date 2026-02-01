package halt.dragon.displayfeatures.data

import halt.dragon.displayfeatures.utils.ShellUtils

object DisplayFeatureManager {

    private const val HBM_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/hbm"
    private const val DC_DIMMING_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/msm_fb_ea_enable"

    // Values to write. Some devices use "1" for on, others might differ.
    private const val ENABLE_VAL = "1"
    private const val DISABLE_VAL = "0"

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
}
