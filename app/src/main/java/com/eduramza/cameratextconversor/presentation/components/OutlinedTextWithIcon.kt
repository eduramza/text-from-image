package com.eduramza.cameratextconversor.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduramza.cameratextconversor.R

@Composable
fun OutlinedTextFieldWithIconButton(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    onClickIcon: () -> Unit
) {
    Box{
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = modifier
        )
        IconButton(
            onClick = { onClickIcon() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 8.dp)
        ) {
            icon()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOutlined() {
    var value = ""
    OutlinedTextFieldWithIconButton(value = value,
        onValueChange = { value = it  },
        label = { Text(text = stringResource(id = R.string.label_analyzed_text_field)) },
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .defaultMinSize(minHeight = 500.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy Content",
            )
        },
        onClickIcon = {})
}