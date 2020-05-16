package it.polito.mad.splintersell.data

class UserModel(
    var fullname: String?,
    var nickname: String?,
    var email: String?,
    var location: String?
) {
    constructor() : this(null, null, null, null)
}