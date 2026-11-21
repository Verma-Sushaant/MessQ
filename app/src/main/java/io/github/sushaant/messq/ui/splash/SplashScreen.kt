package io.github.sushaant.messq.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sushaant.messq.data.local.datastore.UserPreferencesManager
import io.github.sushaant.messq.ui.theme.CorsivaBoldItalic
import io.github.sushaant.messq.ui.theme.MessQTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    preferencesManager: UserPreferencesManager,
    onNavigateToAuth: () -> Unit,
    onNavigateToStudent: () -> Unit,
    onNavigateToStaff: () -> Unit
) {
    LaunchedEffect(Unit) {
        val token = preferencesManager.userToken.first()
        val role = preferencesManager.userRole.first()
        delay(700L)

        if (!token.isNullOrBlank()) {
            if (role == "staff" || role == "admin") {
                onNavigateToStaff()
            } else {
                onNavigateToStudent()
            }
        } else {
            onNavigateToAuth()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("MessQ")
                    }
                },
                style = CorsivaBoldItalic,
                fontSize = 52.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Campus Dining & Crowd Telemetry",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun ShowSplash() {
    MessQTheme {
        SplashScreen(
            preferencesManager = UserPreferencesManager(
                context = LocalContext.current
            ),
            onNavigateToAuth = {},
            onNavigateToStudent = {},
            onNavigateToStaff = {}
        )
    }
}