package com.example.kavach.help

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kavach.R
import com.example.kavach.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavHostController) {
    val context = LocalContext.current

    var showProcedures by remember { mutableStateOf(false) }
    var showNumbers by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }

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
                        Text("KAVACH", color = Color.White, style = AppTypography.titleLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF8000FF))
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF2EAFB), Color(0xFFE8DFFC))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    ExpandableCard(
                        title = "Emergency Procedures",
                        expanded = showProcedures,
                        onClick = { showProcedures = !showProcedures }
                    ) {
                        EmergencyProcedureList()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExpandableCard(
                        title = "Important Numbers",
                        expanded = showNumbers,
                        onClick = { showNumbers = !showNumbers }
                    ) {
                        EmergencyContactItem("Police", "100")
                        EmergencyContactItem("Ambulance", "102")
                        EmergencyContactItem("Women Helpline", "1091")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExpandableCard(
                        title = "Contact Support",
                        expanded = showSupport,
                        onClick = { showSupport = !showSupport }
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFF8000FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:kavach.support@example.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Support Request - Kavach App")
                                    }
                                    context.startActivity(emailIntent)
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Email Us", style = AppTypography.labelLarge)
                                Text(
                                    "kavach.support@example.com",
                                    style = AppTypography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ExpandableCard(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color(0xFF8000FF)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = AppTypography.titleLarge,
                    color = Color(0xFF8000FF)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF8000FF)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = androidx.compose.animation.expandVertically(animationSpec = tween(300)),
                exit = androidx.compose.animation.shrinkVertically(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp), content = content)
            }
        }
    }
}

@Composable
fun EmergencyProcedureList() {
    val procedures = listOf(
        "If you're feeling unsafe, tap the SOS button immediately.",
        "Stay in well-lit and public areas if you're lost.",
        "Call trusted contacts via Kavach contact list.",
        "Alert nearby people if in danger."
    )

    Column {
        procedures.forEach { step ->
            Text(
                "• $step",
                style = AppTypography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmergencyContactItem(name: String, number: String) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFF8000FF)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text("$name: $number", style = AppTypography.bodyLarge)
        }
    }
}
