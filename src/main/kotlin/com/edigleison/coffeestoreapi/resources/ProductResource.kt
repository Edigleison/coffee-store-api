package com.edigleison.coffeestoreapi.resources

import com.edigleison.coffeestoreapi.config.exceptions.ErrorResponse
import com.edigleison.coffeestoreapi.dto.Product
import com.edigleison.coffeestoreapi.dto.toDto
import com.edigleison.coffeestoreapi.dto.toEntity
import com.edigleison.coffeestoreapi.exceptions.NotFound
import com.edigleison.coffeestoreapi.services.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@Tag(name = "product", description = "The product API")
@RestController
@RequestMapping(path = ["/products"], produces = ["application/json"])
class ProductResource(private val service: ProductService) {

    @Operation(summary = "Find product by id", description = "Returns a single product", tags = ["product"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Product =
        service.findById(id)?.let {
            it.toDto()
        } ?: throw NotFound("Product not found!")

    @Operation(summary = "Get products", description = "Returns a product collection", tags = ["product"])
    @GetMapping
    fun findAll(): List<Product> =
        service.findAll().let { allProducts ->
            allProducts.map { it.toDto() }
        }

    @Operation(summary = "Create product", description = "Save a new product", tags = ["product"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created")
        ]
    )
    @PostMapping
    fun create(@RequestBody @Valid product: Product, request: HttpServletRequest): ResponseEntity<Product> =
        service.create(product.toEntity()).let {
            val location = URI.create(request.requestURL.append("/").append(it.id).toString())
            ResponseEntity.created(location).body(it.toDto())
        }

    @Operation(summary = "Update product", description = "Update product information", tags = ["product"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody @Valid product: Product): Product =
        service.update(id, product.toEntity()).let {
            it.toDto()
        }

    @Operation(summary = "Delete product", description = "Remove product from the database", tags = ["product"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Successful operation (No Content)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) =
        service.delete(id)

}
