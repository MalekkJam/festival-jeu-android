package com.example.festivaldujeu.ui.screens.jeu.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.overflow.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.festivaldujeu.domain.model.Jeu

/**
 * Card réutilisable pour afficher un jeu
 * Pattern : Composable pur, aucune logique métier
 * Responsabilités : affichage uniquement
 */
@Composable
fun JeuCard(
    jeu: Jeu,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    showActions: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Titre du jeu
            Text(
                text = jeu.libelleJeu,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Auteur
            jeu.auteurJeu?.let {
                Text(
                    text = "Auteur: $it",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Thème
            jeu.theme?.let {
                Text(
                    text = "Thème: $it",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Joueurs
            if (jeu.nbMinJoueurJeu != null || jeu.nbMaxJoueurJeu != null) {
                Text(
                    text = "Joueurs: ${jeu.nbMinJoueurJeu ?: "?"}-${jeu.nbMaxJoueurJeu ?: "?"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Âge minimum
            jeu.agemini?.let {
                Text(
                    text = "Âge: à partir de $it ans",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Actions en bas (Edit/Delete)
            if (showActions) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Modifier",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
