package com.example.testgeneratorphoto

data class artModel(var preview: String?, var styleName: String?, var guidance_scale: Byte?, var lora_model: String?, var model_id: String?, var prompt: String?, var negative_prompt: String?, var steps: Byte?, var lora_strength: Byte?) {

}