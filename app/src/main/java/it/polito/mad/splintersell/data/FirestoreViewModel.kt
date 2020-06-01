package it.polito.mad.splintersell.data

import android.location.Address
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import java.util.*


class FirestoreViewModel : ViewModel(), FirestoreRepository.OnFirestoreTaskComplete {
    val TAG = "FIRESTORE_VIEW_MODEL"
    val AVAILABLE = "available"
    val BLOCKED = "blocked"
    val SOLD = "sold"
    var firestoreRepository = FirestoreRepository(this)
    lateinit var userId: String
    var authenticatedUserLiveData: LiveData<UserModel>? = null
    var createdUserLiveData: LiveData<UserModel>? = null

    private var _user: MutableLiveData<UserModel> = MutableLiveData()
    private var _myUser: MutableLiveData<UserModel> = MutableLiveData()
    private var _myNotificationsList: MutableLiveData<List<NotificationModel>> = MutableLiveData()
    private var _wishItemsList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _isRequested: MutableLiveData<Boolean> = MutableLiveData()
    private var _item: MutableLiveData<ItemModel> = MutableLiveData()
    private var _onSaleItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _myItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _availableItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _allItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _interestedUserList: MutableLiveData<List<UserModel>> = MutableLiveData()
    private var _soldItemsList: MutableLiveData<List<ItemModel>> = MutableLiveData()

    init {
        this.fetchAllItemListFromFirestore()
    }

    fun createUser(authenticatedUser: UserModel) {
        createdUserLiveData =
            firestoreRepository.createUserInFirestoreIfNotExists(authenticatedUser)
    }

    fun getCurrentUser() {
        createdUserLiveData = firestoreRepository.getCurrentUser()
    }

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData =
            firestoreRepository.firebaseSignInWithGoogle(googleAuthCredential)
    }

    override fun itemListDataAdded(itemModelList: List<ItemModel>) {
        _onSaleItemList.value = itemModelList
    }

    override fun fetchNotifications(requested: Boolean) {
        _isRequested.value = requested
    }

    override fun userListDataAdded(userList: List<UserModel>) {
        _interestedUserList.value = userList
    }

    override fun notListDataAdded(notificationList: List<ItemModel>) {
        _wishItemsList.value = notificationList
    }

    fun saveItemToFirestore(item: ItemModel) {

        val myItemRef = firestoreRepository.getItemDocument(item.documentName!!)


            myItemRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    Log.e(TAG, "Item exists, proceed to update!")
                    firestoreRepository.saveItem(item).addOnFailureListener {
                        Log.e(TAG, "Failed to save Item!")
                    }
                }
                else{
                    Log.e(TAG, "Creating new item!")
                    firestoreRepository.createItem(item)
                }

            }


    }

    fun saveNotificationToFirestore(not: NotificationModel) {
        firestoreRepository.saveNotification(not).addOnFailureListener {
            Log.e(TAG, "Failed to save Notification!")
        }
    }

    fun saveFeedbackToFirestore(feed: FeedbackModel) {
        firestoreRepository.saveFeedback(feed).addOnFailureListener {
            Log.e(TAG, "Failed to save Feedback!")
        }
    }

    fun updateUserLocation(address: GeoPoint, location: String, ownerId: String){
        firestoreRepository.updateUserLocation(address, location, ownerId)
    }

    fun updateItemLocation(address: GeoPoint, location: String, itemId: String){
        firestoreRepository.updateItemLocation(address, location, itemId)
    }

    fun updateStatus(status: String, item_id: String) {
        firestoreRepository.updateStatus(status, item_id)
    }

    fun updateStatusFeed(item_id: String) {
        firestoreRepository.updateStatusFeed(item_id)
    }

    fun updateRating(ownerid: String, newrate: Float) {
        firestoreRepository.updateRating(ownerid, newrate)
    }

    fun setSoldTo(uid: String, itemId: String) {
        firestoreRepository.setSoldTo(uid, itemId)
    }

    fun cancelAllNotifications(item_id: String) {
        firestoreRepository.removeAllNotifications(item_id)
    }

    fun fetchSingleItemFromFirestore(documentName: String) {
        firestoreRepository.getItemDocument(documentName)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _item.value = null
                    return@EventListener
                }

                if (value != null && value.exists()) {
                    Log.d(TAG, "Current data: ${value.data}")
                    _item.value = value.toObject(ItemModel::class.java)
                } else {
                    Log.d(TAG, "Current data: null")
                    //Log.d(TAG, _item.value?.title)
                }
            })
    }

    fun fetchMyItemListFromFirestore() {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        firestoreRepository.itemRef.whereEqualTo("ownerId", userId)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _myItemList.value = null
                    return@EventListener
                }

                val savedItemList: MutableList<ItemModel> = mutableListOf()
                for (doc in value!!) {
                    val item = doc.toObject(ItemModel::class.java)
                    savedItemList.add(item)
                }
                _myItemList.value = savedItemList
            })
    }

    fun fetchAllItemListFromFirestore() {
        firestoreRepository.itemRef
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _allItemList.value = null
                    return@EventListener
                }

                val allItemList: MutableList<ItemModel> = mutableListOf()
                for (doc in value!!) {
                    val item = doc.toObject(ItemModel::class.java)
                    val date = item.expireDate!!.split("/")
                    if (!validateDate(date) && item.status == AVAILABLE)
                        updateStatus(BLOCKED, item.documentName!!)
                    allItemList.add(item)
                }
                _allItemList.value = allItemList
                this.fetchAvailableItemListFromFirestore()
            })
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

    fun fetchAvailableItemListFromFirestore() {
        firestoreRepository.itemRef.whereEqualTo("status", AVAILABLE)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _availableItemList.value = null
                    return@EventListener
                }

                val availableItemList: MutableList<ItemModel> = mutableListOf()
                for (doc in value!!) {
                    val item = doc.toObject(ItemModel::class.java)
                    availableItemList.add(item)
                }
                _availableItemList.value = availableItemList
            })
    }

    fun fetchSoldItemListFromFirestore() {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        firestoreRepository.itemRef
            .whereEqualTo("status", SOLD)
            .whereEqualTo("soldTo", userId)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _soldItemsList.value = null
                    return@EventListener
                }

                val soldList: MutableList<ItemModel> = mutableListOf()
                for (doc in value!!) {
                    val item = doc.toObject(ItemModel::class.java)
                    soldList.add(item)
                }
                _soldItemsList.value = soldList
            })
    }

    fun fetchUserFromFirestore(userID: String) {
        firestoreRepository.getUserDocument(userID).addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                Log.w(TAG, "USER Listen failed.", e)
                _user.value = null
                return@EventListener
            }

            if (value != null && value.exists()) {
                Log.d(TAG, "USER Current data: ${value.data}")
                _user.value = value.toObject(UserModel::class.java)
            } else {
                Log.d(TAG, "USER Current data: null")
            }
        })
    }

    fun fetchMyUserFromFirestore() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            userId = FirebaseAuth.getInstance().currentUser!!.uid
        }

        // This method is called only when the user is authenticated, so we can use user
        firestoreRepository.getUserDocument(userId)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "USER Listen failed.", e)
                    _myUser.value = null
                    return@EventListener
                }

                if (value != null && value.exists()) {
                    Log.d(TAG, "USER Current data: ${value.data}")
                    _myUser.value = value.toObject(UserModel::class.java)
                } else {
                    Log.d(TAG, "USER Current data: null")
                }
            })
    }

    fun fetchAllNotificationsFromFirestore() {
        firestoreRepository.notRef.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                _myNotificationsList.value = null
                return@EventListener
            }

            val notificationList: MutableList<NotificationModel> = mutableListOf()
            for (doc in value!!) {
                val notification = doc.toObject(NotificationModel::class.java)
                notificationList.add(notification)
            }
            _myNotificationsList.value = notificationList
        })
    }

    fun saveUserToFirestore(myUser: UserModel) {
        this.saveUser(myUser).addOnFailureListener {
            Log.e(TAG, "USER Failed to save User!")
        }
    }

    fun getItemNotification(itemId: String) {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        var requested = false
        val task = firestoreRepository.firestore.collection("notifications")
            .whereEqualTo("id_item", itemId).get()

        task.addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val notification = document.toObject(NotificationModel::class.java)
                if (notification.id_user.toString() == userId) {
                    requested = true
                }
            }
            firestoreRepository.onFirestoreTaskComplete.fetchNotifications(requested)

        }
    }

    fun getItemData(
        minPrice: Int = 0,
        maxPrice: Int = Int.MAX_VALUE,
        mainCategory: String? = null,
        secondCategory: String? = null
    ) {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        val firstTask: Task<QuerySnapshot>
        val secondTask: Task<QuerySnapshot>

        if (mainCategory == null) {
            firstTask = firestoreRepository.firestore.collection("items")
                .whereLessThan("ownerId", userId)
                .whereEqualTo("status", AVAILABLE).get()

            secondTask = firestoreRepository.firestore.collection("items")
                .whereGreaterThan("ownerId", userId)
                .whereEqualTo("status", AVAILABLE).get()
        } else {
            firstTask = firestoreRepository.firestore.collection("items")
                .whereLessThan("ownerId", userId)
                .whereEqualTo("mainCategory", mainCategory)
                .whereEqualTo("secondCategory", secondCategory)
                .whereEqualTo("status", AVAILABLE)
                .get()

            secondTask = firestoreRepository.firestore.collection("items")
                .whereGreaterThan("ownerId", userId)
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

                firestoreRepository.onFirestoreTaskComplete.itemListDataAdded(list)
            }
    }

    fun removeNotification(itemId: String) {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        val docQuery = firestoreRepository.firestore.collection("notifications")
            .whereEqualTo("id_item", itemId)
            .whereEqualTo("id_user", userId).get()

        docQuery.addOnSuccessListener { documents ->
            for (document in documents) {
                document.reference.delete().addOnSuccessListener {
                    Log.d(TAG, "Successfully removed")
                }
            }
        }.addOnFailureListener { Log.d(TAG, "Error in removing") }
    }

    private fun saveUser(myUser: UserModel): Task<Void> {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        val documentReferenceUser = firestoreRepository.firestore.collection("users")
            .document(userId)

        return documentReferenceUser.set(myUser)
            .addOnSuccessListener { Log.d(TAG, "USER Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "USER Error in saving") }
    }

    fun getUsersData(itemID: String) {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        val interestedUsers = ArrayList<String>()
        val list = ArrayList<UserModel>()
        firestoreRepository.firestore.collection("notifications")
            .whereEqualTo("id_item", itemID)
            .whereEqualTo("id_owner", userId)
            .get().addOnSuccessListener { notifications ->
                for (doc in notifications) {
                    val username = doc.get("id_user").toString()
                    interestedUsers.add(username)
                }

                if (interestedUsers.isNotEmpty()) {
                    firestoreRepository.firestore.collection("users")
                        .whereIn(FieldPath.documentId(), interestedUsers).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val user = document.toObject(UserModel::class.java)
                                user.userId = document.id
                                list.add(user)
                                firestoreRepository.onFirestoreTaskComplete.userListDataAdded(list)
                            }
                        }
                } else firestoreRepository.onFirestoreTaskComplete.userListDataAdded(list)
            }
    }

    fun getNotificationData() {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        val likedItems = ArrayList<String>()
        val list = ArrayList<ItemModel>()
        firestoreRepository.firestore.collection("notifications")
            .whereEqualTo("id_user", userId).get()
            .addOnSuccessListener { notifications ->
                for (doc in notifications) {
                    val itemName = doc.get("id_item").toString()
                    likedItems.add(itemName)
                }

                if (likedItems.isNotEmpty()) {
                    firestoreRepository.firestore.collection("items")
                        .whereEqualTo("status", "available")
                        .whereIn("documentName", likedItems)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val item = document.toObject(ItemModel::class.java)
                                list.add(item)
                            }
                            firestoreRepository.onFirestoreTaskComplete.notListDataAdded(list)
                        }
                } else {
                    firestoreRepository.onFirestoreTaskComplete.notListDataAdded(list)
                }
            }
    }

    fun updateToken(token: String) {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        // This method is called only when the user is authenticated, so we can use user
        firestoreRepository.firestore.collection("users")
            .document(userId)
            .update("token", token)
    }

    internal var user: MutableLiveData<UserModel>
        get() {
            return _user
        }
        set(value) {
            _user = value
        }

    internal var myUser: MutableLiveData<UserModel>
        get() {
            return _myUser
        }
        set(value) {
            _myUser = value
        }

    internal var isRequested: MutableLiveData<Boolean>
        get() {
            return _isRequested
        }
        set(value) {
            _isRequested = value
        }

    internal var myNotificationsList: MutableLiveData<List<NotificationModel>>
        get() {
            return _myNotificationsList
        }
        set(value) {
            _myNotificationsList = value
        }

    internal var wishItemsList: MutableLiveData<List<ItemModel>>
        get() {
            return _wishItemsList
        }
        set(value) {
            _wishItemsList = value
        }

    internal var item: MutableLiveData<ItemModel>
        get() {
            return _item
        }
        set(value) {
            _item = value
        }

    internal var onSaleItemList: MutableLiveData<List<ItemModel>>
        get() {
            return _onSaleItemList
        }
        set(value) {
            _onSaleItemList = value
        }

    internal var myItemList: MutableLiveData<List<ItemModel>>
        get() {
            return _myItemList
        }
        set(value) {
            _myItemList = value
        }

    internal var availableItemList: MutableLiveData<List<ItemModel>>
        get() {
            return _availableItemList
        }
        set(value) {
            _availableItemList = value
        }

    internal var allItemList: MutableLiveData<List<ItemModel>>
        get() {
            return _allItemList
        }
        set(value) {
            _allItemList = value
        }

    internal var interestedUserList: MutableLiveData<List<UserModel>>
        get() {
            return _interestedUserList
        }
        set(value) {
            _interestedUserList = value
        }

    internal var soldItemsList: MutableLiveData<List<ItemModel>>
        get() {
            return _soldItemsList
        }
        set(value) {
            _soldItemsList = value
        }
}