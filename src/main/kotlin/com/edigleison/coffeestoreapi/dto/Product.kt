package com.edigleison.coffeestoreapi.dto

import com.edigleison.coffeestoreapi.entities.DrinkEntity
import com.edigleison.coffeestoreapi.entities.ProductEntity
import com.edigleison.coffeestoreapi.entities.ToppingEntity
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.util.*

class Product(
    var id: UUID?,
    @field:NotBlank(message = "Name is mandatory")
    var name: String,
    @field:Min(value = 0, message = "Price cannot be negative")
    var price: BigDecimal,
    var type: ProductType
)

fun Product.toEntity(): ProductEntity =
    when (this.type) {
        ProductType.DRINK -> DrinkEntity(
            id = UUID.randomUUID(),
            name = this.name,
            price = this.price
        )

        ProductType.TOPPING -> ToppingEntity(
            id = UUID.randomUUID(),
            name = this.name,
            price = this.price
        )
    }

fun ProductEntity.toDto(): Product = Product(
    id = this.id,
    name = this.name,
    price = this.price,
    type = ProductType.fromEntity(this)
)


