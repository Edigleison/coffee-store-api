package com.edigleison.coffeestoreapi.util

import com.edigleison.coffeestoreapi.entities.CartEntity
import com.edigleison.coffeestoreapi.entities.CartItemEntity
import com.edigleison.coffeestoreapi.entities.DrinkEntity
import com.edigleison.coffeestoreapi.entities.ToppingEntity
import java.math.BigDecimal
import java.util.*

object EntityFaker {
    fun cart(
        id: UUID = UUID.randomUUID(),
        discount: BigDecimal = BigDecimal.ZERO,
        amount: BigDecimal = BigDecimal.ZERO,
        items: MutableList<CartItemEntity> = mutableListOf()
    ) = CartEntity(id).apply {
        this.items = items
        this.discount = discount
        this.amount = amount
    }

    fun cartItem(
        id: UUID = UUID.randomUUID(),
        drink: DrinkEntity = drink(),
        toppings: MutableSet<ToppingEntity> = mutableSetOf(topping()),
        cart: CartEntity
    ) = CartItemEntity(
        id = id,
        drink = drink,
        toppings = toppings,
        cart = cart
    )

    fun topping(
        id: UUID = UUID.randomUUID(),
        name: String = Faker.instance.food().dish(),
        price: BigDecimal = BigDecimal(1.00)
    ) = ToppingEntity(
        id = id,
        name = name,
        price = price
    )

    fun drink(
        id: UUID = UUID.randomUUID(),
        name: String = Faker.instance.food().dish(),
        price: BigDecimal = BigDecimal(5.00)
    ) = DrinkEntity(
        id = id,
        name = name,
        price = price
    )
}
