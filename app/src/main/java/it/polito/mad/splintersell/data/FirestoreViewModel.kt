package it.polito.mad.splintersell.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener

class FirestoreViewModel : ViewModel() {
    val TAG = "FIRESTORE_VIEW_MODEL"
    var firestoreRepository =
        FirestoreRepository()
    var user = FirebaseAuth.getInstance().currentUser
    var item: MutableLiveData<ItemModel> = MutableLiveData()

    fun saveItemToFirestore(item: ItemModel) {
        firestoreRepository.saveItem(item).addOnFailureListener {
            Log.e(TAG, "Failed to save Item!")
        }
    }

    fun getItemFromFirestore(documentName:String): MutableLiveData<ItemModel> {
        firestoreRepository.getItemDocument(documentName).addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                item.value = null
                return@EventListener
            }
            if (value != null && value.exists()) {
                Log.d(TAG, "Current data: ${value.data}")
                item.value = value.toObject(ItemModel::class.java)
            } else {
                Log.d(TAG, "Current data: null")
            }
        })

        return item
    }

}