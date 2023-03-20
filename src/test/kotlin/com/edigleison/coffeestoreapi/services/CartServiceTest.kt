package com.edigleison.coffeestoreapi.services

import com.edigleison.coffeestoreapi.entities.CartEntity
import com.edigleison.coffeestoreapi.entities.DrinkEntity
import com.edigleison.coffeestoreapi.entities.ToppingEntity
import com.edigleison.coffeestoreapi.exceptions.NotFound
import com.edigleison.coffeestoreapi.repositories.CartRepository
import com.edigleison.coffeestoreapi.repositories.DrinkRepository
import com.edigleison.coffeestoreapi.repositories.ToppingRepository
import com.edigleison.coffeestoreapi.util.Constants
import com.edigleison.coffeestoreapi.util.Faker
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockKExtension::class)
class CartServiceTest {
    @MockK
    private lateinit var cartRepository: CartRepository

    @MockK
    private lateinit var drinkRepository: DrinkRepository

    @MockK
    private lateinit var toppingRepository: ToppingRepository

    @InjectMockKs
    private lateinit var service: CartService

    @BeforeEach
    fun init() {
        every { cartRepository.save(any()) } returnsArgument 0
    }

    @Test
    fun `given an existing cart when calls findById then retrieves the cart from the database`() {
        val id = UUID.randomUUID();
        val cart = givenAnExistingCartEligibleToPromotionByAmount(id)

        val result = service.findById(id)

        expectThat(result).isEqualTo(cart)
    }

    @Test
    fun `given an non existing cart when calls findById then returns null`() {
        val id = UUID.randomUUID()
        givenANonExistingCart(id)

        val result = service.findById(id)

        expectThat(result).isNull()
    }


    @Test
    fun `given a drink and some toppings when calls create then creates a cart with one item`() {
        val drink = givenAnExistingDrink(price = BigDecimal(4))
        val toppings = givenSomeExistingToppings(3, price = BigDecimal(2))
        val toppingsId = toppings.map { it.id }.toSet()

        val result = service.create(drinkId = drink.id, toppingsId = toppingsId)

        expectThat(result) {
            get { this.items }.single().and {
                get { this.drink.id }.isEqualTo(drink.id)
                get { this.toppings.map { it.id } }.contains(toppingsId)
            }
            get { this.amount }.isEqualTo(BigDecimal(10).setScale(2))
            get { this.discount }.isEqualTo(BigDecimal.ZERO.setScale(2))
        }

        verify(exactly = 1) {
            cartRepository.save(any())
        }
    }

    @Test
    fun `given products eligible to the promotion by amount when calls create then a cart is created applying that promotion`() {
        val drink = givenAnExistingDrink(price = BigDecimal(10))
        val toppings = mutableListOf(
            Faker.entity.topping(price = BigDecimal(2))
        )
        val toppingsId = toppings.map { it.id }.toSet()
        every { toppingRepository.findAllById(toppingsId) } returns toppings

        val result = service.create(drinkId = drink.id, toppingsId = toppingsId)

        val expectedDiscount = BigDecimal(12).multiply(BigDecimal(0.25))
        val expectedAmount = BigDecimal(12).minus(expectedDiscount)

        expectThat(result) {
            get { this.discount }.isEqualTo(expectedDiscount.setScale(2))
            get { this.amount }.isEqualTo(expectedAmount.setScale(2))
        }
    }

    @Test
    fun `given a non existing drink when calls create then throws NotFound exception`() {
        val drinkId = UUID.randomUUID();
        givenANonExistingDrink(drinkId)

        expectThrows<NotFound> {
            service.create(drinkId = drinkId, toppingsId = setOf(UUID.randomUUID()))
        }.and {
            get { message }.isEqualTo("Drink not found!")
        }

        verify(exactly = 0) {
            cartRepository.save(any())
        }
    }


    @Test
    fun `given a non existing topping when calls create then throws NotFound exception`() {
        val drink = givenAnExistingDrink()
        val toppings = givenSomeExistingToppings(2);
        val toppingsId = setOf<UUID>(*toppings.map { it.id }.toTypedArray(), UUID.randomUUID())

        expectThrows<NotFound> {
            service.create(drinkId = drink.id, toppingsId = toppingsId)
        }.and {
            get { message }.isEqualTo("Topping not found!")
        }

        verify(exactly = 0) {
            cartRepository.save(any())
        }
    }

    @Test
    fun `given a cart eligible to the promotion by amount when adds a new item then the cart is updated`() {
        val cart = givenAnExistingCartEligibleToPromotionByAmount(quantityOfItems = 1)
        val cartGrossAmount =
            cart.items.sumOf { it.drink.price }.plus(cart.items.flatMap { it.toppings.map { it.price } }.sumOf { it })

        val drink = givenAnExistingDrink(price = BigDecimal(10))
        val toppings = givenSomeExistingToppings(2)
        val toppingsId = toppings.map { it.id }.toSet()
        val newItemGrossAmount = drink.price.plus(toppings.sumOf { it.price })

        val result = service.addItem(cartId = cart.id, drinkId = drink.id, toppingsId = toppingsId)

        val expectedDiscount =
            cartGrossAmount.plus(newItemGrossAmount).multiply(Constants.PROMOTION_BY_AMOUNT_PERCENTAGE_DISCOUNT)

        val expectedAmount = cartGrossAmount.plus(newItemGrossAmount).minus(expectedDiscount)

        expectThat(result) {
            get { items }.hasSize(2)
            get { items.last().drink.id }.isEqualTo(drink.id)
            get { items.last().toppings.map { it.id } }.containsExactlyInAnyOrder(toppingsId)
            get { amount }.isEqualTo(expectedAmount.setScale(2))
            get { discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given a cart eligible to the promotion by quantity of items when adds a new item then the cart is updated`() {
        val cart = givenAnExistingCartEligibleToPromotionByQuantityOfItems(
            drinkPrice = BigDecimal(2),
            toppingsPrice = BigDecimal(1)
        )
        val cartGrossAmount = BigDecimal(3 * (2 + 1))

        val drink = givenAnExistingDrink(price = BigDecimal(1))
        val toppings = givenSomeExistingToppings(1, price = BigDecimal(1))
        val toppingsId = toppings.map { it.id }.toSet()

        val result = service.addItem(cartId = cart.id, drinkId = drink.id, toppingsId = toppingsId)

        val expectedDiscount = BigDecimal(2)

        expectThat(result) {
            get { items }.hasSize(4)
            get { items.last().drink.id }.isEqualTo(drink.id)
            get { items.last().toppings.map { it.id } }.containsExactlyInAnyOrder(toppingsId)
            get { amount }.isEqualTo(cartGrossAmount.setScale(2))
            get { discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given a cart eligible to both promotions when adds a new item then the cart is updated using the promotion with biggest discount`() {
        val cart = givenAnExistingCartEligibleToPromotionByQuantityOfItems(
            drinkPrice = BigDecimal(12),
            toppingsPrice = BigDecimal(1)
        )

        val drink = givenAnExistingDrink(price = BigDecimal(2))
        val toppings = givenSomeExistingToppings(1, price = BigDecimal(1))
        val toppingsId = toppings.map { it.id }.toSet()

        val result = service.addItem(cartId = cart.id, drinkId = drink.id, toppingsId = toppingsId)

        val grossAmount = BigDecimal((3 * (12 + 1)) + 2 + 1)
        val expectedDiscount = grossAmount.multiply(Constants.PROMOTION_BY_AMOUNT_PERCENTAGE_DISCOUNT)

        expectThat(result) {
            get { items }.hasSize(4)
            get { items.last().drink.id }.isEqualTo(drink.id)
            get { items.last().toppings.map { it.id } }.containsExactlyInAnyOrder(toppingsId)
            get { amount }.isEqualTo(grossAmount.minus(expectedDiscount).setScale(2))
            get { discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given an not existing cart when calls addItem then throws NotFound exception`() {
        val cartId = UUID.randomUUID()
        givenANonExistingCart(cartId)

        expectThrows<NotFound> {
            service.addItem(cartId = cartId, drinkId = UUID.randomUUID(), setOf())
        }.and {
            get { message }.isEqualTo("Cart not found!")
        }
    }

    @Test
    fun `given an not existing drink when calls addItem then throws NotFound exception`() {
        val cart = givenAnExistingCartEligibleToPromotionByAmount()
        val nonExistingDrinkId = givenANonExistingDrink()

        expectThrows<NotFound> {
            service.addItem(cartId = cart.id, drinkId = nonExistingDrinkId, setOf())
        }.and {
            get { message }.isEqualTo("Drink not found!")
        }
    }

    @Test
    fun `given an existing cart eligible to the promotion by amount when edit an item then the cart is updated`() {
        val cart = givenAnExistingCartEligibleToPromotionByAmount(quantityOfItems = 1);

        val toppings = givenSomeExistingToppings(size = 2, price = BigDecimal(6))
        val toppingsId = toppings.map { it.id }.toSet()
        val cartGrossAmount = cart.items.single().drink.price.plus(BigDecimal(2 * 6))

        val result = service.editItem(cartId = cart.id, itemId = cart.items.first().id, toppingsId = toppingsId)

        val expectedDiscount = cartGrossAmount.multiply(Constants.PROMOTION_BY_AMOUNT_PERCENTAGE_DISCOUNT)
        val expectedAmount = cartGrossAmount.minus(expectedDiscount)

        expectThat(result) {
            get { items.single().toppings.map { it.id } }.containsExactlyInAnyOrder(toppingsId)
            get { this.amount }.isEqualTo(expectedAmount.setScale(2))
            get { this.discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given an existing cart eligible to the promotion by quantity of items when edit an item then the cart is updated`() {
        val cart = givenAnExistingCartEligibleToPromotionByQuantityOfItems(
            drinkPrice = BigDecimal(2),
            toppingsPrice = BigDecimal(1.50)
        )

        val toppings = givenSomeExistingToppings(size = 1, price = BigDecimal(1))
        val toppingsId = toppings.map { it.id }.toSet()

        val cartGrossAmount = BigDecimal(2 + 1 + (2 * (2 + 1.5)))
        val expectedDiscount = BigDecimal(3)
        val expectedAmount = cartGrossAmount.minus(expectedDiscount)

        val result = service.editItem(cartId = cart.id, itemId = cart.items.first().id, toppingsId = toppingsId)

        expectThat(result) {
            get { items.first().toppings.map { it.id } }.containsExactlyInAnyOrder(toppingsId)
            get { this.amount }.isEqualTo(expectedAmount.setScale(2))
            get { this.discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given an existing cart eligible to both promotions when edit an item then the cart is updated using the promotion with the biggest discount`() {
        val cart = givenAnExistingCartEligibleToPromotionByQuantityOfItems(
            drinkPrice = BigDecimal(12),
            toppingsPrice = BigDecimal(2)
        )

        val toppings = givenSomeExistingToppings(size = 1, price = BigDecimal(1))
        val toppingsId = toppings.map { it.id }.toSet()

        val cartGrossAmount = BigDecimal(12 + 1 + (2 * (12 + 2)))
        val expectedDiscount = BigDecimal(13)
        val expectedAmount = cartGrossAmount.minus(expectedDiscount)

        val result = service.editItem(cartId = cart.id, itemId = cart.items.first().id, toppingsId = toppingsId)

        expectThat(result) {
            get { items.first().toppings.map { it.id } }.containsExactlyInAnyOrder(toppingsId)
            get { this.amount }.isEqualTo(expectedAmount.setScale(2))
            get { this.discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given an not existing cart when calls editItem then throws NotFound exception`() {
        val cartId = UUID.randomUUID()
        givenANonExistingCart(cartId)

        expectThrows<NotFound> {
            service.editItem(cartId = cartId, itemId = UUID.randomUUID(), setOf())
        }.and {
            get { message }.isEqualTo("Cart not found!")
        }
    }

    @Test
    fun `given an not existing cart item when calls editItem then throws NotFound exception`() {
        val cart = givenAnExistingCartEligibleToPromotionByAmount()

        expectThrows<NotFound> {
            service.editItem(cartId = cart.id, itemId = UUID.randomUUID(), setOf())
        }.and {
            get { message }.isEqualTo("Cart item not found!")
        }
    }

    @Test
    fun `given an existing cart eligible to the promotion by quantity of items when remove an item then updates the cart`() {
        val cart = givenAnExistingCartEligibleToPromotionByQuantityOfItems(
            drinkPrice = BigDecimal(2),
            toppingsPrice = BigDecimal(1)
        )

        val itemToRemove = cart.items.first().id
        val result = service.removeItem(cartId = cart.id, itemId = itemToRemove)

        expectThat(result) {
            get { items.map { it.id } }.hasSize(2).doesNotContain(itemToRemove)
            get { amount }.isEqualTo(BigDecimal(6).setScale(2))
            get { discount }.isEqualTo(BigDecimal.ZERO.setScale(2))
        }
    }

    @Test
    fun `given an existing cart eligible to the promotion by amount when remove an item then updates the cart`() {
        val cart = givenAnExistingCartEligibleToPromotionByAmount(quantityOfItems = 2)

        val itemToRemove = cart.items.first().id

        val grossAmount = BigDecimal(13)
        val expectedDiscount = grossAmount.multiply(Constants.PROMOTION_BY_AMOUNT_PERCENTAGE_DISCOUNT)
        val expectedAmount = grossAmount.minus(expectedDiscount)

        val result = service.removeItem(cartId = cart.id, itemId = itemToRemove)

        expectThat(result) {
            get { items.map { it.id } }.hasSize(1).doesNotContain(itemToRemove)
            get { amount }.isEqualTo(expectedAmount.setScale(2))
            get { discount }.isEqualTo(expectedDiscount.setScale(2))
        }
    }

    @Test
    fun `given an not existing cart when remove an item then throws NotFound exception`() {
        val cartId = UUID.randomUUID()
        givenANonExistingCart(cartId)

        expectThrows<NotFound> {
            service.removeItem(cartId = cartId, itemId = UUID.randomUUID())
        }.and {
            get { message }.isEqualTo("Cart not found!")
        }
    }

    @Test
    fun `given an not existing cart item when remove an item then throws NotFound exception`() {
        val cart = givenAnExistingCartEligibleToPromotionByAmount()

        expectThrows<NotFound> {
            service.removeItem(cartId = cart.id, itemId = UUID.randomUUID())
        }.and {
            get { message }.isEqualTo("Cart item not found!")
        }
    }

    private fun givenAnExistingDrink(id: UUID = UUID.randomUUID(), price: BigDecimal = BigDecimal(4)): DrinkEntity {
        val drink = Faker.entity.drink(id = id, price = price);
        every { drinkRepository.findByIdOrNull(drink.id) } returns drink
        return drink
    }

    private fun givenAnExistingCartEligibleToPromotionByAmount(
        id: UUID = UUID.randomUUID(),
        quantityOfItems: Int = 1
    ): CartEntity {
        val cart = Faker.entity.cart(id = id)
        val items = MutableList(quantityOfItems) {
            val drink = Faker.entity.drink(price = Constants.PROMOTION_BY_AMOUNT_MIN_VALUE)
            Faker.entity.cartItem(cart = cart, drink = drink)
        }
        cart.items = items

        every {
            cartRepository.findByIdOrNull(id)
        } returns cart
        return cart
    }

    private fun givenAnExistingCartEligibleToPromotionByQuantityOfItems(
        id: UUID = UUID.randomUUID(),
        drinkPrice: BigDecimal,
        toppingsPrice: BigDecimal
    ): CartEntity {
        val cart = Faker.entity.cart(id = id)
        val items = MutableList(3) {
            val drink = Faker.entity.drink(price = drinkPrice)
            val toppingsPrice = mutableSetOf(Faker.entity.topping(price = toppingsPrice))
            Faker.entity.cartItem(cart = cart, drink = drink, toppings = toppingsPrice)
        }
        cart.items = items

        every {
            cartRepository.findByIdOrNull(id)
        } returns cart
        return cart
    }

    private fun givenANonExistingCart(id: UUID = UUID.randomUUID()): UUID {
        every {
            cartRepository.findByIdOrNull(id)
        } returns null

        return id
    }

    private fun givenSomeExistingToppings(
        size: Int,
        price: BigDecimal = BigDecimal.ONE.setScale(0)
    ): MutableList<ToppingEntity> {
        val toppings = MutableList(size) {
            Faker.entity.topping(price = price)
        }

        every { toppingRepository.findAllById(any()) } returns toppings
        return toppings
    }

    private fun givenANonExistingDrink(drinkId: UUID = UUID.randomUUID()): UUID {
        every { drinkRepository.findByIdOrNull(drinkId) } returns null
        return drinkId
    }
}
