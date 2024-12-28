package com.example.mycalender.calender

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mycalender.data.Event

@Composable
fun EventDetailDialog(
    event: Event,
    onClose: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(text = "行程詳情")
        },
        text = {
            Column {
                Text(text = "名稱: ${event.title}", style = MaterialTheme.typography.bodyLarge)
                if (!event.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "備註: ${event.note}", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "日期: ${event.date}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "時間: ${event.startTime} - ${event.endTime}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Row {

                TextButton(onClick = onEdit) {
                    Text(text = "編輯")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onClose) {
                    Text(text = "關閉")
                }

            }
        }
    )
}
