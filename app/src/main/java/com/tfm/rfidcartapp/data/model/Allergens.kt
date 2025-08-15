package com.tfm.rfidcartapp.data.model

data class AllergenOption(val id: String, val label: String)

object Allergens {
    val all = listOf(
        AllergenOption("gluten", "Cereales con gluten"),
        AllergenOption("crustaceos", "Crustáceos"),
        AllergenOption("moluscos", "Moluscos"),
        AllergenOption("pescado", "Pescado"),
        AllergenOption("huevos", "Huevos"),
        AllergenOption("altramuces", "Altramuces"),
        AllergenOption("mostaza", "Mostaza"),
        AllergenOption("cacahuetes", "Cacahuetes"),
        AllergenOption("frutos_cascara", "Frutos de cáscara"),
        AllergenOption("soja", "Soja"),
        AllergenOption("sesamo", "Sésamo"),
        AllergenOption("apio", "Apio"),
        AllergenOption("leche", "Leche y productos lácteos (incl. lactosa)"),
        AllergenOption("sulfitos", "Dióxido de azufre y sulfitos (>10 mg/kg o mg/L)")
    )
}
