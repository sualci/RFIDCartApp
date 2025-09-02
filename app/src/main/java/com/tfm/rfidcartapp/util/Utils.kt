package com.tfm.rfidcartapp.util

import android.content.Context
import com.tfm.rfidcartapp.data.model.Allergens

// Función para formatear un Double como precio con dos decimales y el símbolo €
fun Double.toPrice(): String = "%.2f€".format(this)

// Objeto que gestiona las etiquetas (textos) de los alérgenos
object AllergenLabels {

    // Devuelve la etiqueta traducida para un alérgeno concreto a partir de su id
    fun labelFor(context: Context, id: String): String {
        // Busca el recurso de texto asociado al alérgeno
        val resId = Allergens.all.firstOrNull { it.id == id }?.labelRes
        // Si lo encuentra, devuelve el string localizado. Si no, devuelve el id como fallback
        return resId?.let { context.getString(it) } ?: id
    }

    // Devuelve una lista de etiquetas para un conjunto de ids de alérgenos
    fun labelsFor(context: Context, ids: Iterable<String>): List<String> =
        ids.map { labelFor(context, it) }
}
