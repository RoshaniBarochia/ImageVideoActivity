package com.app.imagevideoactivity.model

data class DriveFile(
    val id: String,
    val name: String,
    var mimeType: String,
    val webViewLink: String?,   // Google Drive preview link (not direct)
    val webContentLink: String?, // Google Drive content link (for files)
    val thumbnailLink: String? // Direct Google Drive-generated image link

) {
    init {
        // If Google Drive incorrectly returns an image MIME, check file extension
        if ((name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".avi"))) {
            mimeType = "video/mp4"  // Force correct video type
        }
    }
    fun isImage(): Boolean = mimeType.startsWith("image/")
}

