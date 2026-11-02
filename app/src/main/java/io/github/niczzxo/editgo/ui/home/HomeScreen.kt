package io.github.niczzxo.editgo.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.niczzxo.editgo.R
import io.github.niczzxo.editgo.domain.Project

@Composable
fun HomeScreen(
    projects: List<Project>,
    hasMediaPermission: Boolean,
    onGrantPermission: () -> Unit,
    onNewProject: () -> Unit,
    onOpenProject: (Project) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_editgo_logo),
                contentDescription = "EditGo logo",
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = "EditGo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fast video editor for short video",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!hasMediaPermission) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("EditGo needs access to your videos to import them.")
                    TextButton(onClick = onGrantPermission) {
                        Text("Grant access")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onNewProject,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("New Project", modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Recent projects", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (projects.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    Icons.Filled.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No projects yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Start a new project to edit your first video.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects, key = { it.id }) { project ->
                    Card(
                        onClick = { onOpenProject(project) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(project.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Tap to open",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
