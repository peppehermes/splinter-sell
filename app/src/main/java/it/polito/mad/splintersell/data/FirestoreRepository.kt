package it.polito.mad.splintersell.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

val storage: StorageReference = FirebaseStorage.getInstance().reference

class FirestoreRepository(private val onFirestoreTaskComplete: OnFirestoreTaskComplete) {
    val TAG = "FIREBASE_REPOSITORY"
    var firestore = FirebaseFirestore.getInstance()
    val itemRef = firestore.collection("items")
    val notRef = firestore.collection("notifications")
    var user = FirebaseAuth.getInstance().currentUser

    fun getItemDocument(documentName: String): DocumentReference {
        return firestore.collection("items")
            .document(documentName)
    }

    fun getItemNotification(itemId: String) {
        var requested = false
        val task = firestore
            .collection("notifications")
            .whereEqualTo("id_item", itemId)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(task)
            .addOnSuccessListener { querySnaps ->
                for (docSnap in querySnaps)
                    for (document in docSnap) {
                        val notification = document.toObject(NotificationModel::class.java)
                        if (notification.id_user.toString() == user!!.uid) {
                            requested = true
                            break
                        }
                    }
                onFirestoreTaskComplete.fetchNotifications(requested)
            }
    }

    fun getItemData() {
        val firstTask = FirebaseFirestore.getInstance()
            .collection("items")
            .whereLessThan("ownerId", user!!.uid)
            .whereEqualTo("status","Available")
            .get()

        val secondTask = FirebaseFirestore.getInstance()
            .collection("items")
            .whereGreaterThan("ownerId", user!!.uid)
            .whereEqualTo("status","Available")
            .get()

        val list = ArrayList<ItemModel>()

        Tasks.whenAllSuccess<QuerySnapshot>(firstTask, secondTask)
            .addOnSuccessListener { querySnaps ->
                for (docSnap in querySnaps)
                    for (document in docSnap) {
                        val item = document.toObject(ItemModel::class.java)
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
        val documentReferenceUser = firestore.collection("notifications")
            .document("${not!!.id_user}_${not!!.id_item}")

        return documentReferenceUser.set(not)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving") }
    }

    fun removeNotifications(itemId: String){
        val task = firestore.collection("notifications")
            .whereEqualTo("id_item", itemId)
            .whereEqualTo("id_user", user!!.uid)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(task)
            .addOnSuccessListener { querySnaps ->
                for (docSnap in querySnaps)
                    for (document in docSnap) {
                        document.reference.delete().addOnSuccessListener(){
                            Log.d(TAG, "Successfully removed")
                        }
                    }
            }
            .addOnFailureListener() { Log.d(TAG, "Error in removing") }
    }

    fun removeAllNotifications(itemId: String){
        val task = firestore.collection("notifications")
            .whereEqualTo("id_item", itemId)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(task)
            .addOnSuccessListener { querySnaps ->
                for (docSnap in querySnaps)
                    for (document in docSnap) {
                        document.reference.delete().addOnSuccessListener(){
                            Log.d(TAG, "Successfully removed")
                        }
                    }
            }
            .addOnFailureListener() { Log.d(TAG, "Error in removing") }
    }

    fun getUserDocument(userID: String): DocumentReference {
        return firestore.collection("users")
            .document(userID)
    }

    fun saveUser(myUser: UserModel): Task<Void> {
        val documentReferenceUser = firestore.collection("users")
            .document(user!!.uid)

        return documentReferenceUser.set(myUser)
            .addOnSuccessListener { Log.d(TAG, "USER Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "USER Error in saving")}
    }


    fun getUsersData(itemID :String) {
        val interestedUsers = ArrayList<String>()
        val list = ArrayList<UserModel>()
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("id_item", itemID)
            .whereEqualTo("id_owner", user!!.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val username = doc.get("id_user").toString()
                    interestedUsers.add(username)
                    Log.d("interested", username)
                    Log.d("interestedList",interestedUsers.toString())
                }

                if(interestedUsers.isNotEmpty()){
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereIn(FieldPath.documentId(), interestedUsers)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.d("documento",document.toString())
                                val user = document.toObject(UserModel::class.java)
                                list.add(user)
                                Log.d("listUsers",list.toString())
                                onFirestoreTaskComplete.userListDataAdded(list)
                            }
                        }}
                else
                    onFirestoreTaskComplete.userListDataAdded(list)
            }
    }

    fun getNotificationData() {
        val likedItems = ArrayList<String>()
        val list = ArrayList<ItemModel>()
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("id_user", user!!.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val itemname = doc.get("id_item").toString()
                    likedItems.add(itemname)
                    Log.d("liked", itemname)
                    Log.d("likedList",likedItems.toString())
                }

                if(likedItems.isNotEmpty()){
                FirebaseFirestore.getInstance()
                    .collection("items")
                    .whereIn(FieldPath.documentId(), likedItems)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            Log.d("documento",document.toString())
                            val item = document.toObject(ItemModel::class.java)
                            list.add(item)
                            Log.d("listNot1",list.toString())
                        }

                        onFirestoreTaskComplete.notListDataAdded(list)
                    }}

            }

            }

    fun updateStatus(status:String,itemId: String){

        firestore
            .collection("items")
            .document(itemId)
            .update("status",status)

    }

    interface OnFirestoreTaskComplete {
        fun itemListDataAdded(itemModelList: List<ItemModel>)
        fun fetchNotifications(requested:Boolean)
        fun notListDataAdded(notificationList: List<ItemModel>)
        fun userListDataAdded(userList: List<UserModel>)
    }
}

