package it.polito.mad.splintersell.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

val storage: StorageReference = FirebaseStorage.getInstance().reference

class FirestoreRepository(private val onFirestoreTaskComplete: OnFirestoreTaskComplete) {
    val AVAILABLE = "available"
    val BLOCKED = "blocked"
    val SOLD = "sold"
    val TAG = "FIREBASE_REPOSITORY"
    var firestore = FirebaseFirestore.getInstance()
    val itemRef = firestore.collection("items")
    val notRef = firestore.collection("notifications")
    var user = FirebaseAuth.getInstance().currentUser

    fun getItemDocument(documentName: String): DocumentReference {
        return firestore.collection("items").document(documentName)
    }

    fun getItemNotification(itemId: String) {
        var requested = false
        val task = firestore.collection("notifications")
            .whereEqualTo("id_item", itemId).get()

        task.addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val notification = document.toObject(NotificationModel::class.java)
                if (notification.id_user.toString() == user!!.uid) {
                    requested = true
                }
            }
            onFirestoreTaskComplete.fetchNotifications(requested)

        }
    }

    fun getItemData(
        minPrice: Int = 0,
        maxPrice: Int = Int.MAX_VALUE,
        mainCategory: String? = null,
        secondCategory: String? = null
    ) {

        val firstTask: Task<QuerySnapshot>
        val secondTask: Task<QuerySnapshot>

        if (mainCategory == null) {
            firstTask = firestore.collection("items")
                .whereLessThan("ownerId", user!!.uid)
                .whereEqualTo("status", AVAILABLE).get()

            secondTask = firestore.collection("items")
                .whereGreaterThan("ownerId", user!!.uid)
                .whereEqualTo("status", AVAILABLE).get()
        } else {
            firstTask = firestore.collection("items")
                .whereLessThan("ownerId", user!!.uid)
                .whereEqualTo("mainCategory", mainCategory)
                .whereEqualTo("secondCategory", secondCategory)
                .whereEqualTo("status", AVAILABLE)
                .get()

            secondTask = firestore.collection("items")
                .whereGreaterThan("ownerId", user!!.uid)
                .whereEqualTo("mainCategory", mainCategory)
                .whereEqualTo("secondCategory", secondCategory)
                .whereEqualTo("status", AVAILABLE)
                .get()
        }

        val list = ArrayList<ItemModel>()

        Tasks.whenAllSuccess<QuerySnapshot>(firstTask, secondTask)
            .addOnSuccessListener { querySnaps ->
                for (docSnap in querySnaps) for (document in docSnap) {
                    val item = document.toObject(ItemModel::class.java)

                    if (item.price!!.toInt() in minPrice..maxPrice)
                        list.add(item)
                }

                onFirestoreTaskComplete.itemListDataAdded(list)
            }
    }

    fun saveItem(item: ItemModel): Task<Void> {
        val documentReferenceUser = firestore.collection("items")
            .document(item.documentName!!)

        return documentReferenceUser.set(item)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving") }
    }

    fun saveNotification(not: NotificationModel): Task<Void> {
        val documentReferenceUser =
            firestore.collection("notifications")
                .document("${not.id_user}_${not.id_item}")

        return documentReferenceUser.set(not)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving") }
    }

    fun saveFeedback(feed: FeedbackModel): Task<Void> {
        val documentReferenceUser =
            firestore.collection("feedbacks")
                .document("${feed.id_user}_${feed.id_item}_${feed.id_owner}")

        return documentReferenceUser.set(feed)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving") }
    }

    fun removeNotification(itemId: String) {
        val docQuery = firestore.collection("notifications")
            .whereEqualTo("id_item", itemId)
            .whereEqualTo("id_user", user!!.uid).get()

        docQuery.addOnSuccessListener { documents ->
            for (document in documents) {
                document.reference.delete().addOnSuccessListener {
                    Log.d(TAG, "Successfully removed")
                }
            }
        }.addOnFailureListener { Log.d(TAG, "Error in removing") }
    }

    fun removeAllNotifications(itemId: String) {
        val task = firestore.collection("notifications")
            .whereEqualTo("id_item", itemId).get()

        task.addOnSuccessListener { querySnaps ->
            for (document in querySnaps) {
                document.reference.delete().addOnSuccessListener {
                    Log.d(TAG, "Successfully removed")
                }
            }
        }.addOnFailureListener { Log.d(TAG, "Error in removing") }
    }

    fun getUserDocument(userID: String): DocumentReference {
        return firestore.collection("users").document(userID)
    }

    fun saveUser(myUser: UserModel): Task<Void> {
        val documentReferenceUser = firestore.collection("users").document(user!!.uid)

        return documentReferenceUser.set(myUser)
            .addOnSuccessListener { Log.d(TAG, "USER Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "USER Error in saving") }
    }

    fun isUniqueNickname(nick: String): Task<QuerySnapshot> {
        return firestore.collection("users")
            .whereEqualTo("nickname", nick)
            .get()
    }

    fun getUsersData(itemID: String) {
        val interestedUsers = ArrayList<String>()
        val list = ArrayList<UserModel>()
        firestore.collection("notifications")
            .whereEqualTo("id_item", itemID)
            .whereEqualTo("id_owner", user!!.uid)
            .get().addOnSuccessListener { notifications ->
                for (doc in notifications) {
                    val username = doc.get("id_user").toString()
                    interestedUsers.add(username)
                }

                if (interestedUsers.isNotEmpty()) {
                    firestore.collection("users")
                        .whereIn(FieldPath.documentId(), interestedUsers).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val user = document.toObject(UserModel::class.java)
                                user.userid = document.id
                                list.add(user)
                                onFirestoreTaskComplete.userListDataAdded(list)
                            }
                        }
                } else onFirestoreTaskComplete.userListDataAdded(list)
            }
    }

    fun getNotificationData() {
        val likedItems = ArrayList<String>()
        val list = ArrayList<ItemModel>()
        firestore.collection("notifications")
            .whereEqualTo("id_user", user!!.uid).get()
            .addOnSuccessListener { notifications ->
                for (doc in notifications) {
                    val itemName = doc.get("id_item").toString()
                    likedItems.add(itemName)
                }

                if (likedItems.isNotEmpty()) {
                    firestore.collection("items")
                        .whereEqualTo("status","available")
                        .whereIn("documentName", likedItems)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val item = document.toObject(ItemModel::class.java)
                                list.add(item)
                            }
                            onFirestoreTaskComplete.notListDataAdded(list)
                        }
                } else {
                    onFirestoreTaskComplete.notListDataAdded(list)
                }
            }
    }

    fun updateRating(ownerid: String, newrate: Float) {
        firestore.collection("users").document(ownerid).update(
            "counterfeed", FieldValue.increment(1))
            .addOnSuccessListener() {
            firestore.collection("users")
                .document(ownerid)
                .get()
                .addOnSuccessListener { users ->
                    val user = users.toObject(UserModel::class.java)
                    val counter = user!!.counterfeed
                    val sum = (user!!.rating) * (counter - 1)
                    val rating = (sum + newrate)/ counter
                    firestore.collection("users").document(ownerid).update("rating", rating)
                }
            }
    }

    fun updateStatus(status: String, itemId: String) {
        firestore.collection("items").document(itemId).update("status", status)
    }

    fun updateStatusFeed(itemId: String) {
        firestore.collection("items").document(itemId).update("isleft", true)
    }

    fun updateToken(token: String) {
        firestore.collection("users").document(user!!.uid).update("token", token)
    }

    fun setSoldTo(uid:String, itemId:String){

        firestore.collection("items").document(itemId).update("soldTo",uid)
    }


    interface OnFirestoreTaskComplete {
        fun itemListDataAdded(itemModelList: List<ItemModel>)
        fun fetchNotifications(requested: Boolean)
        fun notListDataAdded(notificationList: List<ItemModel>)
        fun userListDataAdded(userList: List<UserModel>)
    }
}

