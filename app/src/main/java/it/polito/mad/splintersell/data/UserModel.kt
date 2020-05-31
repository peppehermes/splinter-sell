package it.polito.mad.splintersell.data

import com.google.firebase.firestore.GeoPoint

class UserModel {

    var fullname: String? = null
    var nickname: String? = null
    var email: String? = null
    var location: String? = null
    var photoName: String = "img_avatar.jpg"
    var userId: String? = null
    var token: String = ""
    var rating: Float = 0F
    var counterfeed: Int = 0
    var address: GeoPoint? = null

    constructor()

    constructor(
        fullname: String, email: String
    ) {
        this.fullname = fullname
        this.email = email
    }

    constructor(
        id: String, fullname: String, email: String
    ) {
        this.userId = id
        this.fullname = fullname
        this.email = email
    }

    constructor(
        address: GeoPoint,
        location: String
    ){
        this.address = address
        this.location = location
    }

    constructor(
        fullname: String, nickname: String, email: String, location: String,
        photoName: String, userId: String, token: String, counterFeed: Int, rating: Float, address: GeoPoint
    ) {
        this.fullname = fullname
        this.nickname = nickname
        this.email = email
        this.location = location
        this.photoName = photoName
        this.userId = userId
        this.token = token
        this.rating = rating
        this.counterfeed = counterFeed
        this.address = address
    }

    fun toMap(): Map<String, Any> {

        val result = HashMap<String, Any>()
        result["fullname"] = fullname!!
        result["nickname"] = nickname!!
        result["email"] = email!!
        result["location"] = location!!
        result["photoName"] = photoName
        result["rating"] = rating
        result["counterfeed"] = counterfeed

        return result
    }

}

