package com.aethornex.location

data class PersonBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val score: Float
)

object DetectionStore {
    @Volatile
    var boxes: List<PersonBox> = emptyList()

    @Volatile
    var aiEnabled: Boolean = false

    @Volatile
    var modelStatus: String = "MODEL: LOADING"

    @Volatile
    var inferenceCount: Int = 0
}
