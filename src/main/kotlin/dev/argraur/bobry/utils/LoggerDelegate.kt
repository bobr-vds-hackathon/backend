package dev.argraur.bobry.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

class LoggerDelegate(private val name: String? = null) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger =
        LoggerFactory.getLogger(name ?: thisRef!!::class.qualifiedName)
}
