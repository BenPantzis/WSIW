package com.bsp.wsiw.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.ui.theme.AppTheme

@Composable
fun LoginCallbackScreen(
    requestToken: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginCallbackViewModel = hiltViewModel<LoginCallbackViewModel, LoginCallbackViewModel.Factory>(
        creationCallback = { factory -> factory.create(requestToken) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginCallbackEvent.NavigateToProfile -> onSuccess()
            }
        }
    }

    val spacing = AppTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.content),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(spacing.lg))
            Text(
                text = stringResource(R.string.login_callback_signing_in),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (state.error != null) {
            Text(
                text = state.error!!.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.xl))
            Button(
                onClick = { viewModel.onAction(LoginCallbackAction.Retry) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.login_callback_try_again))
            }
            Spacer(Modifier.height(spacing.md))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.login_callback_cancel))
            }
        }
    }
}
