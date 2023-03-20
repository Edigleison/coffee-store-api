package com.edigleison.coffeestoreapi.util

import java.math.BigDecimal

object Constants {
    val PROMOTION_BY_AMOUNT_MIN_VALUE = BigDecimal(12.00)
    val PROMOTION_BY_AMOUNT_PERCENTAGE_DISCOUNT = BigDecimal(0.25)
    const val PROMOTION_BY_QTY_OF_ITEMS_MIN_VALUE = 3

    const val CACHE_ALL_PRODUCTS = "all_products"
    const val CACHE_PRODUCT_BY_ID = "product_by_id"
}
