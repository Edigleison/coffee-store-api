package com.edigleison.coffeestoreapi.services

import com.edigleison.coffeestoreapi.entities.ProductEntity
import com.edigleison.coffeestoreapi.exceptions.NotFound
import com.edigleison.coffeestoreapi.repositories.ProductRepository
import com.edigleison.coffeestoreapi.util.Constants.CACHE_ALL_PRODUCTS
import com.edigleison.coffeestoreapi.util.Constants.CACHE_PRODUCT_BY_ID
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService(
    private val repository: ProductRepository,
) {
    @Cacheable(CACHE_PRODUCT_BY_ID, key = "#id")
    fun findById(id: UUID): ProductEntity? =
        repository.findByIdOrNull(id)

    @Cacheable(CACHE_ALL_PRODUCTS)
    fun findAll(): List<ProductEntity> =
        repository.findAll()

    @CacheEvict(CACHE_ALL_PRODUCTS, allEntries = true)
    fun create(product: ProductEntity): ProductEntity =
        repository.save(product)

    @Caching(
        evict = [
            CacheEvict(CACHE_PRODUCT_BY_ID, key = "#id"),
            CacheEvict(CACHE_ALL_PRODUCTS, allEntries = true)
        ]
    )
    fun update(id: UUID, product: ProductEntity): ProductEntity =
        repository.findByIdOrNull(id)?.let {
            it.name = product.name
            it.price = product.price

            repository.save(it)
        } ?: throw NotFound("Product not found!")

    @Caching(
        evict = [
            CacheEvict(CACHE_PRODUCT_BY_ID, key = "#id"),
            CacheEvict(CACHE_ALL_PRODUCTS, allEntries = true)
        ]
    )
    fun delete(id: UUID) =
        repository.findByIdOrNull(id)?.let {
            repository.delete(it)
        } ?: throw NotFound("Product not found!")
}
