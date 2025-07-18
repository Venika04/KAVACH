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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    var showTips by remember { mutableStateOf(false) }
    var showLaws by remember { mutableStateOf(false) }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4B0082))
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE8DFFC), Color(0xFF4B0082))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EAFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFF4B0082)),
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
                                Text("Email Us", style = AppTypography.labelLarge, color = Color(0xFF1C1C1C))
                                Text(
                                    "kavach.support@example.com",
                                    style = AppTypography.bodyMedium,
                                    color = Color(0xFF1C1C1C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExpandableCard(
                        title = "Safety Tips (Videos)",
                        expanded = showTips,
                        onClick = { showTips = !showTips }
                    ) {
                        SafetyTipLink("Self-Defense Basics", "https://youtube.com/shorts/OR7RXkHquaU?si=2fGyk0LyvQxPphqL")
                        SafetyTipLink("Top 5 Safety Tips for Women", "https://youtu.be/0TGUVEmoM8E?si=GzBTZB99k50rC2k1")
                        SafetyTipLink("React in Dangerous Situations", "https://youtu.be/KVpxP3ZZtAc?si=xjCrh2XAPCkRYbAx")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExpandableCard(
                        title = "Know Your Rights (Basic Laws)",
                        expanded = showLaws,
                        onClick = { showLaws = !showLaws }
                    ) {
                        val laws = listOf(
                            "Right to File Zero FIR" to "You can file an FIR at any police station, regardless of where the incident occurred. The police must register it.\n\n(Under: CrPC Section 154, Supreme Court Guidelines)",

                            "Right to No Arrest at Night" to "Women cannot be arrested after sunset and before sunrise without prior approval from a magistrate.\n\n(Under: CrPC Section 46)",

                            "Right to Privacy during Medical Exam" to "Medical examination of a woman victim should be done by a female doctor in complete privacy.\n\n(Under: CrPC Section 164A)",

                            "Protection from Domestic Violence" to "Provides legal protection against abuse (physical, verbal, emotional, etc.) within the home.\n\n(Under: Protection of Women from Domestic Violence Act, 2005)",

                            "Sexual Harassment at Workplace" to "Mandates protection and a complaint mechanism for women facing harassment at work.\n\n(Under: Sexual Harassment of Women at Workplace Act, 2013)",

                            "Protection from Stalking" to "Stalking, whether physical or online, is a punishable offense.\n\n(Under: Indian Penal Code, Section 354D)",

                            "Right against Voyeurism" to "Watching or recording a woman doing private acts without consent is a punishable crime.\n\n(Under: Indian Penal Code, Section 354C)",

                            "Right against Acid Attacks" to "Acid attacks are punishable, and victims are entitled to free medical treatment.\n\n(Under: Indian Penal Code, Sections 326A and 326B)",

                            "Right to Legal Aid" to "Women are entitled to free legal services if they can’t afford representation.\n\n(Under: Legal Services Authorities Act, 1987)",

                            "Right to Maternity Benefits" to "Includes 26 weeks of paid leave, job protection, and nursing breaks.\n\n(Under: Maternity Benefit (Amendment) Act, 2017)",

                            "Equal Pay for Equal Work" to "Women must receive the same pay as men for the same work.\n\n(Under: Equal Remuneration Act, 1976)",

                            "Live-in Relationship Protection" to "Women in live-in relationships are legally protected from abuse.\n\n(Under: Domestic Violence Act, 2005 - interpreted by courts)",

                            "Right to Confidentiality in Assault Cases" to "Victim’s identity must remain confidential in media and records.\n\n(Under: Indian Penal Code, Section 228A)",

                            "Right to Safe Abortion" to "Permits abortion up to 24 weeks under legal and medical guidance.\n\n(Under: Medical Termination of Pregnancy (Amendment) Act, 2021)",

                            "Nirbhaya Fund" to "A government fund supporting initiatives for women's safety and empowerment.\n\n(Under: Ministry of Women and Child Development Scheme, 2013)"
                        )


                        Column {
                            laws.forEach { (title, desc) ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EAFB)),
                                    border = BorderStroke(1.dp, Color(0xFF4B0082)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = title,
                                            style = AppTypography.titleSmall,
                                            color = Color(0xFF4B0082)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            style = AppTypography.bodyMedium,
                                            color = Color(0xFF1C1C1C)
                                        )
                                    }
                                }
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
        border = BorderStroke(1.dp, Color(0xFF4B0082)),
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
                    color = Color(0xFF4B0082)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF4B0082)
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
                color = Color(0xFF333333),
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFF4B0082)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                "$name: $number",
                style = AppTypography.bodyLarge,
                color = Color(0xFF1C1C1C)
            )
        }
    }
}

@Composable
fun SafetyTipLink(title: String, url: String) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFF4B0082)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(title, style = AppTypography.bodyLarge, color = Color(0xFF1C1C1C))
        }
    }
}
