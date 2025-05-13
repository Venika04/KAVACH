package com.example.kavach

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Contact(val name: String, val phone: String, val relationship: String)

class ContactViewModel : ViewModel() {
    val contactList = mutableStateListOf<Contact>()

    fun addContact(contact: Contact) {
        contactList.add(contact)
    }

    fun updateContact(index: Int, contact: Contact) {
        if (index in contactList.indices) {
            contactList[index] = contact
        }
    }

    fun removeContact(index: Int) {
        if (index in contactList.indices) {
            contactList.removeAt(index)
        }
    }
}
