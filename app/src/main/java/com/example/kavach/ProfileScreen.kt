package com.example.kavach

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavHostController) {
    val topBarColor = Color(0xFF6200EE)
    val backgroundColor = Color(0xFFF3E5F5)
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "No Email Found"
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("KavachPrefs", Context.MODE_PRIVATE)
    val username = sharedPrefs.getString("username", "Unknown User")


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kavach_logo),
                            contentDescription = "Kavach Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("KAVACH", color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = topBarColor
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(it)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Username: $username", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Email: $email", fontSize = 18.sp, fontWeight = FontWeight.Bold)                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🚨 Log Out Button
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                val items = listOf("My Contacts", "Location History", "Safety Tips", "About App")

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(items.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable {
                                    // Future navigation here
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(items[index], fontWeight = FontWeight.SemiBold)
                        }
                    }
                }



            }
        }
    )
}
