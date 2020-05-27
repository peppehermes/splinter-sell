package it.polito.mad.splintersell.data

class FeedbackModel {
    var id_item: String? = null
    var id_user: String? = null
    var id_owner: String? = null
    var rate:  Float? = null
    var comment: String? = null

    constructor()

    constructor(id_item: String, id_user: String, id_owner: String, rate: Float, comment: String ) {
        this.id_user = id_user
        this.id_owner = id_owner
        this.id_item = id_item
        this.rate = rate
        this.comment = comment
    }
}