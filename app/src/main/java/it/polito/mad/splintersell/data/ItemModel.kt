package it.polito.mad.splintersell.data

import com.google.firebase.storage.StorageReference

class ItemModel {
    var title: String? = null
    var description: String? = null
    var price: String? = null
    var mainCategory: String? = null
    var secondCategory: String? = null
    var location: String? = null
    var expireDate: String? = null
    var documentName: String? = null
    var ownerId: String? = null
    var imgPath: String = ""

    constructor() {}

    constructor(title: String, description: String, price: String,
        mainCategory: String, secondCategory: String, location: String,
                expireDate: String, documentName: String, ownerId: String, imgPath: String) {
        this.title = title
        this.description = description
        this.price = price
        this.mainCategory = mainCategory
        this.secondCategory = secondCategory
        this.location = location
        this.expireDate = expireDate
        this.documentName = documentName
        this.ownerId = ownerId
        this.imgPath = imgPath
    }

    fun toMap(): Map<String, Any> {

        val result = HashMap<String, Any>()
        result["title"] = title!!
        result["description"] = description!!
        result["price"] = price!!
        result["mainCategory"] = mainCategory!!
        result["secondCategory"] = secondCategory!!
        result["location"] = location!!
        result["expire_date"] = expireDate!!

        return result
    }
}