package com.karpicki.locationscanner.geojson

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class SyncData(val timestamp: Long) {
}