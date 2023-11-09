package dev.argraur.bobry.di.application

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("dev.argraur.bobry.services")
class ServiceModule
