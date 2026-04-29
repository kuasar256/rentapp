package com.example.rentapp.ui.screens.tenant

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.ui.screens.property.EmptyState
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.viewmodel.TenantViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(
    viewModel: TenantViewModel,
    navController: NavHostController,
    onTenantClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val tenants by viewModel.tenantDisplayList.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var tenantToDelete by remember { mutableStateOf<Tenant?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceContainer,
                    contentColor = OnBackground,
                    actionColor = Primary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.tenants_list_title), 
                        color = OnBackground, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cancel), tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_tenant), tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            RentAppBottomBar(navController = navController)
        },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Stats Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.active_filter), 
                    value = "$activeCount", 
                    icon = Icons.Default.VerifiedUser, 
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "TOTAL", 
                    value = "${tenants.size}", 
                    icon = Icons.Default.Groups, 
                    color = Tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search bar
            NeonTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = stringResource(R.string.search_hint),
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { viewModel.setStatusFilter("ALL") },
                    label = { Text(stringResource(R.string.all_filter)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = statusFilter == "ACTIVE",
                    onClick = { viewModel.setStatusFilter("ACTIVE") },
                    label = { Text(stringResource(R.string.active_filter)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = statusFilter == "PAID",
                    onClick = { viewModel.setStatusFilter("PAID") },
                    label = { Text(stringResource(R.string.status_paid)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = statusFilter == "DELAYED",
                    onClick = { viewModel.setStatusFilter("DELAYED") },
                    label = { Text(stringResource(R.string.status_delayed)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Error,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    leadingIcon = {
                        if (statusFilter == "DELAYED") {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                FilterChip(
                    selected = statusFilter == "INACTIVE",
                    onClick = { viewModel.setStatusFilter("INACTIVE") },
                    label = { Text(stringResource(R.string.inactive_filter)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OnSurfaceVariant,
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (tenants.isEmpty()) {
                EmptyState(
                    message = stringResource(R.string.no_tenants_msg),
                    icon = Icons.Default.PeopleOutline
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tenants, key = { it.tenant.id }) { displayModel ->
                        SwipeToDeleteTenantCard(
                            displayModel = displayModel,
                            onClick = { onTenantClick(displayModel.tenant.id) },
                            onDeleteRequested = { tenantToDelete = displayModel.tenant }
                        )
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }

    // ── Confirm deletion dialog ──────────────────────────────────────────────
    tenantToDelete?.let { tenant ->
        AlertDialog(
            onDismissRequest = { tenantToDelete = null },
            containerColor = SurfaceContainer,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    stringResource(R.string.delete_tenant_title),
                    color = OnBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_tenant_confirm, "${tenant.firstName} ${tenant.lastName}"),
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deletedTenant = tenant
                        tenantToDelete = null
                        viewModel.deleteTenant(deletedTenant)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.tenant_deleted_msg, "${deletedTenant.firstName} ${deletedTenant.lastName}"),
                                actionLabel = context.getString(R.string.undo),
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.insertTenant(deletedTenant.copy(id = 0L))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { tenantToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = Primary)
                }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SurfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = OnBackground, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SwipeToDeleteTenantCard(
    displayModel: com.example.rentapp.viewmodel.TenantDisplayModel,
    onClick: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequested()
            }
            false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            DeleteBackground(dismissState.targetValue)
        }
    ) {
        TenantCard(displayModel = displayModel, onClick = onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(targetValue: SwipeToDismissBoxValue) {
    val active = targetValue == SwipeToDismissBoxValue.EndToStart
    val bgColor = if (active) Error else Error.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier.padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.delete),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun TenantCard(displayModel: com.example.rentapp.viewmodel.TenantDisplayModel, onClick: () -> Unit) {
    val tenant = displayModel.tenant
    val context = LocalContext.current
    val initials = "${tenant.firstName.firstOrNull() ?: ""}${tenant.lastName.firstOrNull() ?: ""}"

    val statusColor = when (displayModel.paymentStatus) {
        "PAID" -> Color(0xFF4CAF50) // Green
        "DELAYED" -> Color(0xFFF44336) // Red
        "PENDING" -> Color(0xFFFFC107) // Amber
        else -> OnSurfaceVariant
    }

    val statusText = when (displayModel.paymentStatus) {
        "PAID" -> stringResource(R.string.status_paid)
        "DELAYED" -> stringResource(R.string.status_delayed)
        "PENDING" -> stringResource(R.string.status_pending)
        else -> "N/A"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f))
            ) {
                if (tenant.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(tenant.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Tenant Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            initials.uppercase(), 
                            color = Primary, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 20.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    "${tenant.firstName} ${tenant.lastName}",
                    color = OnBackground,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tenant.email, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tenant.phone, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Payment Status Chip
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        statusText.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Active/Inactive Status Chip
                Surface(
                    color = if (tenant.status == "ACTIVE") Primary.copy(0.1f) else OnSurfaceVariant.copy(0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (tenant.status == "ACTIVE") stringResource(R.string.active_filter) else stringResource(R.string.inactive_filter),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (tenant.status == "ACTIVE") Primary else OnSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                IconButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${tenant.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(32.dp).background(Tertiary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

