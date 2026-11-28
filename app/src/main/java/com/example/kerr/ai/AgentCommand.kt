package com.example.kerr.ai

data class AgentCommand(val intent: AgentIntent, val productQuery: String,
                        val quantity: String="1",
                        val targetApps: List<DeliveryApp> =
listOf(DeliveryApp.BLINKIT, DeliveryApp.ZEPTO,
DeliveryApp.INSTAMART, DeliveryApp.BIGBASKET
),
                        val comparePrices: Boolean=true)

    enum class AgentIntent {
        COMPARE_AND_BUY,
        SEARCH_ONLY,
        OPEN_APP,
        UNKNOWN
    }

    enum class DeliveryApp(val packageName: String, val displayName: String) {
        BLINKIT("com.grofers.customerapp", "Blinkit"),
        ZEPTO("com.zeptoconsumerapp", "Zepto"),
        INSTAMART("in.swiggy.android", "Swiggy Instamart"),
        BIGBASKET("com.bigbasket.mobileapp", "BigBasket")
    }
