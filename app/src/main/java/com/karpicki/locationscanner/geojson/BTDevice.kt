package com.karpicki.locationscanner.geojson

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class BTDevice(val address: String, val name: String) {
}