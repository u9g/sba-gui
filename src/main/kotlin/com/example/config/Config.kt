package com.example.config

import com.example.core.Feature

object Config {
    const val GUI_SCALE_MINIMUM: Float = 0.5f;
    const val GUI_SCALE_MAXIMUM: Float = 5f;
    private const val DEFAULT_GUI_SCALE: Float = 1f; // 0.5 - 5
    var guiScale = DEFAULT_GUI_SCALE
    var textStyle = TextStyle.STYLE_ONE
    object Chroma {
        var mode: ChromaMode = ChromaMode.FADE
        var useNewMode = true
        var size: Float = 30f
        var speed: Float = 6f
        var saturation = 0.75f
        var brightness = 0.9f
    }
    const val isNewChromaEnabled = true

    enum class TextStyle(private val TRANSLATION_KEY: String) {
        STYLE_ONE("settings.textStyles.one"),
        STYLE_TWO("settings.textStyles.two");

        val message: String
            get() = throw AssertionError() //Translations.getMessage(TRANSLATION_KEY)
        val nextType: TextStyle
            get() {
                var nextType = ordinal + 1
                if (nextType > TextStyle.values().size - 1) {
                    nextType = 0
                }
                return TextStyle.values()[nextType]
            }
    }

    enum class ChromaMode(private val TRANSLATION_KEY: String) {
        ALL_SAME_COLOR("settings.chromaModes.allTheSame"), FADE("settings.chromaModes.fade");

        val message: String
            get() = throw AssertionError()//Translations.getMessage(TRANSLATION_KEY)
        val nextType: ChromaMode
            get() {
                var nextType = ordinal + 1
                if (nextType > values().size - 1) {
                    nextType = 0
                }
                return ChromaMode.values()[nextType]
            }
    }

    enum class AnchorPoint(val id: Int) {
        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3),
        BOTTOM_MIDDLE(4);

        fun getX(maxX: Int): Int {
            var x = 0
            when (this) {
                TOP_RIGHT, BOTTOM_RIGHT -> x = maxX
                BOTTOM_MIDDLE -> x = maxX / 2
                else -> {}
            }
            return x
        }

        fun getY(maxY: Int): Int {
            var y = 0
            when (this) {
                BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_MIDDLE -> y = maxY
                else -> {}
            }
            return y
        }

        companion object {
            // Accessed by reflection...
//            fun fromId(id: Int): AnchorPoint? {
//                for (feature in AnchorPoint.values()) {
//                    if (feature.id == id) {
//                        return feature
//                    }
//                }
//                return null
//            }
        }
    }
}