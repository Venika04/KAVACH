package com.example.kavach.main.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.kavach.contact.ContactViewModel
import com.example.kavach.main.triggerSOS
import com.example.kavach.main.util.ACTION_TRIGGER_SOS


class SosReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_TRIGGER_SOS") {
            Log.d("KavachSOS", "Broadcast received from floating bubble")

            val contactViewModel = ContactViewModel()

            triggerSOS(
                context = context,
                contactViewModel = contactViewModel
            ) { _, _ -> }
        }
    }
}
