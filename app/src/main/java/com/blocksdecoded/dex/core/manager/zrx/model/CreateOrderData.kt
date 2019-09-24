package com.blocksdecoded.dex.core.manager.zrx.model

import com.blocksdecoded.dex.presentation.orders.model.EOrderSide
import java.math.BigDecimal

data class CreateOrderData(
    val coinPair: Pair<String, String>,
    val side: EOrderSide,
    val amount: BigDecimal,
    val price: BigDecimal
)