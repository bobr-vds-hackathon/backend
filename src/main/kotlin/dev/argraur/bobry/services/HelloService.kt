package dev.argraur.bobry.services

import org.koin.core.annotation.Factory

@Factory
class HelloService {
    fun invoke() = "Hello, world!"
}
