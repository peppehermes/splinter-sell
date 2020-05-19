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
    private var _item: MutableLiveData<ItemModel> = MutableLiveData()
    private var _onSaleItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _myItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _allItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    var _isrequested : MutableLiveData<Boolean> = MutableLiveData()

    init {
        firestoreRepository.getItemData()
        //fetchOnSaleItemList()
    }

    override fun itemListDataAdded(itemModelList: List<ItemModel>) {
        _onSaleItemList.value = itemModelList
    }

    override fun fetchNotifications(requested:Boolean){
        _isrequested.value=requested
    }

    fun getNotifications(item_id :String){
        firestoreRepository.getItemNotification(item_id)
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

    fun cancelNotifications(item_id :String){
        firestoreRepository.removeNotifications(item_id)
    }


    fun fetchSingleItemFromFirestore(documentName:String) {
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


    fun fetchMyItemListFromFirestore() {
        firestoreRepository.itemRef
            .whereEqualTo("ownerId", user!!.uid)
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
}