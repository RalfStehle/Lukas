package de.project.lukas.ui.cards

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.project.lukas.R

/**
 * Shared card frame matching the original layout: a title row (type label + device name), an
 * optional battery/message row, and a body of controls. The context menu opens on long-press,
 * just like the original `setOnCreateContextMenuListener`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceCardFrame(
    typeLabel: String,
    name: String,
    battery: Int?,
    message: String?,
    menu: @Composable (dismiss: () -> Unit) -> Unit,
    body: @Composable ColumnScope.() -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .combinedClickable(onClick = {}, onLongClick = { menuOpen = true }),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1B1B1B),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = typeLabel,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(2f),
                    )
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu_header))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            menu { menuOpen = false }
                        }
                    }
                }

                if (battery != null) {
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text("Battery", modifier = Modifier.weight(1f))
                        Row(Modifier.weight(2f)) {
                            Text("$battery %")
                            Spacer(Modifier.weight(1f))
                            if (!message.isNullOrEmpty()) {
                                Text(message, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                body()
            }
        }
    }
}

/** A filled, coloured control button as used on the cards. */
@Composable
fun RowScope.CardButton(
    onClick: () -> Unit,
    container: Color,
    modifier: Modifier = Modifier,
    weight: Float = 2f,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.weight(weight),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        content = content,
    )
}

/** A control-button row with the card's standard spacing. */
@Composable
fun CardButtonRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun CardIcon(@DrawableRes res: Int, contentDescription: String?) {
    Icon(
        painter = painterResource(res),
        contentDescription = contentDescription,
        modifier = Modifier.size(20.dp),
    )
}
