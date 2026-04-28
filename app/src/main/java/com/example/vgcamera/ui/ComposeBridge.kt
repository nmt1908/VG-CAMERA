package com.example.vgcamera.ui

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.example.vgcamera.User
import com.example.vgcamera.ui.components.ModernConfirmDialog
import com.example.vgcamera.ui.components.ModernUserInfoPanel
import com.example.vgcamera.ui.components.ScanningOverlay
import com.example.vgcamera.ui.screens.ModernMenuScreen
import com.example.vgcamera.ui.theme.VGCameraTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color

object ComposeBridge {
    @JvmStatic
    fun setMainOverlayContent(
        view: ComposeView,
        user: User?,
        isPanelVisible: Boolean,
        uiState: Int,
        frozenBitmap: Bitmap?,
        faceRect: Rect?,
        meshPoints: FloatArray?,
        imageWidth: Int,
        imageHeight: Int,
        rotation: Int,
        isExitDialogVisible: Boolean = false,
        exitTitle: String = "Xác nhận Thoát",
        exitMsg: String = "Bạn có chắc chắn muốn thoát không?",
        exitConfirm: String = "Thoát",
        exitCancel: String = "Hủy bỏ",
        onExitConfirm: Runnable? = null,
        onExitDismiss: Runnable? = null
    ) {
        view.setContent {
            VGCameraTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    ScanningOverlay(
                        uiState = uiState,
                        frozenBitmap = frozenBitmap,
                        faceRect = faceRect,
                        meshPoints = meshPoints,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        rotation = rotation
                    )
                    ModernUserInfoPanel(user = user, isVisible = isPanelVisible, uiState = uiState)

                    // Unified Modern Exit Dialog
                    ModernConfirmDialog(
                        isVisible = isExitDialogVisible,
                        title = exitTitle,
                        message = exitMsg,
                        confirmText = exitConfirm,
                        cancelText = exitCancel,
                        confirmColor = Color(0xFFFF1744),
                        icon = Icons.Default.Warning,
                        onConfirm = { onExitConfirm?.run() },
                        onDismiss = { onExitDismiss?.run() }
                    )
                }
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun setMenuContent(
        view: ComposeView,
        user: User?,
        onStartCamera: Runnable,
        onSettings: Runnable,
        onGallery: Runnable,
        onLogout: Runnable,
        isLogoutDialogVisible: Boolean = false,
        onLogoutDismiss: Runnable? = null,
        onRequestLogout: Runnable? = null,
        currentLanguage: String = "en",
        onLanguageChange: (String) -> Unit = {}
    ) {
        view.setContent {
            VGCameraTheme {
                ModernMenuScreen(
                    user = user,
                    onStartCamera = { onStartCamera.run() },
                    onSettings = { onSettings.run() },
                    onGallery = { onGallery.run() },
                    onLogout = { onLogout.run() },
                    isLogoutDialogVisible = isLogoutDialogVisible,
                    onLogoutDismiss = { onLogoutDismiss?.run() },
                    onRequestLogout = { onRequestLogout?.run() },
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun setAlbumOverlayContent(
        view: ComposeView,
        isUploadDialogVisible: Boolean,
        isDeleteDialogVisible: Boolean,
        isMessageDialogVisible: Boolean = false,
        messageTitle: String = "",
        messageText: String = "",
        uploadTitle: String = "Xác nhận Tải lên",
        uploadMsg: String = "Bạn có chắc chắn muốn tải lên không?",
        uploadConfirm: String = "Tải lên",
        uploadCancel: String = "Hủy bỏ",
        deleteTitle: String = "Xác nhận Xóa",
        deleteMsg: String = "Các tệp tin bị xóa sẽ không thể khôi phục.",
        deleteConfirm: String = "Xóa bỏ",
        deleteCancel: String = "Hủy bỏ",
        onUploadConfirm: Runnable,
        onDeleteConfirm: Runnable,
        onMessageConfirm: Runnable,
        onDismiss: Runnable
    ) {
        view.setContent {
            VGCameraTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Upload Confirmation (White Theme)
                    ModernConfirmDialog(
                        isVisible = isUploadDialogVisible,
                        title = uploadTitle,
                        message = uploadMsg,
                        confirmText = uploadConfirm,
                        cancelText = uploadCancel,
                        confirmColor = Color(0xFF00E5FF),
                        isLightTheme = true,
                        icon = Icons.Default.CloudUpload,
                        onConfirm = { onUploadConfirm.run() },
                        onDismiss = { onDismiss.run() }
                    )

                    // Delete Confirmation (Dark Theme)
                    ModernConfirmDialog(
                        isVisible = isDeleteDialogVisible,
                        title = deleteTitle,
                        message = deleteMsg,
                        confirmText = deleteConfirm,
                        cancelText = deleteCancel,
                        confirmColor = Color(0xFFFF1744),
                        icon = Icons.Default.DeleteForever,
                        onConfirm = { onDeleteConfirm.run() },
                        onDismiss = { onDismiss.run() }
                    )

                    // Generic Message Dialog
                    ModernConfirmDialog(
                        isVisible = isMessageDialogVisible,
                        title = messageTitle,
                        message = messageText,
                        confirmText = "OK",
                        cancelText = "", // Hide cancel button
                        confirmColor = Color(0xFF00E5FF),
                        icon = Icons.Default.Info,
                        onConfirm = { onMessageConfirm.run() },
                        onDismiss = { onMessageConfirm.run() }
                    )
                }
            }
        }
    }
    @JvmStatic
    fun setSimpleMessageContent(
        view: ComposeView,
        isVisible: Boolean,
        title: String,
        message: String,
        confirmText: String = "Đóng",
        onConfirm: Runnable
    ) {
        view.setContent {
            VGCameraTheme {
                ModernConfirmDialog(
                    isVisible = isVisible,
                    title = title,
                    message = message,
                    confirmText = confirmText,
                    cancelText = "", // Not used
                    confirmColor = Color(0xFF00E5FF),
                    icon = Icons.Default.Info,
                    onConfirm = { onConfirm.run() },
                    onDismiss = { onConfirm.run() }
                )
            }
        }
    }
}
