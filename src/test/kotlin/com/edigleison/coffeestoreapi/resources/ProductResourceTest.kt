package com.edigleison.coffeestoreapi.resources

import com.edigleison.coffeestoreapi.dto.Product
import com.edigleison.coffeestoreapi.dto.ProductType
import com.edigleison.coffeestoreapi.dto.toDto
import com.edigleison.coffeestoreapi.exceptions.NotFound
import com.edigleison.coffeestoreapi.services.ProductService
import com.edigleison.coffeestoreapi.util.Faker
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.*
import java.util.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(ProductResource::class)
class ProductResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var service: ProductService

    @Autowired
    lateinit var mapper: ObjectMapper

    @Test
    fun `given an existing product id when find by id then returns the product`() {
        val existingProduct = Faker.entity.drink()
        `when`(service.findById(existingProduct.id)).thenReturn(existingProduct)

        val expected = existingProduct.toDto()

        mockMvc.get("/products/{id}", existingProduct.id) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(mapper.writeValueAsString(expected)) }
        }
    }

    @Test
    fun `given a non existing product id when find by id then returns not found`() {
        val nonExistingProductId = UUID.randomUUID()
        `when`(service.findById(nonExistingProductId)).thenReturn(null)

        mockMvc.get("/products/{id}", nonExistingProductId) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { jsonPath("$.message", `is`("Product not found!")) }
        }
    }

    @Test
    fun `given some existing products when list all then returns the list of all existing products`() {
        val existingProducts = listOf(
            Faker.entity.drink(),
            Faker.entity.topping()
        )
        `when`(service.findAll()).thenReturn(existingProducts)

        val expected = existingProducts.map { it.toDto() }

        mockMvc.get("/products") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(mapper.writeValueAsString(expected)) }
        }
    }

    @Test
    fun `given no existing products when list all then returns an empty list`() {
        `when`(service.findAll()).thenReturn(emptyList())

        val expected = emptyList<Product>()

        mockMvc.get("/products") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(mapper.writeValueAsString(expected)) }
        }
    }

    @Test
    fun `given a new product when create then returns ok`() {
        val input = Faker.dto.product(id = null, type = ProductType.DRINK)
        val createdEntity = Faker.entity.drink(
            name = input.name,
            price = input.price
        )
        `when`(service.create(any())).thenReturn(createdEntity)

        val expected = createdEntity.toDto()

        mockMvc.post("/products") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(input)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { json(mapper.writeValueAsString(expected)) }
        }
    }

    @Test
    fun `given a product to update when update then returns ok`() {
        val input = Faker.dto.product(type = ProductType.DRINK)
        val entity = Faker.entity.drink(
            id = input.id!!,
            name = input.name,
            price = input.price,
        )
        `when`(service.update(any(), any())).thenReturn(entity)

        mockMvc.put("/products/{id}", input.id) {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(input)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(mapper.writeValueAsString(input)) }
        }
    }

    @Test
    fun `given a product to update when service throw NotFound exception on update then returns not found`() {
        val productToUpdate = Faker.dto.product()
        `when`(service.update(any(), any())).thenThrow(NotFound("Product not found!"))

        mockMvc.put("/products/{id}", productToUpdate.id) {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(productToUpdate)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { jsonPath("$.message", `is`("Product not found!")) }
        }
    }

    @Test
    fun `given a product to delete when delete then returns success with no content`() {
        val input = Faker.dto.product(type = ProductType.DRINK)

        mockMvc.delete("/products/{id}", input.id).andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `given a product to delete when service throw NotFound exception on update then returns not found`() {
        val input = Faker.dto.product()
        `when`(service.delete(input.id!!)).thenThrow(NotFound("Product not found!"))

        mockMvc.delete("/products/{id}", input.id).andExpect {
            status { isNotFound() }
            content { jsonPath("$.message", `is`("Product not found!")) }
        }
    }

}
