package it.polito.mad.splintersell.data

import android.location.Address
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

val storage: StorageReference = FirebaseStorage.getInstance().reference

class FirestoreRepository(val onFirestoreTaskComplete: OnFirestoreTaskComplete) {
    val TAG = "FIREBASE_REPOSITORY"
    var firestore = FirebaseFirestore.getInstance()
    val itemRef = firestore.collection("items")
    val notRef = firestore.collection("notifications")
    val feedRef = firestore.collection("feedbacks")

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val usersRef: CollectionReference = firestore.collection("users")

    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential?): MutableLiveData<UserModel>? {
        val authenticatedUserMutableLiveData: MutableLiveData<UserModel> =
            MutableLiveData<UserModel>()
        firebaseAuth.signInWithCredential(googleAuthCredential!!)
            .addOnCompleteListener { authTask: Task<AuthResult> ->
                if (authTask.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        val uid = firebaseUser.uid
                        val name = firebaseUser.displayName!!
                        val email = firebaseUser.email!!
                        val user = UserModel(uid, name, email)
                        authenticatedUserMutableLiveData.value = user
                    }
                } else {
                    Log.e(TAG, authTask.exception!!.message!!)
                }
            }
        return authenticatedUserMutableLiveData
    }

    fun createUserInFirestoreIfNotExists(authenticatedUser: UserModel): MutableLiveData<UserModel>? {
        val newUserMutableLiveData: MutableLiveData<UserModel> = MutableLiveData<UserModel>()
        val uidRef: DocumentReference =
            usersRef.document(authenticatedUser.userId!!)
        uidRef.get()
            .addOnCompleteListener { uidTask: Task<DocumentSnapshot?> ->
                if (uidTask.isSuccessful) {
                    val document = uidTask.result
                    if (!document!!.exists()) {
                        uidRef.set(authenticatedUser)
                            .addOnCompleteListener { userCreationTask: Task<Void?> ->
                                if (userCreationTask.isSuccessful) {
                                    newUserMutableLiveData.value = authenticatedUser
                                } else {
                                    Log.e(TAG, userCreationTask.exception!!.message!!)
                                }
                            }
                    } else {
                        newUserMutableLiveData.value = authenticatedUser
                    }
                } else {
                    Log.e(TAG, uidTask.exception!!.message!!)
                }
            }
        return newUserMutableLiveData
    }

    fun getCurrentUser(): MutableLiveData<UserModel>? {
        val newUserMutableLiveData: MutableLiveData<UserModel> = MutableLiveData<UserModel>()
        lateinit var user: UserModel

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            val name = firebaseUser.displayName!!
            val email = firebaseUser.email!!
            user = UserModel(uid, name, email)
        }

        val uidRef: DocumentReference = usersRef.document(user.userId!!)
        uidRef.get()
            .addOnCompleteListener { uidTask: Task<DocumentSnapshot?> ->
                if (uidTask.isSuccessful) {
                    val document = uidTask.result
                    if (document!!.exists()) {
                        val userObj = document.toObject(UserModel::class.java)
                        newUserMutableLiveData.value = userObj
                    }
                }
            }

        return newUserMutableLiveData
    }

    fun getItemDocument(documentName: String): DocumentReference {
        return firestore.collection("items").document(documentName)
    }

    fun saveItem(item: ItemModel): Task<Void> {
        val documentReferenceUser = firestore.collection("items")
            .document(item.documentName!!)

        return documentReferenceUser.set(item)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving") }
    }

    fun createItem(item: ItemModel){

        var newItemLocation = "Null Location"
        var newItemAddress = GeoPoint(0.0, 0.0)

        val documentReferenceUser = firestore.collection("users").document(item.ownerId!!)

            documentReferenceUser
            .get()
            .addOnSuccessListener {
                    users ->
                Log.d("ITEMTAG", "Found User: $users")
                val myOwnUser = users.toObject(UserModel::class.java)
                Log.d("ITEMTAG", "User Model: "+myOwnUser.toString())
                newItemLocation = myOwnUser!!.location!!
                newItemAddress = myOwnUser.address!!
                Log.d("ITEMTAG", newItemLocation)
                Log.d("ITEMTAG", newItemAddress.toString())

                val newItem = ItemModel(item.title!!, item.description!!, item.price!!, item.mainCategory!!, item.secondCategory!!,
                    newItemLocation, newItemAddress, item.expireDate!!, item.documentName!!, item.ownerId!!, item.imgPath, item.status!!)

                Log.d("ITEMTAG", "My item: $newItem")

                val documentReferenceItem = firestore.collection("items")
                    .document(newItem.documentName!!)

                documentReferenceItem.set(newItem)
                    .addOnSuccessListener { Log.d(TAG, "Successfully created") }
                    .addOnFailureListener { Log.d(TAG, "Error in creating item") }
            }
            .addOnFailureListener{
                Log.d("ITEMTAG", "NO USER FOUND")
            }




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

    fun updateRating(ownerId: String, newRate: Float) {
        firestore.collection("users").document(ownerId).update(
            "counterfeed", FieldValue.increment(1))
            .addOnSuccessListener {
            firestore.collection("users")
                .document(ownerId)
                .get()
                .addOnSuccessListener { users ->
                    val user = users.toObject(UserModel::class.java)
                    val counter = user!!.counterfeed
                    val sum = (user.rating) * (counter - 1)
                    val rating = (sum + newRate) / counter
                    firestore.collection("users").document(ownerId).update("rating", rating)
                }
            }
    }

    fun updateUserLocation(address: GeoPoint, location: String, ownerId: String){
        firestore.collection("users").document(ownerId).update("address", address)
        firestore.collection("users").document(ownerId).update("location", location)
    }

    fun updateItemLocation(address: GeoPoint, location: String, itemId: String){
        firestore.collection("items").document(itemId).update("address", address)
        firestore.collection("items").document(itemId).update("location", location)
    }


    fun updateStatus(status: String, itemId: String) {
        firestore.collection("items").document(itemId).update("status", status)
    }

    fun setSoldTo(uid: String, itemId: String) {
        firestore.collection("items").document(itemId).update("soldTo", uid)
    }

    fun updateStatusFeed(itemId: String) {
        firestore.collection("items").document(itemId).update("isleft", true)
    }

    interface OnFirestoreTaskComplete {
        fun itemListDataAdded(itemModelList: List<ItemModel>)
        fun fetchNotifications(requested: Boolean)
        fun notListDataAdded(notificationList: List<ItemModel>)
        fun userListDataAdded(userList: List<UserModel>)
    }
}

