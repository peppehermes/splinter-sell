package it.polito.mad.splintersell.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class FirestoreViewModel : ViewModel(), FirestoreRepository.OnFirestoreTaskComplete {
    val TAG = "FIRESTORE_VIEW_MODEL"
    var firestoreRepository = FirestoreRepository(this)
    var user = FirebaseAuth.getInstance().currentUser
    private var _item: MutableLiveData<ItemModel> = MutableLiveData()
    private var _onSaleItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var _myItemList: MutableLiveData<List<ItemModel>> = MutableLiveData()

    init {
        firestoreRepository.getItemData()
        //fetchOnSaleItemList()
    }

    override fun itemListDataAdded(itemModelList: List<ItemModel>) {
        _onSaleItemList.value = itemModelList
    }

    fun saveItemToFirestore(item: ItemModel) {
        firestoreRepository.saveItem(item).addOnFailureListener {
            Log.e(TAG, "Failed to save Item!")
        }
    }

    fun fetchItemFromFirestore(documentName:String) {
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
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
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

    private fun fetchOnSaleItemList() {
        val itemCollection = firestoreRepository.itemRef
        val onSaleItems = ArrayList<ItemModel>()

        val task1 = itemCollection.whereLessThan("ownerId", user!!.uid)
            .get()

        val task2 = itemCollection.whereGreaterThan("ownerId", user!!.uid)
            .get()

        val allTasks = Tasks.whenAllSuccess<QuerySnapshot>(task1, task2)

        allTasks.addOnSuccessListener { querySnapshots ->
            for (queryDocumentSnapshots in querySnapshots) {
                for (documentSnapshot in queryDocumentSnapshots) {
                    val item = documentSnapshot.toObject(ItemModel::class.java)

                    onSaleItems.add(item)
                }
            }
            _onSaleItemList.value = onSaleItems
        }
    }

    internal var item: MutableLiveData<ItemModel>
        get() { return _item }
        set(value) { _item = value }

    internal var onSaleItemList: MutableLiveData<List<ItemModel>>
        get() { return _onSaleItemList }
        set(value) { _onSaleItemList = value }

    internal var myItemList: MutableLiveData<List<ItemModel>>
        get() { return _myItemList }
        set(value) { _myItemList = value }
}