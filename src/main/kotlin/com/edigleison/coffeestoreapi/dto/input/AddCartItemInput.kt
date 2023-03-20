package com.edigleison.coffeestoreapi.dto.input

import java.util.*

class AddCartItemInput(
    val drinkId: UUID,
    val toppingsId: Set<UUID>
)

