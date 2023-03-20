package com.edigleison.coffeestoreapi.repositories

import com.edigleison.coffeestoreapi.entities.CartEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CartRepository : JpaRepository<CartEntity, UUID> {
}
