package it.polito.mad.splintersell.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FirestoreRepository(private val onFirestoreTaskComplete: OnFirestoreTaskComplete) {
    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    val itemRef = firestoreDB.collection("items")
    var user = FirebaseAuth.getInstance().currentUser

    fun getItemDocument(documentName: String): DocumentReference {
        return firestoreDB.collection("items")
            .document(documentName)
    }

    fun getItemData() {
        val firstTask = FirebaseFirestore.getInstance()
            .collection("items")
            .whereGreaterThan("ownerId", user!!.uid)
            .get()

        val secondTask = FirebaseFirestore.getInstance()
            .collection("items")
            .whereLessThan("ownerId", user!!.uid)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(firstTask, secondTask)
            .addOnSuccessListener {querySnaps ->
                for (docSnap in querySnaps)
                    onFirestoreTaskComplete.itemListDataAdded(docSnap.toObjects(ItemModel::class.java))
            }
    }

    fun saveItem(item: ItemModel): Task<Void> {
        val documentReferenceUser = firestoreDB.collection("items")
            .document(item.documentName!!)

        return documentReferenceUser.set(item)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving")}
    }

    interface OnFirestoreTaskComplete {
        fun itemListDataAdded(itemModelList: List<ItemModel>)
    }
}

