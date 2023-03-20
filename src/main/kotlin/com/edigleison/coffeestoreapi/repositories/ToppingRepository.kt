package com.edigleison.coffeestoreapi.repositories

import com.edigleison.coffeestoreapi.entities.ToppingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ToppingRepository : JpaRepository<ToppingEntity, UUID>
