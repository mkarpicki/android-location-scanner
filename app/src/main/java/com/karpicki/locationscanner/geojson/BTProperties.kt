package com.karpicki.locationscanner.geojson

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class BTProperties(
    val name: String,
    val rssi: Int,
    val timestamp: Long
    ) : Properties {
}