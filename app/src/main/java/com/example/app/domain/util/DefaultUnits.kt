package com.example.app.domain.util

import java.util.concurrent.ConcurrentHashMap

/**
 * Default measurement units for some ingredients. When the user enters only
 * an amount and a name, the unit from this map will be used.
 */
val defaultUnits: MutableMap<String, String> = ConcurrentHashMap(
    mapOf(
        "Nudeln" to "g",
        "Milch" to "ml",
        "Zwiebel" to "Stk",
        "Mehl" to "g"
    )
)

/** Adds or updates the default unit for the given ingredient name. */
fun setDefaultUnit(name: String, unit: String) {
    defaultUnits[name] = unit
}

/** Returns the default unit for the ingredient name, or null if unknown. */
fun getDefaultUnit(name: String): String? = defaultUnits[name]
