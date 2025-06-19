package com.example.kavach.contact

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Contact data class
data class Contact(val name: String, val phone: String, val relationship: String)

class ContactViewModel : ViewModel() {
    val contactList = mutableStateListOf<Contact>()
    var isLoading by mutableStateOf(false)
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun fetchContacts() {
        val userId = auth.currentUser?.uid ?: return
        isLoading = true
        db.collection("users")
            .document(userId)
            .collection("contacts")
            .get()
            .addOnSuccessListener { documents ->
                contactList.clear()
                for (doc in documents) {
                    val name = doc.getString("name") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val relationship = doc.getString("relationship") ?: ""
                    contactList.add(Contact(name, phone, relationship))
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun addContact(contact: Contact) {
        val userId = auth.currentUser?.uid ?: return
        contactList.add(contact)
        db.collection("users").document(userId)
            .collection("contacts")
            .add(contact)
    }

    fun updateContact(index: Int, contact: Contact) {
        val userId = auth.currentUser?.uid ?: return
        if (index in contactList.indices) {
            val oldContact = contactList[index]
            contactList[index] = contact

            db.collection("users").document(userId).collection("contacts")
                .whereEqualTo("phone", oldContact.phone) // Assuming phone is unique
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        db.collection("users").document(userId)
                            .collection("contacts").document(doc.id)
                            .set(contact)
                    }
                }
        }
    }

    fun removeContact(index: Int) {
        val userId = auth.currentUser?.uid ?: return
        if (index in contactList.indices) {
            val contact = contactList[index]
            contactList.removeAt(index)

            db.collection("users").document(userId).collection("contacts")
                .whereEqualTo("phone", contact.phone)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        db.collection("users").document(userId)
                            .collection("contacts").document(doc.id)
                            .delete()
                    }
                }
        }
    }

    // Helper method to get just phone numbers (used for SOS SMS)
    fun getEmergencyContactNumbers(): List<String> {
        return contactList.map { it.phone }
    }
}
