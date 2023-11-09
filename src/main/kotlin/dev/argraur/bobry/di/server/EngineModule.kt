package dev.argraur.bobry.di.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.dsl.module

val engineModule = module {
    single<ApplicationEngineFactory<*, *>> { Netty }
}
