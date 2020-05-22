package it.polito.mad.splintersell.data

class NotificationModel {
    var id_item: String? = null
    var id_user: String? = null
    var id_owner: String? = null

    constructor()

    constructor(id_item: String, id_user: String, id_owner: String) {
        this.id_user = id_user
        this.id_owner = id_owner
        this.id_item = id_item
    }
}