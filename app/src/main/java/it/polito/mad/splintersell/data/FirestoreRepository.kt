package it.polito.mad.splintersell.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

val storage: StorageReference = FirebaseStorage.getInstance().reference

class FirestoreRepository(private val onFirestoreTaskComplete: OnFirestoreTaskComplete) {
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
                .whereEqualTo("status", "Available").get()

            secondTask = firestore.collection("items")
                .whereGreaterThan("ownerId", user!!.uid)
                .whereEqualTo("status", "Available").get()
        } else {
            firstTask = firestore.collection("items")
                .whereLessThan("ownerId", user!!.uid)
                .whereEqualTo("mainCategory", mainCategory)
                .whereEqualTo("secondCategory", secondCategory)
                .whereEqualTo("status", "Available")
                .get()

            secondTask = firestore.collection("items")
                .whereGreaterThan("ownerId", user!!.uid)
                .whereEqualTo("mainCategory", mainCategory)
                .whereEqualTo("secondCategory", secondCategory)
                .whereEqualTo("status", "Available")
                .get()
        }

        val list = ArrayList<ItemModel>()

        Tasks.whenAllSuccess<QuerySnapshot>(firstTask, secondTask)
            .addOnSuccessListener { querySnaps ->
                for (docSnap in querySnaps) for (document in docSnap) {
                    val item = document.toObject(ItemModel::class.java)
                    val date = item.expireDate!!.split("/")

                    if (item.price!!.toInt() in minPrice..maxPrice && validateDate(date))
                        list.add(item)
                }

                onFirestoreTaskComplete.itemListDataAdded(list)
            }
    }

    private fun validateDate(date: List<String>): Boolean {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        val years = date[2].toInt()
        val monthOfYear = date[1].toInt()
        val dayOfMonth = date[0].toInt()
        return !(years < year ||
                ((years == year) && (monthOfYear < month)) ||
                ((years == year) && (monthOfYear == month) &&
                        dayOfMonth < day))
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
                    Log.d("liked", itemName)
                    Log.d("likedList", likedItems.toString())
                }

                if (likedItems.isNotEmpty()) {
                    firestore.collection("items")
                        .whereIn(FieldPath.documentId(), likedItems).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.d("documento", document.toString())
                                val item = document.toObject(ItemModel::class.java)
                                list.add(item)
                                Log.d("listNot1", list.toString())
                            }
                            onFirestoreTaskComplete.notListDataAdded(list)
                        }
                } else {
                    onFirestoreTaskComplete.notListDataAdded(list)
                }
            }
    }

    fun updateStatus(status: String, itemId: String) {
        firestore.collection("items").document(itemId).update("status", status)
    }

    fun updateToken(token: String) {
        firestore.collection("users").document(user!!.uid).update("token", token)
    }

    interface OnFirestoreTaskComplete {
        fun itemListDataAdded(itemModelList: List<ItemModel>)
        fun fetchNotifications(requested: Boolean)
        fun notListDataAdded(notificationList: List<ItemModel>)
        fun userListDataAdded(userList: List<UserModel>)
    }
}

