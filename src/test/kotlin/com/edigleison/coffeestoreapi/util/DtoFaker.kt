package com.edigleison.coffeestoreapi.util

import com.edigleison.coffeestoreapi.dto.Product
import com.edigleison.coffeestoreapi.dto.ProductType
import java.math.BigDecimal
import java.util.*

object DtoFaker {
    fun product(
        id: UUID? = UUID.randomUUID(),
        name: String = Faker.instance.food().dish(),
        price: BigDecimal = BigDecimal(5.00),
        type: ProductType = ProductType.DRINK
    ) = Product(
        id = id,
        name = name,
        price = price,
        type = type
    )
}
