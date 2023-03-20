package com.edigleison.coffeestoreapi.repositories

import com.edigleison.coffeestoreapi.entities.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : JpaRepository<ProductEntity, UUID>
