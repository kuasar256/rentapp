package com.example.rentapp.ui.screens.tenant

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.ui.screens.property.EmptyState
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(
    viewModel: TenantViewModel,
    onTenantClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tenants by viewModel.searchResults.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inquilinos", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Agregar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = Primary) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Inquilino", tint = OnPrimaryFixed)
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Stats header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Activos", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text("$activeCount", style = MaterialTheme.typography.headlineSmall, color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text("${tenants.size}", style = MaterialTheme.typography.headlineSmall, color = OnBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search bar
            NeonTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "Buscar inquilino...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (tenants.isEmpty()) {
                EmptyState(message = "No se encontraron inquilinos.\nToca + para agregar uno.", icon = Icons.Default.People)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tenants) { tenant ->
                        TenantCard(tenant = tenant, onClick = { onTenantClick(tenant.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun TenantCard(tenant: Tenant, onClick: () -> Unit) {
    val initials = "${tenant.firstName.firstOrNull() ?: ""}${tenant.lastName.firstOrNull() ?: ""}"
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(Primary.copy(alpha = 0.2f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials.uppercase(), color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("${tenant.firstName} ${tenant.lastName}", color = OnBackground,
                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(tenant.email, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(tenant.phone, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                color = if (tenant.status == "ACTIVE") Primary.copy(0.15f) else OutlineVariant.copy(0.3f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (tenant.status == "ACTIVE") "Activo" else "Inactivo",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = if (tenant.status == "ACTIVE") Primary else OnSurfaceVariant,
                    fontSize = 11.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
