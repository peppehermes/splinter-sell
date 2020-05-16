package it.polito.mad.splintersell.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun getItemDocument(documentName: String): DocumentReference {
        return firestoreDB.collection("items")
            .document(documentName)
    }

    fun getItemCollection(collectionName: String) : CollectionReference {
        return firestoreDB.collection("items")
    }

    fun saveItem(item: ItemModel): Task<Void> {
        val documentReferenceUser = firestoreDB.collection("items")
            .document(item.documentName!!)

        return documentReferenceUser.set(item)
            .addOnSuccessListener { Log.d(TAG, "Successfully saved") }
            .addOnFailureListener { Log.d(TAG, "Error in saving")}
    }
}

