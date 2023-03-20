package com.edigleison.coffeestoreapi.util

object Faker {
    internal val instance = com.github.javafaker.Faker()
    val entity = EntityFaker
}
