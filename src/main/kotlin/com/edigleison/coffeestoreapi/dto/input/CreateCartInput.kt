package com.edigleison.coffeestoreapi.dto.input

import java.util.*

class CreateCartInput(
    val drinkId: UUID,
    val toppingsId: Set<UUID>
)

