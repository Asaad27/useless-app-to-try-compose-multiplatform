package com.asaad27.life.utils


import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone Light",
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Tablet Dark",
    device = Devices.PIXEL_FOLD,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
annotation class AndroidDevicesPreview