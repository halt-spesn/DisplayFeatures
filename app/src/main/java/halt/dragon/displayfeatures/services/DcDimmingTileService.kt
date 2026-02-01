package halt.dragon.displayfeatures.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import halt.dragon.displayfeatures.data.DisplayFeatureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DcDimmingTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val isEnabled = qsTile.state == Tile.STATE_ACTIVE

        // Toggle
        CoroutineScope(Dispatchers.IO).launch {
            DisplayFeatureManager.setDcDimming(!isEnabled)
            updateTileState()
        }
    }

    private fun updateTileState() {
        CoroutineScope(Dispatchers.IO).launch {
            val isEnabled = DisplayFeatureManager.isDcDimmingEnabled()
            val tile = qsTile
            if (tile != null) {
                tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                tile.label = "DC Dimming"
                tile.updateTile()
            }
        }
    }
}
