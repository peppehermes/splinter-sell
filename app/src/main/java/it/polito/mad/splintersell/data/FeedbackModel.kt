package it.polito.mad.splintersell.data

class FeedbackModel {
    var id_item: String? = null
    var id_user: String? = null
    var id_owner: String? = null
    var rate:  Float? = null
    var comment: String? = null
    var item_title: String? = null
    var user_nick: String? = null

    constructor()

    constructor(id_item: String, id_user: String, id_owner: String, rate: Float, comment: String, item_title: String, user_nick: String ) {
        this.id_user = id_user
        this.id_owner = id_owner
        this.id_item = id_item
        this.rate = rate
        this.comment = comment
        this.item_title = item_title
        this.user_nick = user_nick
    }
}