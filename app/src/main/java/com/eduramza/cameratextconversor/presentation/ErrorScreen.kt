package com.eduramza.cameratextconversor.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion.Error
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLoggerImpl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(
    errorMessage: String,
    navigateBack: () -> Unit,
) {

    val analytics: FirebaseAnalyticsLogger = FirebaseAnalyticsLoggerImpl()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                analytics.trackSelectContent(
                                    id = Error.ID_BACK,
                                    itemName = Error.ITEM_NAME_BACK,
                                    contentType = ConstantsAnalytics.CONTENT_BUTTON,
                                    area = Error.AREA
                                )
                            }
                            navigateBack()
                        }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_description_back_button),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        SideEffect {
            scope.launch {
                analytics.trackScreenView(
                    screenName = Error.SCREEN_NAME,
                    area = Error.AREA
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.error_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    fontSize = 21.sp,
                    textAlign = TextAlign.Center
                )
            }
            ElevatedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onClick = {
                    scope.launch {
                        analytics.trackSelectContent(
                            id = Error.ID_TRY_AGAIN,
                            itemName = Error.ITEM_NAME_TRY_AGAIN,
                            contentType = ConstantsAnalytics.CONTENT_BUTTON,
                            area = Error.AREA
                        )
                    }
                    navigateBack()
                }
            ) {
                Text(text = stringResource(id = R.string.button_try_again))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewError() {
    ErrorScreen(
        errorMessage = "Ops, algo deu errado, tente novamente!"
    ) {

    }
}