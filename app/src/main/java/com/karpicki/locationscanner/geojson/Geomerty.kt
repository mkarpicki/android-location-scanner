package com.karpicki.locationscanner.geojson

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Geometry(val coordinates: List<Double>) {
    val type:String = "Point"
}