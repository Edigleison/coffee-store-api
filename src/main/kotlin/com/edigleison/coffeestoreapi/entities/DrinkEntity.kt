package com.edigleison.coffeestoreapi.entities

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.math.BigDecimal
import java.util.*

@Entity
@DiscriminatorValue("drink")
class DrinkEntity(
    id: UUID,
    name: String,
    price: BigDecimal
) : ProductEntity(
    id = id,
    name = name,
    price = price
)
