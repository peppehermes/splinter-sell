package it.polito.mad.splintersell.data

class NotificationModel(
    var id_item :String?,
    var id_user :String?,
    var id_owner :String?
    )
    {
        constructor() : this(null, null, null)
    }