package it.polito.mad.splintersell.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import java.util.*

class FirestoreViewModel : ViewModel(), FirestoreRepository.OnFirestoreTaskComplete {
    val TAG = "FIRESTORE_VIEW_MODEL"
    val AVAILABLE = "available"
    val BLOCKED = "blocked"
    var firestoreRepository = FirestoreRepository(this)
    var user = FirebaseAuth.getInstance().currentUser

    private var _myUser: MutableLiveData<UserModel> = MutableLiveData()
    private var _myUserNav: MutableLiveData<UserModel> = MutableLiveData()
    private var _myNotificationsList: MutableLiveData<List<NotificationModel>> = MutableLiveData()
    private var _wishItemsList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _isRequested: MutableLiveData<Boolean> = MutableLiveData()
    private var _item: MutableLiveData<ItemModel> = MutableLiveData()
    private var _onSaleItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _myItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _availableItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _allItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _interestedUserList: MutableLiveData<List<UserModel>> = MutableLiveData()

    init {
        this.fetchAllItemListFromFirestore()
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
        firestoreRepository.updateStatus(status, item_id)
    }

    fun updateToken(token: String) {
        firestoreRepository.updateToken(token)
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
        firestoreRepository.itemRef.whereEqualTo("ownerId", user!!.uid)
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
                    if (!validateDate(date))
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

    fun fetchUserFromFirestore(userID: String) {
        firestoreRepository.getUserDocument(userID).addSnapshotListener(EventListener { value, e ->
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
        firestoreRepository.saveUser(myUser).addOnFailureListener {
            Log.e(TAG, "USER Failed to save User!")
        }
    }


    internal var myUser: MutableLiveData<UserModel>
        get() {
            return _myUser
        }
        set(value) {
            _myUser = value
        }

    internal var myUserNav: MutableLiveData<UserModel>
        get() {
            return _myUserNav
        }
        set(value) {
            _myUserNav = value
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
}