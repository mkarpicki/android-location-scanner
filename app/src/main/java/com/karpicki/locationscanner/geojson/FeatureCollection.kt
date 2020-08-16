package com.karpicki.locationscanner.geojson

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class FeatureCollection(val features: List<Feature>) {
    val type: String = "FeatureCollection"
}