package com.karpicki.locationscanner.geojson

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Feature(val properties: Properties, val geometry: Geometry) {
    val type: String = "Feature"
}