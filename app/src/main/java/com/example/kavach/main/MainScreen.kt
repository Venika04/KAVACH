package com.example.kavach.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.kavach.R
import com.example.kavach.contact.ContactViewModel


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    getLocation: () -> Location?,
    sendSOS: (Location?) -> Unit,
    navController: NavHostController,
    contactViewModel: ContactViewModel = viewModel()
) {
    val topBarColor = Color(0xFF6200EE)
    val backgroundColor = Color(0xFFF3E5F5)
    val bottomBarColor = topBarColor


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kavach_logo), // your logo
                            contentDescription = "Kavach Logo",
                            modifier = Modifier.size(36.dp).clip(CircleShape) // adjust size as needed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("KAVACH", color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("profile")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = topBarColor
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = bottomBarColor) {
                BottomNavigationItem(
                    selected = false,
                    onClick = {
                        navController.navigate("add_contact")
                    },
                    label = { Text("Contact") },
                    icon = { },
                    alwaysShowLabel = true
                )
                BottomNavigationItem(selected = false, onClick = { }, label = { Text("Rating") }, icon = { }, alwaysShowLabel = true)
                BottomNavigationItem(selected = false, onClick = { }, label = { Text("Help") }, icon = { }, alwaysShowLabel = true)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(it),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(top = 40.dp)
            ) {
                LocationBox()
                Spacer(modifier = Modifier.height(40.dp))
                SOSButton { sendSOS(getLocation()) }
            }
        }
    }
}


@Composable
fun LocationBox() {
//    Surface(
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxWidth(0.9f)
//            .height(100.dp),
//        color = Color.White,
//        elevation = 4.dp
//    ) {
//        Box(contentAlignment = Alignment.Center) {
//            Text("Current Location", fontSize = 18.sp, color = Color.Black)
//        }
//    }

    LocationScreen()
}


@Composable
fun SOSButton(onClick: ()-> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
        modifier = Modifier.size(100.dp)
    ) {
        Text("SOS", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun BoxPlaceholder() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Box", style = MaterialTheme.typography.bodyMedium)
    }
}
