package it.polito.mad.splintersell.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener

class FirestoreViewModel : ViewModel(), FirestoreRepository.OnFirestoreTaskComplete {
    val TAG = "FIRESTORE_VIEW_MODEL"
    var firestoreRepository = FirestoreRepository(this)
    var user = FirebaseAuth.getInstance().currentUser

    private var _myUser: MutableLiveData<UserModel> = MutableLiveData()
    private var _myUserNav: MutableLiveData<UserModel> = MutableLiveData()
    private var _notificationsList: MutableLiveData<List<NotificationModel>> = MutableLiveData()
    private var _wishItemsList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _isrequested: MutableLiveData<Boolean> = MutableLiveData()
    private var _item: MutableLiveData<ItemModel> = MutableLiveData()
    private var _onSaleItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _myItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _allItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _interestedUserList: MutableLiveData<List<UserModel>> = MutableLiveData()


    override fun itemListDataAdded(itemModelList: List<ItemModel>) {
        _onSaleItemList.value = itemModelList
    }

    override fun fetchNotifications(requested: Boolean) {
        _isrequested.value = requested
    }

    override fun userListDataAdded(userList: List<UserModel>) {
        _interestedUserList.value=userList
    }

    override fun notListDataAdded(notificationList: List<ItemModel>) {
        _wishItemsList.value = notificationList
    }

    fun saveItemToFirestore(item: ItemModel) {
        firestoreRepository.saveItem(item).addOnFailureListener {
            Log.e(TAG, "Failed to save Item!")
        }
    }

    fun saveNotificationToFirestore(not: NotificationModel) {
        firestoreRepository.saveNotification(not).addOnFailureListener {
            Log.e(TAG, "Failed to save Notification!")
        }
    }


    fun updateStatus(status: String, item_id: String) {
        firestoreRepository.updateStatus(status,item_id)
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
                }
            })
    }

    fun fetchMyItemListFromFirestore() {
        firestoreRepository.itemRef
            .whereEqualTo("ownerId", user!!.uid)
            .whereEqualTo("status","Available")
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _myItemList.value = null
                    return@EventListener
                }

                val savedItemList : MutableList<ItemModel> = mutableListOf()
                for (doc in value!!) {
                    val item = doc.toObject(ItemModel::class.java)
                    savedItemList.add(item)
                }
                    _myItemList.value = savedItemList
            })
    }

    fun fetchAllItemListFromFirestore() {
        firestoreRepository.itemRef
            .whereEqualTo("status","Available")
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _myItemList.value = null
                    return@EventListener
                }

                val allItemList : MutableList<ItemModel> = mutableListOf()
                for (doc in value!!) {
                    val item = doc.toObject(ItemModel::class.java)
                    allItemList.add(item)
                }
                _allItemList.value = allItemList
            })
    }


    fun fetchUserFromFirestore(userID:String) {
        firestoreRepository.getUserDocument(userID)
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


    fun fetchMyUserFromFirestore() {
        firestoreRepository.getUserDocument(user!!.uid)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "USER Listen failed.", e)
                    _myUserNav.value = null
                    return@EventListener
                }

                if (value != null && value.exists()) {
                    Log.d(TAG, "USER Current data: ${value.data}")
                    _myUserNav.value = value.toObject(UserModel::class.java)
                } else {
                    Log.d(TAG, "USER Current data: null")
                }
            })
    }

    fun fetchAllNotificationsFromFirestore() {
        firestoreRepository.notRef
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    _notificationsList.value = null
                    return@EventListener
                }

                val notificatioList : MutableList<NotificationModel> = mutableListOf()
                for (doc in value!!) {
                    val notification = doc.toObject(NotificationModel::class.java)
                    notificatioList.add(notification)
                }
                _notificationsList.value = notificatioList
            })
    }



    fun saveUserToFirestore(myUser: UserModel) {
        firestoreRepository.saveUser(myUser).addOnFailureListener {
            Log.e(TAG, "USER Failed to save User!")
        }
    }



    internal var myUser: MutableLiveData<UserModel>
        get() { return _myUser }
        set(value) { _myUser = value }

    internal var myUserNav: MutableLiveData<UserModel>
        get() { return _myUserNav }
        set(value) { _myUserNav = value }

    internal var isrequested : MutableLiveData<Boolean>
        get() { return _isrequested }
        set(value) { _isrequested = value }
    internal var myNotificationsList: MutableLiveData<List<NotificationModel>>
        get() { return _notificationsList }
        set(value) { _notificationsList = value }

    internal var wishItemsList: MutableLiveData<List<ItemModel>>
        get() { return _wishItemsList }
        set(value) { _wishItemsList = value }

    internal var item: MutableLiveData<ItemModel>
        get() { return _item }
        set(value) { _item = value }

    internal var onSaleItemList: MutableLiveData<List<ItemModel>>
        get() { return _onSaleItemList }
        set(value) { _onSaleItemList = value }

    internal var myItemList: MutableLiveData<List<ItemModel>>
        get() { return _myItemList }
        set(value) { _myItemList = value }

    internal var allItemList: MutableLiveData<List<ItemModel>>
        get() { return _allItemList }
        set(value) { _allItemList = value }

    internal var interestedUserList: MutableLiveData<List<UserModel>>
        get() { return _interestedUserList }
        set(value) { _interestedUserList = value }
}