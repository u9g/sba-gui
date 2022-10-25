package com.example.core

object FeatureManager {
    private var features: MutableSet<Feature> = mutableSetOf()

    fun addFeature(feature: Feature) = features.add(feature)

    val guiFeatures: List<Feature>
        get() = features.filter { it.isGuiFeature }
}