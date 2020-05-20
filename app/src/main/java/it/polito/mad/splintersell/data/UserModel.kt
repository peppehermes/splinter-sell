package it.polito.mad.splintersell.data

class UserModel{

    var fullname: String? = null
    var nickname: String? = null
    var email: String? = null
    var location: String? = null
    var photoName: String = "img_avatar.jpg"
    var userid: String? = null

    constructor() {}

    constructor(fullname: String, nickname: String, email: String,
                location: String, photoName: String) {
        this.fullname = fullname
        this.nickname = nickname
        this.email = email
        this.location = location
        this.photoName = photoName
    }

    fun toMap(): Map<String, Any> {

        val result = HashMap<String, Any>()
        result["fullname"] = fullname!!
        result["nickname"] = nickname!!
        result["email"] = email!!
        result["location"] = location!!
        result["photoName"] = photoName!!

        return result
    }

}

