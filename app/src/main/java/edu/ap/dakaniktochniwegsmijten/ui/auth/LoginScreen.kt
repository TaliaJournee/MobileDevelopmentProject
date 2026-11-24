package edu.ap.dakaniktochniwegsmijten.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.dakaniktochniwegsmijten.R
import androidx.compose.material3.TextButton

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val canEdit = !uiState.isLoading && !uiState.isSignedIn

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = loginViewModel::updateEmail,
            modifier = Modifier.fillMaxWidth(),
            enabled = canEdit,
            label = {
                Text(stringResource(R.string.login_email))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = loginViewModel::updatePassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = canEdit,
            label = {
                Text(stringResource(R.string.login_password))
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    loginViewModel.signIn()
                }
            )
        )

        uiState.errorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                loginViewModel.signIn()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canEdit
        ) {
            Text(
                text = stringResource(
                    if (uiState.isLoading) {
                        R.string.login_loading
                    } else {
                        R.string.login_submit
                    }
                )
            )
        }

        TextButton(
            onClick = onRegisterClick,
            enabled = canEdit
        ) {
            Text(stringResource(R.string.register_title))
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}