package edu.ap.dakaniktochniwegsmijten.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.ap.dakaniktochniwegsmijten.R

@Composable
fun SessionStatusScreen(
    uiState: SessionUiState,
    registrationState: RegistrationUiState,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = uiState.isLoading ||
            uiState.isProfileLoading ||
            registrationState.isSubmitting
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()

            Text(
                text = stringResource(
                    when {
                        registrationState.isSubmitting -> R.string.register_submitting
                        uiState.isLoading -> R.string.session_loading
                        else -> R.string.session_profile_loading
                    }
                ),
                textAlign = TextAlign.Center
            )
        } else {
            val errorRes = if (
                registrationState.errorRes == R.string.register_error_profile_save
            ) {
                R.string.register_error_profile_save
            } else {
                uiState.profileErrorRes
            }

            Text(
                text = stringResource(
                    errorRes ?: R.string.session_profile_missing
                ),
                color = if (errorRes != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )

            Button(onClick = onRetry) {
                Text(stringResource(R.string.session_retry))
            }
        }

        if (uiState.userId != null) {
            TextButton(
                onClick = onSignOut,
                enabled = !registrationState.isSubmitting
            ) {
                Text(stringResource(R.string.sign_out))
            }
        }
    }
}