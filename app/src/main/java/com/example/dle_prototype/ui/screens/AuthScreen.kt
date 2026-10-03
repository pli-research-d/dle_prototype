package com.example.dle_prototype.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.User
import com.example.dle_prototype.ui.components.AppTrendyLogo
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.launch

/**
 * Clean, single-purpose AuthScreen conforming to user requirements:
 * - Logo + one-line subtitle
 * - Segmented toggle: Sign In | Sign Up
 * - Username + Password with show/hide toggle
 * - Inline validation under the respective fields
 * - Full-width bottom primary button: "Sign In" / "Create Account"
 */
@Composable
fun AuthScreen(
    dbHelper: DatabaseHelper,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Sign Up
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var showOnboarding by remember { mutableStateOf(false) }
    var pendingUser by remember { mutableStateOf<User?>(null) }
    var selectedTopics by remember { mutableStateOf(setOf("HTML", "CSS", "JavaScript")) }
    var selectedSkillLevel by remember { mutableStateOf("Surprise me") }
    var selectedDailyGoal by remember { mutableIntStateOf(10) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Logo
                AppTrendyLogo(
                    size = 80.dp,
                    animatedGlow = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PLi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // One-line subtitle
                Text(
                    text = "Personal Learning intelligence with On-Device private AI for adaptive learning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Segmented Toggle: Sign In | Sign Up
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("auth_segmented_toggle"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .clickable {
                                    selectedTab = 0
                                    usernameError = null
                                    passwordError = null
                                    generalError = null
                                    successNotice = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .clickable {
                                    selectedTab = 1
                                    usernameError = null
                                    passwordError = null
                                    generalError = null
                                    successNotice = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Username Field with inline validation
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = null
                        generalError = null
                    },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = usernameError != null,
                    supportingText = {
                        usernameError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_username")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password Field with show/hide toggle and inline validation
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        generalError = null
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("toggle_password")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = {
                        passwordError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_password")
                )

                // Inline General Error Notification
                AnimatedVisibility(
                    visible = generalError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    generalError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }

                // Inline Success Notification
                AnimatedVisibility(
                    visible = successNotice != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    successNotice?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            // Primary Button (full width, bottom): "Sign In" / "Create Account"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        usernameError = null
                        passwordError = null
                        generalError = null
                        successNotice = null

                        var hasValidationError = false
                        if (username.trim().isEmpty()) {
                            usernameError = "Username is required"
                            hasValidationError = true
                        } else if (username.trim().length < 3) {
                            usernameError = "Username must be at least 3 characters"
                            hasValidationError = true
                        }

                        if (password.isEmpty()) {
                            passwordError = "Password is required"
                            hasValidationError = true
                        } else if (password.length < 4) {
                            passwordError = "Password must be at least 4 characters"
                            hasValidationError = true
                        }

                        if (hasValidationError) return@Button

                        isLoading = true
                        coroutineScope.launch {
                            if (selectedTab == 0) {
                                // Sign In
                                val result = dbHelper.loginUser(username.trim(), password)
                                isLoading = false
                                result.fold(
                                    onSuccess = { user -> onLoginSuccess(user) },
                                    onFailure = { err -> generalError = err.message ?: "Invalid username or password" }
                                )
                            } else {
                                // Sign Up -> trigger 3-tap onboarding
                                val result = dbHelper.registerUser(username.trim(), password)
                                isLoading = false
                                result.fold(
                                    onSuccess = { user ->
                                        pendingUser = user
                                        showOnboarding = true
                                    },
                                    onFailure = { err -> generalError = err.message ?: "Registration failed. Try a different username." }
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (selectedTab == 0) "Sign In" else "Create Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // First-run onboarding (3 taps max, then straight into the feed)
        if (showOnboarding && pendingUser != null) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = Color(0xFF0F172A),
                title = {
                    Column {
                        Text("🚀 Quick Calibration", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                        Text("3 taps to personalize your Feed", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Pick topics
                        Text("1. PICK TOPICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        val allTopics = listOf("HTML", "CSS", "JavaScript", "PHP", "MySQL", "Python")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                allTopics.take(3).forEach { topic ->
                                    val isSel = selectedTopics.contains(topic)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) CyanAccent.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, if (isSel) CyanAccent else Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            selectedTopics = if (isSel) {
                                                if (selectedTopics.size > 1) selectedTopics - topic else selectedTopics
                                            } else selectedTopics + topic
                                        }
                                    ) {
                                        Text(
                                            text = topic,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CyanAccent else Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                allTopics.drop(3).forEach { topic ->
                                    val isSel = selectedTopics.contains(topic)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) CyanAccent.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, if (isSel) CyanAccent else Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            selectedTopics = if (isSel) {
                                                if (selectedTopics.size > 1) selectedTopics - topic else selectedTopics
                                            } else selectedTopics + topic
                                        }
                                    ) {
                                        Text(
                                            text = topic,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CyanAccent else Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Pick skill level
                        Text("2. SKILL LEVEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        val levels = listOf("Beginner", "Intermediate", "Advanced", "Surprise me")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            levels.forEach { lvl ->
                                val isSel = selectedSkillLevel == lvl
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(0xFFA855F7).copy(alpha = 0.25f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSel) Color(0xFFA855F7) else Color(0xFF334155)),
                                    modifier = Modifier.weight(1f).clickable { selectedSkillLevel = lvl }
                                ) {
                                    Text(
                                        text = if (lvl == "Surprise me") "DDA ⚡" else lvl.take(3),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color(0xFFC084FC) else Color(0xFFCBD5E1),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // 3. Pick daily goal
                        Text("3. DAILY GOAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        val goals = listOf(Triple("Chill", 5, "5 q"), Triple("Regular", 10, "10 q"), Triple("Intense", 20, "20 q"))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            goals.forEach { (label, count, sub) ->
                                val isSel = selectedDailyGoal == count
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) EmeraldSuccess.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSel) EmeraldSuccess else Color(0xFF334155)),
                                    modifier = Modifier.weight(1f).clickable { selectedDailyGoal = count }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) EmeraldSuccess else Color.White)
                                        Text(sub, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val user = pendingUser ?: return@Button
                            coroutineScope.launch {
                                dbHelper.saveOnboardingPreferences(
                                    username = user.username,
                                    topics = selectedTopics.toList(),
                                    skillLevel = selectedSkillLevel,
                                    dailyGoalQuestions = selectedDailyGoal
                                )
                                showOnboarding = false
                                onLoginSuccess(user)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_launch_feed_button")
                    ) {
                        Text("Launch Feed 🚀", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
