package com.example.vgcamera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vgcamera.R
import com.example.vgcamera.User
import com.example.vgcamera.ui.components.ModernConfirmDialog
import java.text.SimpleDateFormat
import java.util.*

data class MenuOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val color: Color,
    val description: String = ""
)

@Composable
fun ModernMenuScreen(
    user: User?,
    onStartCamera: () -> Unit,
    onSettings: () -> Unit,
    onGallery: () -> Unit,
    onLogout: () -> Unit,
    isLogoutDialogVisible: Boolean = false,
    onLogoutDismiss: () -> Unit = {},
    onRequestLogout: () -> Unit = {},
    currentLanguage: String = "en",
    onLanguageChange: (String) -> Unit = {}
) {
    var isLangMenuExpanded by remember { mutableStateOf(false) }
    
    // Localization Mapping
    val loginText = when(currentLanguage) {
        "vi" -> "Xin chào,"
        "cn" -> "你好,"
        else -> "Hello,"
    }
    val defaultUserText = when(currentLanguage) {
        "vi" -> "Quý khách"
        "cn" -> "贵宾"
        else -> "Guest"
    }
    val categoryText = when(currentLanguage) {
        "vi" -> "DANH MỤC CHỨC NĂNG"
        "cn" -> "功能目录"
        else -> "FUNCTION MENU"
    }
    
    val menuItems = when(currentLanguage) {
        "vi" -> listOf(
            Triple("Quay phim & Chụp ảnh", "Bắt đầu quay phim và chụp ảnh", Color(0xFFFF6D00)),
            Triple("Cài đặt hệ thống", "Cấu hình tham số thiết bị", Color(0xFF2979FF)),
            Triple("Đăng xuất", "Thoát tài khoản hiện tại", Color(0xFFFF1744))
        )
        "cn" -> listOf(
            Triple("拍照与录像", "开始拍摄照片和录制视频", Color(0xFFFF6D00)),
            Triple("系统设置", "配置设备参数", Color(0xFF2979FF)),
            Triple("退出账号", "退出当前帐号", Color(0xFFFF1744))
        )
        else -> listOf(
            Triple("Photo & Video", "Start capturing photos and videos", Color(0xFFFF6D00)),
            Triple("System Settings", "Configure device parameters", Color(0xFF2979FF)),
            Triple("Log Out", "Sign out of account", Color(0xFFFF1744))
        )
    }

    val options = listOf(
        MenuOption(menuItems[0].first, Icons.Default.CameraAlt, onStartCamera, menuItems[0].third, menuItems[0].second),
        MenuOption(menuItems[1].first, Icons.Default.Settings, onSettings, menuItems[1].third, menuItems[1].second),
        MenuOption(menuItems[2].first, Icons.Default.ExitToApp, onRequestLogout, menuItems[2].third, menuItems[2].second)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Image
        Image(
            painter = painterResource(id = R.drawable.bg4),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 2. Premium Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            // Header Section: Fully dedicated to Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loginText,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = user?.name ?: defaultUserText,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 30.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = categoryText,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            )

            // Action List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                options.forEach { option ->
                    MenuWideCard(option)
                }
            }
            
            // Footer Info & Language Dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                // Version in Center
                val context = androidx.compose.ui.platform.LocalContext.current
                val versionName = remember {
                    try {
                        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        packageInfo.versionName
                    } catch (e: Exception) {
                        "1.0"
                    }
                }
                
                Text(
                    text = "VG-CAMERA SYSTEM v$versionName",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Language Dropdown Anchor at Right (Pushed far right to edge)
                Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 20.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { isLangMenuExpanded = true }
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentFlag = when(currentLanguage) {
                                "vi" -> "VN"
                                "cn" -> "CN"
                                else -> "EN"
                            }
                            Text(text = currentFlag, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = isLangMenuExpanded,
                        onDismissRequest = { isLangMenuExpanded = false },
                        offset = androidx.compose.ui.unit.DpOffset(x = (0).dp, y = (4).dp),
                        modifier = Modifier.background(Color(0xFF1E1E1E))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tiếng Việt (VN)", color = Color.White, fontSize = 14.sp) },
                            onClick = { onLanguageChange("vi"); isLangMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("中文 (CN)", color = Color.White, fontSize = 14.sp) },
                            onClick = { onLanguageChange("cn"); isLangMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("English (EN)", color = Color.White, fontSize = 14.sp) },
                            onClick = { onLanguageChange("en"); isLangMenuExpanded = false }
                        )
                    }
                }
            }
        }

        // Logout Confirmation Dialog
        val logoutDialogTitle = when(currentLanguage) {
            "vi" -> "Xác nhận Đăng xuất"
            "cn" -> "确认注销"
            else -> "Confirm Logout"
        }
        val logoutDialogMessage = when(currentLanguage) {
            "vi" -> "Bạn có chắc chắn muốn đăng xuất tài khoản?"
            "cn" -> "您确定要退出当前账号吗？"
            else -> "Are you sure you want to sign out?"
        }
        val logoutConfirmBtn = when(currentLanguage) {
            "vi" -> "Đăng xuất"
            "cn" -> "登出"
            else -> "Log Out"
        }
        val logoutCancelBtn = when(currentLanguage) {
            "vi" -> "Hủy bỏ"
            "cn" -> "取消"
            else -> "Cancel"
        }

        ModernConfirmDialog(
            isVisible = isLogoutDialogVisible,
            title = logoutDialogTitle,
            message = logoutDialogMessage,
            confirmText = logoutConfirmBtn,
            cancelText = logoutCancelBtn,
            confirmColor = Color(0xFFFF1744),
            icon = Icons.Default.ExitToApp,
            onConfirm = {
                onLogout()
            },
            onDismiss = { onLogoutDismiss() }
        )
    }
}

@Composable
fun LanguageFlag(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MenuWideCard(option: MenuOption) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { option.onClick() }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background with Gradient
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(option.color, option.color.copy(alpha = 0.6f))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = option.description,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
