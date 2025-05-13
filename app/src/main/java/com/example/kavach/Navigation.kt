package com.example.kavach

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import android.location.Location
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    getLocation: () -> android.location.Location?,
    sendSOS: (android.location.Location?) -> Unit
) {
    val contactViewModel: ContactViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                navController = navController,
                getLocation = getLocation,
                sendSOS = sendSOS,
                contactViewModel = contactViewModel
            )
        }

        composable("profile") {
            ProfileScreen(navController)
        }

        composable("add_contact") {
            AddContactScreen(navController)
        }

    }
}
