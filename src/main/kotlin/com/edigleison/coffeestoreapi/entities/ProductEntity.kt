package com.edigleison.coffeestoreapi.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
class ProductEntity(
    @Id
    var id: UUID,
    var name: String,
    var price: BigDecimal
)
