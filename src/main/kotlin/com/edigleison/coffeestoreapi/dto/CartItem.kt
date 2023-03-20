package com.edigleison.coffeestoreapi.dto

import com.edigleison.coffeestoreapi.entities.CartItemEntity
import java.util.*

class CartItem(
    val id: UUID,
    var drink: Product
) {
    var toppings: List<Product> = mutableListOf()
}

fun CartItemEntity.toDto() = CartItem(
    id = this.id,
    drink = this.drink.toDto()
).also {
    it.toppings = this.toppings.map { topping -> topping.toDto() }
}
