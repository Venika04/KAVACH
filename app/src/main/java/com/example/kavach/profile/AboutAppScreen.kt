package com.example.kavach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kavach.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("About Kavach", color = Color.White, style = AppTypography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4B0082))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE8DFFC), Color(0xFF4B0082))
                        )
                    )
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "KAVACH is a comprehensive safety application designed to empower women and ensure their safety at all times.",
                    style = AppTypography.bodyLarge,
                    color = Color(0xFF1C1C1C)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "App Features:",
                    style = AppTypography.titleMedium,
                    color = Color(0xFF4B0082)
                )

                Spacer(modifier = Modifier.height(8.dp))
                val features = listOf(
                    "• SOS Alert System",
                    "• Emergency Contacts",
                    "• Real-Time Location Sharing",
                    "• Location Rating and Reviews",
                    "• Voice and Shake Detection",
                    "• Offline SOS Triggers",
                    "• Safety Videos and Legal Awareness"
                )
                features.forEach {
                    Text(
                        text = it,
                        style = AppTypography.bodyMedium,
                        color = Color(0xFF1C1C1C),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Our mission is to promote safety, awareness, and confidence among users through innovation and community.",
                    style = AppTypography.bodyMedium,
                    color = Color(0xFF1C1C1C)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Developed by:",
                    style = AppTypography.titleSmall,
                    color = Color(0xFF4B0082)
                )
                Text(
                    text = "Team Kavach\nFor academic and social impact purposes.",
                    style = AppTypography.bodyMedium,
                    color = Color(0xFF1C1C1C)
                )
            }
        }
    )
}
