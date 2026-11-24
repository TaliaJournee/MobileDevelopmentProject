package edu.ap.dakaniktochniwegsmijten.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.dakaniktochniwegsmijten.R
import edu.ap.dakaniktochniwegsmijten.data.user.UserRole
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun RegisterScreen(
    registrationState: RegistrationUiState,
    onRegister: (RegisterUiState) -> Unit,
    modifier: Modifier = Modifier,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val uiState by registerViewModel.uiState.collectAsStateWithLifecycle()
    val showNameError = uiState.name.isNotEmpty() && !uiState.isNameValid
    val showEmailError = uiState.email.isNotEmpty() && !uiState.isEmailValid
    val showPasswordError = uiState.password.isNotEmpty() && !uiState.isPasswordValid
    val focusManager = LocalFocusManager.current
    val canEdit = !registrationState.isSubmitting
    val canSubmit = canEdit &&
            uiState.isNameValid &&
            uiState.isEmailValid &&
            uiState.isPasswordValid &&
            uiState.role != null

    BackHandler(enabled = registrationState.isSubmitting) {
        // Keep the form open while account creation is in progress.
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = registerViewModel::updateName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.register_name)) },
            singleLine = true,
            enabled = canEdit,
            isError = showNameError,
            supportingText = if (showNameError) {
                { Text(stringResource(R.string.register_error_name)) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = registerViewModel::updateEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_email)) },
            singleLine = true,
            enabled = canEdit,
            isError = showEmailError,
            supportingText = if (showEmailError) {
                { Text(stringResource(R.string.login_error_email_invalid)) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = registerViewModel::updatePassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_password)) },
            singleLine = true,
            enabled = canEdit,
            isError = showPasswordError,
            supportingText = if (showPasswordError) {
                { Text(stringResource(R.string.register_error_password_short)) }
            } else {
                null
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Text(
            text = stringResource(R.string.register_role),
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            UserRole.entries.forEach { role ->
                val selected = uiState.role == role
                val labelRes = when (role) {
                    UserRole.CUSTOMER -> R.string.register_role_customer
                    UserRole.MERCHANT -> R.string.register_role_merchant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            enabled = canEdit,
                            role = Role.RadioButton,
                            onClick = { registerViewModel.selectRole(role) }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        enabled = canEdit
                    )
                    Text(stringResource(labelRes))
                }
            }
        }
        registrationState.errorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                onRegister(uiState)
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (registrationState.isSubmitting) {
                        R.string.register_submitting
                    } else {
                        R.string.register_submit
                    }
                )
            )
        }

        if (registrationState.isSubmitting) {
            CircularProgressIndicator()
        }
    }
}