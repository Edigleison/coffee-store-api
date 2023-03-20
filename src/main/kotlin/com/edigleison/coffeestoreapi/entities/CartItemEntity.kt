package com.edigleison.coffeestoreapi.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "cart_items")
class CartItemEntity(
    @Id
    var id: UUID,

    @ManyToOne()
    var drink: DrinkEntity,

    @ManyToMany(
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY
    )
    @JoinTable(
        name = "cart_item_toppings",
        joinColumns = [JoinColumn(name = "cart_item_id")],
        inverseJoinColumns = [JoinColumn(name = "topping_id")],
    )
    var toppings: MutableSet<ToppingEntity> = mutableSetOf(),

    @ManyToOne
    var cart: CartEntity
)
