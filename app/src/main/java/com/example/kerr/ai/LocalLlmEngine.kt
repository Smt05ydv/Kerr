package com.example.kerr.ai

import org.json.JSONObject
import android.util.Log.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class LocalLlmEngine(){
    fun buildQwenPrompt(userCommand: String): String{
 return """
        <|im_start|>system
        Convert the user's request into valid JSON ONLY.No extra Text
        
       Schema: 
        {"intent": "COMPARE_AND_BUY", "productQuery": "string",
         "quantity": "1", "targetApps": ["BLINKIT",
          "ZEPTO", "INSTAMART", "BIGBASKET"],
           "comparePrices": true}
        <|im_end|>
        
        <|im_start|>user
        $userCommand
        <|im_end|>,

        <|im_start|>assistant
        """.trimIndent()


    }

    private fun parseJsontoCommand
                (jsonString: String,fallbackQuery: String): AgentCommand{
            return  try {
                 val obj= JSONObject(jsonString)
                  val intentStr=obj.optString("intent","COMPARE_AND_BUY")
                  val intent = runCatching {
                      AgentIntent.valueOf(intentStr)
                  }.getOrDefault(AgentIntent.UNKNOWN)
                  val product=obj.optString("productQuery",fallbackQuery)
                  val quantity=obj.optString("quantity","1")
                  val compare=obj.optBoolean("comparePrices",true)
                  val appsArray=obj.optJSONArray("targetApps")
                val apps = mutableListOf<DeliveryApp>()
                if (appsArray != null) {
                    for (i in 0 until appsArray.length()) {
                        val appName = appsArray.optString(i)
                        runCatching { DeliveryApp.valueOf(appName) }
                            .getOrNull()
                            ?.let { apps.add(it) }
                    }
                }
                AgentCommand(
                    intent = intent,
                    productQuery = product,
                    quantity = quantity,
                    targetApps = apps.ifEmpty {
                        listOf(
                            DeliveryApp.BLINKIT,
                            DeliveryApp.ZEPTO,
                            DeliveryApp.INSTAMART,
                            DeliveryApp.BIGBASKET
                        )
                    },
                    comparePrices = compare
                )
            }
              catch (e:Exception){
                  AgentCommand(intent = AgentIntent.UNKNOWN,
                     productQuery = fallbackQuery)
              }
    }

    suspend fun decideAction(userCommand: String):
            AgentCommand= withContext(Dispatchers.Default){
        val prompt= buildQwenPrompt(userCommand)
        val rawJsonOutput=simulatedInference(userCommand)
        parseJsontoCommand(rawJsonOutput,userCommand)
    }

    private suspend fun simulatedInference(command: String): String {
        delay(350)
        val lower = command.lowercase()

        val apps = buildList {
            if ("blinkit" in lower) add("\"BLINKIT\"")
            if ("zepto" in lower) add("\"ZEPTO\"")
            if ("instamart" in lower || "swiggy" in lower) add("\"INSTAMART\"")
            if ("bigbasket" in lower || "bb" in lower) add("\"BIGBASKET\"")
        }.ifEmpty {
            listOf("\"BLINKIT\"", "\"ZEPTO\"", "\"INSTAMART\"", "\"BIGBASKET\"")
        }

        val cleanedProduct = command
            .replace(Regex("(?i)\\b(order|buy|find|check|cheapest|on blinkit|on zepto|on instamart|on bigbasket|from)\\b"), "")
            .trim()
            .ifEmpty { command }

        return """
            {
              "intent": "COMPARE_AND_BUY",
              "productQuery": "$cleanedProduct",
              "quantity": "1",
              "targetApps": [${apps.joinToString(",")}],
              "comparePrices": ${apps.size > 1}
            }
        """.trimIndent()
    }
}

