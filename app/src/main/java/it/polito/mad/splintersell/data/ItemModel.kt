package it.polito.mad.splintersell.data

import com.google.firebase.firestore.GeoPoint

class ItemModel {
    var title: String? = null
    var description: String? = null
    var price: String? = null
    var mainCategory: String? = null
    var secondCategory: String? = null
    var location: String? = null
    var address: GeoPoint? = null
    var expireDate: String? = null
    var documentName: String? = null
    var ownerId: String? = null
    var imgPath: String = ""
    var status: String? = null
    var soldTo: String = ""
    var isleft: Boolean = false

    constructor()

    constructor(
        location: String, address: GeoPoint
    ) {
        this.location = location
        this.address = address
    }

    constructor(
        title: String, description: String, price: String,
        mainCategory: String, secondCategory: String,
        expireDate: String, documentName: String, ownerId: String, imgPath: String, status: String
    ) {
        this.title = title
        this.description = description
        this.price = price
        this.mainCategory = mainCategory
        this.secondCategory = secondCategory
        this.expireDate = expireDate
        this.documentName = documentName
        this.ownerId = ownerId
        this.imgPath = imgPath
        this.status = status
    }

    constructor(
        title: String, description: String, price: String,
        mainCategory: String, secondCategory: String, location: String, address: GeoPoint,
        expireDate: String, documentName: String, ownerId: String, imgPath: String, status: String
    ) {
        this.title = title
        this.description = description
        this.price = price
        this.mainCategory = mainCategory
        this.secondCategory = secondCategory
        this.location = location
        this.address = address
        this.expireDate = expireDate
        this.documentName = documentName
        this.ownerId = ownerId
        this.imgPath = imgPath
        this.status = status
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