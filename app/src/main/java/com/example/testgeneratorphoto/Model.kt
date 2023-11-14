package com.example.testgeneratorphoto

data class Model(val preview: Any, val previewHeader: Any, val styleName: String, val count: Byte, val modelId: String, val strength: Double,
                 val  prompt: String, val negativePrompt: String, val controlModel: String, val steps: Byte, val lora: String, var category : String)