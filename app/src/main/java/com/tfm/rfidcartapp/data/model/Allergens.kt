package com.tfm.rfidcartapp.data.model

import androidx.annotation.StringRes
import com.tfm.rfidcartapp.R

data class AllergenOption(
    val id: String,
    @StringRes val labelRes: Int
)

object Allergens {
    val all = listOf(
        AllergenOption("gluten", R.string.allergen_gluten),
        AllergenOption("crustaceans", R.string.allergen_crustaceans),
        AllergenOption("mollusks", R.string.allergen_mollusks),
        AllergenOption("fish", R.string.allergen_fish),
        AllergenOption("eggs", R.string.allergen_eggs),
        AllergenOption("lupins", R.string.allergen_lupins),
        AllergenOption("mustard", R.string.allergen_mustard),
        AllergenOption("peanuts", R.string.allergen_peanuts),
        AllergenOption("treenuts", R.string.allergen_treenuts),
        AllergenOption("soybean", R.string.allergen_soybeans),
        AllergenOption("sesame", R.string.allergen_sesame),
        AllergenOption("celery", R.string.allergen_celery),
        AllergenOption("milk", R.string.allergen_milk),
        AllergenOption("sulphites", R.string.allergen_sulphites),
    )
}