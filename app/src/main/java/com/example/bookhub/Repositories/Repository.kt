package com.example.bookhub.Repositories

import com.example.bookhub.Models.AudioData
import com.example.bookhub.Models.BookData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class Repository @Inject constructor(private val db: FirebaseFirestore) {

    suspend fun addUserLike(documentId: String, userId: String, result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documentRef = db.collection("AudioStory").document(documentId)
                documentRef.update("likedUserIds", FieldValue.arrayUnion(userId)).await()
                withContext(Dispatchers.Main) {
                    result.invoke("You add Like to the Story")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.invoke(e.message.toString())
                }
            }
        }
    }

    suspend fun removeUserLike(documentId: String, userId: String, result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documentRef = db.collection("AudioStory").document(documentId)
                documentRef.update("likedUserIds", FieldValue.arrayRemove(userId)).await()
                withContext(Dispatchers.Main) {
                    result.invoke("You remove Like from the Story")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.invoke(e.message.toString())
                }
            }
        }
    }

    suspend fun isUserLiked(documentId: String, userId: String): Boolean {
        val documentRef = db.collection("AudioStory").document(documentId).get().await()
        val likeDocument = documentRef.toObject(AudioData::class.java)
        return likeDocument?.likedUserIds?.contains(userId) ?: false

    }

    suspend fun Delete(
        documentId: String,
        userId: String,
        isDelete: Boolean,
        result: (String) -> Unit,
    ): Boolean {
        val docRef = db.collection("AudioStory").document(documentId)
        val documentSnapshot = docRef.get().await()
        documentSnapshot?.let { snapshot ->
            if (snapshot.exists()) {
                if (isDelete) {
                    docRef.delete().await()
                    withContext(Dispatchers.Main) {
                        result.invoke("Story deleted successfully!!")
                    }
                } else {
                    return true
                }
            }
        }
        return false
    }

    //    suspend fun AudioList(result: (String) -> Unit): ArrayList<AudioData> {
//        val collectionRef = db.collection("AudioStory")
//        try {
//            val querySnapshot = collectionRef.get().await()
//            val audioList = querySnapshot.documents.map { documentSnapshot ->
//                documentSnapshot.toObject(AudioData::class.java)
//            }
//
//
//            val sortedAudioList = audioList.sortedByDescending {
//                it?.likedUserIds?.size ?: 0
//            }.take(10)
//            return java.util.ArrayList(sortedAudioList)
//        } catch (e: Exception) {
//            result.invoke(e.message.toString())
//            return ArrayList()
//        }
//    }
    fun AudioList(list: (ArrayList<AudioData>) -> Unit) {
        val collectionRef = db.collection("AudioStory")
        try {
            // Add a real-time snapshot listener to the collection
            collectionRef.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    // Handle the exception if necessary
                    return@addSnapshotListener
                }
                querySnapshot?.let { snapshot ->
                    val audioList = snapshot.documents.map { documentSnapshot ->
                        documentSnapshot.toObject(AudioData::class.java)
                    }

                    val sortedAudioList = audioList.sortedByDescending {
                        it?.likedUserIds?.size ?: 0
                    }.take(7)

                    list.invoke(java.util.ArrayList(sortedAudioList))
                }
            }
        } catch (e: Exception) {
            // Handle the exception if necessary
            list.invoke(arrayListOf())
        }
    }
    fun BookList(list: (ArrayList<BookData>) -> Unit) {
        val collectionRef = db.collection("Books")
        try {
            // Add a real-time snapshot listener to the collection
            collectionRef.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    // Handle the exception if necessary
                    return@addSnapshotListener
                }
                querySnapshot?.let { snapshot ->
                    val bookList = snapshot.documents.map { documentSnapshot ->
                        documentSnapshot.toObject(BookData::class.java)
                    }

                    val sortedBookList = bookList.sortedByDescending {
                        it?.review?.toInt() ?: 0
                    }.take(7)

                    list.invoke(java.util.ArrayList(sortedBookList))
                }
            }
        } catch (e: Exception) {
            // Handle the exception if necessary
            list.invoke(arrayListOf())
        }
    }
}

