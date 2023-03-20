package com.edigleison.coffeestoreapi.services

import com.edigleison.coffeestoreapi.entities.CartEntity
import com.edigleison.coffeestoreapi.entities.CartItemEntity
import com.edigleison.coffeestoreapi.entities.ToppingEntity
import com.edigleison.coffeestoreapi.exceptions.NotFound
import com.edigleison.coffeestoreapi.repositories.CartRepository
import com.edigleison.coffeestoreapi.repositories.DrinkRepository
import com.edigleison.coffeestoreapi.repositories.ToppingRepository
import com.edigleison.coffeestoreapi.util.Constants
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val drinkRepository: DrinkRepository,
    private val toppingRepository: ToppingRepository
) {
    fun findById(id: UUID): CartEntity? =
        cartRepository.findByIdOrNull(id)

    fun create(
        drinkId: UUID,
        toppingsId: Set<UUID>
    ): CartEntity {
        val drink = drinkRepository.findByIdOrNull(drinkId) ?: throw NotFound("Drink not found!")

        val toppings = findAllToppingsOrThrow(toppingsId)

        val cart = CartEntity(UUID.randomUUID()).apply {
            items.add(
                CartItemEntity(
                    id = UUID.randomUUID(),
                    drink = drink,
                    toppings = toppings.toMutableSet(),
                    cart = this
                )
            )
        }

        updateAmountAndDiscount(cart)
        return cartRepository.save(cart)
    }

    fun addItem(
        cartId: UUID,
        drinkId: UUID,
        toppingsId: Set<UUID>
    ): CartEntity =
        cartRepository.findByIdOrNull(cartId)?.let { cart ->
            val drink = drinkRepository.findByIdOrNull(drinkId) ?: throw NotFound("Drink not found!")

            val toppings = findAllToppingsOrThrow(toppingsId)

            cart.items.add(
                CartItemEntity(
                    id = UUID.randomUUID(),
                    drink = drink,
                    toppings = toppings.toMutableSet(),
                    cart = cart
                )
            )
            updateAmountAndDiscount(cart)
            return cartRepository.save(cart)
        } ?: throw NotFound("Cart not found!")

    fun editItem(
        cartId: UUID,
        itemId: UUID,
        toppingsId: Set<UUID>
    ): CartEntity {
        val cart: CartEntity = cartRepository.findByIdOrNull(cartId)
            ?: throw NotFound("Cart not found!")

        cart.items.firstOrNull { it.id == itemId }?.let { item ->
            val toppings = findAllToppingsOrThrow(toppingsId)
            item.toppings = toppings.toMutableSet()
            updateAmountAndDiscount(cart)
            return cartRepository.save(cart)
        } ?: throw NotFound("Cart item not found!")
    }

    fun removeItem(
        cartId: UUID,
        itemId: UUID
    ): CartEntity =
        cartRepository.findByIdOrNull(cartId)?.let { cart ->
            val item = cart.items.find { it.id == itemId } ?: throw NotFound("Cart item not found!")
            cart.items.remove(item)
            updateAmountAndDiscount(cart)
            cartRepository.save(cart)
        } ?: throw NotFound("Cart not found!")

    private fun findAllToppingsOrThrow(toppingsId: Set<UUID>): MutableList<ToppingEntity> {
        val toppings = toppingRepository.findAllById(toppingsId)
        if (!toppings.map { it.id }.containsAll(toppingsId)) {
            throw NotFound("Topping not found!")
        }
        return toppings
    }

    private fun updateAmountAndDiscount(cart: CartEntity) {
        val drinksAmount = cart.items.sumOf { it.drink.price }
        val toppingsAmount = cart.items.flatMap { it.toppings }.sumOf { it.price }
        val grossAmount = drinksAmount.plus(toppingsAmount)

        val discountByAmount = calculateDiscountByAmount(grossAmount)
        val discountByQuantityOfItems = calculateDiscountByQuantityOfItems(cart.items)
        if (discountByAmount > discountByQuantityOfItems) {
            cart.discount = discountByAmount.setScale(2)
        } else {
            cart.discount = discountByQuantityOfItems.setScale(2)
        }

        cart.amount = grossAmount.minus(cart.discount).setScale(2)
    }

    private fun calculateDiscountByAmount(amount: BigDecimal): BigDecimal {
        if (amount >= Constants.PROMOTION_BY_AMOUNT_MIN_VALUE) {
            return amount.multiply(Constants.PROMOTION_BY_AMOUNT_PERCENTAGE_DISCOUNT)
        }
        return BigDecimal.ZERO
    }

    private fun calculateDiscountByQuantityOfItems(items: MutableList<CartItemEntity>): BigDecimal {
        if (items.size >= Constants.PROMOTION_BY_QTY_OF_ITEMS_MIN_VALUE) {
            return items.minOf {
                it.drink.price.add(
                    it.toppings.sumOf { t -> t.price }
                )
            }
        }
        return BigDecimal.ZERO
    }
}
