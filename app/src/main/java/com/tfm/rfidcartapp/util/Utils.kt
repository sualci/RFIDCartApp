package com.tfm.rfidcartapp.util

import android.content.Context
import com.tfm.rfidcartapp.data.model.Allergens

fun Double.toPrice(): String = "%.2f€".format(this)

object AllergenLabels {

    fun labelFor(context: Context, id: String): String {
        val resId = Allergens.all.firstOrNull { it.id == id }?.labelRes
        return resId?.let { context.getString(it) } ?: id
    }

    fun labelsFor(context: Context, ids: Iterable<String>): List<String> =
        ids.map { labelFor(context, it) }
}