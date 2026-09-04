package com.example.nexvora.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutDeveloperCard(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showFullDialog by remember { mutableStateOf(false) }

  val openUrl = { url: String ->
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
  }

  val openWhatsApp = { phone: String ->
    val cleanNumber = if (phone.startsWith("0")) "88$phone" else phone
    val url = "https://wa.me/$cleanNumber"
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      context.startActivity(intent)
    } catch (e: Exception) {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipboard.setPrimaryClip(ClipData.newPlainText("WhatsApp Number", phone))
      Toast.makeText(context, "Copied WhatsApp $phone to clipboard", Toast.LENGTH_SHORT).show()
    }
  }

  val copyText = { label: String, text: String ->
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = modifier
      .fillMaxWidth()
      .testTag("about_developer_card")
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Code,
              contentDescription = "Developer",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "About Developer",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Prince AR Abdur Rahman",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(100.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.clickable { showFullDialog = true }
        ) {
          Text(
            text = "Full Details",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Developer Bio
      Text(
        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Contact & Social Links
      Text(
        text = "CONTACT & SOCIAL",
        style = MaterialTheme.typography.labelSmall.copy(
          letterSpacing = 1.2.sp,
          fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // WhatsApp 1
      ContactRow(
        icon = Icons.Filled.Chat,
        title = "WhatsApp",
        value = "01707424006",
        actionLabel = "Chat",
        onAction = { openWhatsApp("01707424006") },
        onCopy = { copyText("WhatsApp 1", "01707424006") }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // WhatsApp 2
      ContactRow(
        icon = Icons.Filled.Chat,
        title = "WhatsApp",
        value = "01796951709",
        actionLabel = "Chat",
        onAction = { openWhatsApp("01796951709") },
        onCopy = { copyText("WhatsApp 2", "01796951709") }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Facebook
      ContactRow(
        icon = Icons.Filled.Share,
        title = "Facebook",
        value = "Prince AR Abdur Rahman",
        actionLabel = "Open",
        onAction = { openUrl("https://www.facebook.com/share/1BNn32qoJo/") },
        onCopy = { copyText("Facebook Link", "https://www.facebook.com/share/1BNn32qoJo/") }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Instagram
      ContactRow(
        icon = Icons.Filled.CameraAlt,
        title = "Instagram",
        value = "@ur___abdur____rahman__2008",
        actionLabel = "Open",
        onAction = { openUrl("https://www.instagram.com/ur___abdur____rahman__2008") },
        onCopy = { copyText("Instagram Link", "https://www.instagram.com/ur___abdur____rahman__2008") }
      )

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
      )

      // About Company Section
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Business,
            contentDescription = "Company",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "About Company: NexVora Lab's Ofc",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Mission Box
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "MISSION",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
      )

      // Technical Information
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Technical Information",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Credits Box
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "CREDITS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Developed by Prince AR Abdur Rahman\nPublished by NexVora Lab's Ofc",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  // Full Details Dialog
  if (showFullDialog) {
    AlertDialog(
      onDismissRequest = { showFullDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("About NexVora Alarm", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "About Developer\nPrince AR Abdur Rahman",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
            style = MaterialTheme.typography.bodySmall
          )

          Text(
            text = "Contact: WhatsApp: 01707424006 • WhatsApp: 01796951709\nFacebook: https://www.facebook.com/share/1BNn32qoJo/\nInstagram: @ur___abdur____rahman__2008",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider()

          Text(
            text = "About Company\nNexVora Lab's Ofc",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.\n\nMission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
            style = MaterialTheme.typography.bodySmall
          )

          HorizontalDivider()

          Text(
            text = "Technical Information: Version 1.0.0\n\nCredits:\nDeveloped by Prince AR Abdur Rahman\nPublished by NexVora Lab's Ofc\n© 2026 NexVora Lab's Ofc. All Rights Reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(onClick = { showFullDialog = false }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
private fun ContactRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  value: String,
  actionLabel: String,
  onAction: () -> Unit,
  onCopy: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.ContentCopy,
            contentDescription = "Copy",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onAction() }
        ) {
          Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }
  }
}
