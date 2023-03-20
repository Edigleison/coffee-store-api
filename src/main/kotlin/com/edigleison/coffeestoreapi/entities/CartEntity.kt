package com.edigleison.coffeestoreapi.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "carts")
class CartEntity(
    @Id val id: UUID,
) {
    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    var items: MutableList<CartItemEntity> = mutableListOf()

    var discount: BigDecimal = BigDecimal.ZERO

    var amount: BigDecimal = BigDecimal.ZERO
}
