package com.edigleison.coffeestoreapi.dto

import com.edigleison.coffeestoreapi.entities.CartEntity
import java.math.BigDecimal
import java.util.*

class Cart(
    var id: UUID
) {
    var items: List<CartItem> = mutableListOf()

    var discount: BigDecimal = BigDecimal.ZERO

    var amount: BigDecimal = BigDecimal.ZERO

}

fun CartEntity.toDto() = Cart(this.id).also {
    it.id = this.id
    it.items = this.items.map { item -> item.toDto() }
    it.amount = this.amount
    it.discount = this.discount
}
