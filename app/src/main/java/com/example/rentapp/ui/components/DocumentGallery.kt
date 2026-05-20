package com.example.rentapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rentapp.ui.theme.Primary
import com.example.rentapp.ui.theme.SurfaceContainer
import com.example.rentapp.ui.theme.SurfaceContainerHigh

@Composable
fun DocumentGallery(
    uris: List<String>,
    onAddClick: (() -> Unit)? = null,
    onImageClick: (Int) -> Unit = {}
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(uris) { index, uri ->
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { onImageClick(index) },
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(SurfaceContainer)
                )
            }
        }
        
        if (onAddClick != null) {
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh)
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Primary)
                        Text("Añadir", style = MaterialTheme.typography.labelSmall, color = Primary)
                    }
                }
            }
        }
        
        if (uris.isEmpty() && onAddClick == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(SurfaceContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.width(8.dp))
                        Text("Sin documentos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
