package it.polito.mad.splintersell

data class ItemDB(
    val id: Int = 0,
    val title: String? = "",
    val description: String? = "",
    val price: String? = "",
    val mainCategory: String? = "",
    val secondCategory: String? = "",
    val location: String? = "",
    val expire_date: String? = "") {
}