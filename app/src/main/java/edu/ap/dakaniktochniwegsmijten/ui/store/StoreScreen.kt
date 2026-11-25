package edu.ap.dakaniktochniwegsmijten.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.dakaniktochniwegsmijten.R
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import edu.ap.dakaniktochniwegsmijten.ui.map.MapScreen
import edu.ap.dakaniktochniwegsmijten.ui.map.MapViewModel
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect

@Composable
fun StoreScreen(
    ownerId: String,
    modifier: Modifier = Modifier,
    storeViewModel: StoreViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()
    val mapUiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    var isChoosingLocation by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isChoosingLocation) {
        isChoosingLocation = false
    }

    val latitude = mapUiState.selectedLatitude
    val longitude = mapUiState.selectedLongitude

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            storeViewModel.onStorePinSelected(latitude, longitude)
        }
    }

    if (isChoosingLocation) {
        Column(modifier = modifier.fillMaxSize()) {
            TextButton(onClick = { isChoosingLocation = false }) {
                Text(stringResource(R.string.store_back_to_form))
            }

            MapScreen(
                modifier = Modifier.weight(1f),
                mapViewModel = mapViewModel
            )
        }
        return
    }

    val canEdit = !uiState.isSaving && !uiState.isRegistered

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.merchant_home_title),
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = storeViewModel::updateName,
            label = { Text(stringResource(R.string.store_name_label)) },
            singleLine = true,
            enabled = canEdit,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.category,
            onValueChange = storeViewModel::updateCategory,
            label = { Text(stringResource(R.string.store_category_label)) },
            singleLine = true,
            enabled = canEdit,
            modifier = Modifier.fillMaxWidth()
        )

        if (latitude != null && longitude != null) {
            Text(
                text = stringResource(
                    R.string.store_selected_coordinates,
                    latitude,
                    longitude
                )
            )
        }

        Button(
            onClick = { isChoosingLocation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.store_choose_location))
        }

        if (latitude != null && longitude != null) {
            if (uiState.isLookingUpAddress) {
                Text(stringResource(R.string.store_address_loading))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.addressLookupFailed) {
                Text(stringResource(R.string.store_address_lookup_failed))
            }

            val address = uiState.address
            val canEditAddress = canEdit && !uiState.isLookingUpAddress

            OutlinedTextField(
                value = address?.streetAndNumber.orEmpty(),
                onValueChange = { value ->
                    storeViewModel.updateAddress { it.copy(streetAndNumber = value) }
                },
                label = { Text(stringResource(R.string.store_street_and_number_label)) },
                singleLine = true,
                enabled = canEditAddress,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address?.postalCode.orEmpty(),
                onValueChange = { value ->
                    storeViewModel.updateAddress { it.copy(postalCode = value) }
                },
                label = { Text(stringResource(R.string.store_postal_code_label)) },
                singleLine = true,
                enabled = canEditAddress,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address?.city.orEmpty(),
                onValueChange = { value ->
                    storeViewModel.updateAddress { it.copy(city = value) }
                },
                label = { Text(stringResource(R.string.store_city_label)) },
                singleLine = true,
                enabled = canEditAddress,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.isRegistered) {
            Text(stringResource(R.string.store_registered))
        } else {
            Button(
                onClick = {
                    storeViewModel.registerStore(ownerId, latitude, longitude)
                },
                enabled = canEdit && !uiState.isLookingUpAddress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (uiState.isSaving) R.string.store_saving
                        else R.string.store_register
                    )
                )
            }
        }

        if (uiState.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        uiState.errorRes?.let { message ->
            Text(
                text = stringResource(message),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}