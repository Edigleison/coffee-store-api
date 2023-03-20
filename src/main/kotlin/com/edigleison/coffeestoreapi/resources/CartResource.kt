package com.edigleison.coffeestoreapi.resources

import com.edigleison.coffeestoreapi.config.exceptions.ErrorResponse
import com.edigleison.coffeestoreapi.dto.Cart
import com.edigleison.coffeestoreapi.dto.input.AddCartItemInput
import com.edigleison.coffeestoreapi.dto.input.CreateCartInput
import com.edigleison.coffeestoreapi.dto.input.EditCartItemInput
import com.edigleison.coffeestoreapi.dto.toDto
import com.edigleison.coffeestoreapi.exceptions.NotFound
import com.edigleison.coffeestoreapi.services.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@Tag(name = "cart", description = "The cart API")
@RestController
@RequestMapping(path = ["/carts"], produces = ["application/json"])
class CartResource(
    private val cartService: CartService
) {

    @Operation(summary = "Find cart by id", description = "Returns a single cart", tags = ["cart"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(
                responseCode = "404",
                description = "Cart not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Cart =
        cartService.findById(id)?.let {
            it.toDto()
        } ?: throw NotFound("Cart not found!")

    @Operation(
        summary = "Create a cart",
        description = "Save a new cart with the given drink and toppings",
        tags = ["cart"]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(
                responseCode = "404",
                description = "Drink not found or Topping not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
        ]
    )
    @PostMapping
    fun createCart(
        @RequestBody input: CreateCartInput,
        request: HttpServletRequest
    ): ResponseEntity<Cart> =
        cartService.create(
            drinkId = input.drinkId,
            toppingsId = input.toppingsId,
        ).let {
            val location = URI.create(request.requestURL.append("/").append(it.id).toString())
            ResponseEntity.created(location).body(it.toDto())
        }

    @Operation(
        summary = "Add item",
        description = "Add a new item to the cart with the given drink and toppings",
        tags = ["cart"]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(
                responseCode = "404",
                description = "Cart not found, Drink not found or Topping not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
        ]
    )
    @PostMapping("/{id}/items")
    fun addItem(
        @PathVariable id: UUID,
        @RequestBody input: AddCartItemInput,
        request: HttpServletRequest
    ): ResponseEntity<Cart> =
        cartService.addItem(
            cartId = id,
            drinkId = input.drinkId,
            toppingsId = input.toppingsId
        ).let {
            val location = URI.create(request.requestURL.append("/").append(it.id).toString())
            ResponseEntity.created(location).body(it.toDto())
        }

    @Operation(summary = "Edit item", description = "Changes the toppings of the given item", tags = ["cart"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(
                responseCode = "404",
                description = "Cart not found, Cart item not found or Topping not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
        ]
    )
    @PatchMapping("/{cartId}/items/{itemId}")
    fun editItem(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody input: EditCartItemInput
    ): Cart =
        cartService.editItem(
            cartId = cartId,
            itemId = itemId,
            toppingsId = input.toppingsId
        ).let {
            it.toDto()
        }

    @Operation(summary = "Remove item", description = "Remove the given item from the cart", tags = ["cart"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(
                responseCode = "404",
                description = "Cart not found or Cart item not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
        ]
    )
    @DeleteMapping("/{cartId}/items/{itemId}")
    fun removeItem(@PathVariable cartId: UUID, @PathVariable itemId: UUID): Cart =
        cartService.removeItem(
            cartId = cartId,
            itemId = itemId
        ).let {
            it.toDto()
        }
}
