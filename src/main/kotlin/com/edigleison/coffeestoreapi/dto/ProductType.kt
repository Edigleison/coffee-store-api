package com.edigleison.coffeestoreapi.dto

import com.edigleison.coffeestoreapi.entities.ProductEntity
import com.edigleison.coffeestoreapi.entities.ToppingEntity

enum class ProductType {
    DRINK, TOPPING;

    companion object {
        @JvmStatic
        fun fromEntity(entity: ProductEntity): ProductType =
            when (entity) {
                is ToppingEntity -> TOPPING
                else -> DRINK
            }
    }
}
