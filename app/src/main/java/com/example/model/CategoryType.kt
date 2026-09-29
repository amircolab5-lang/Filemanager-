package com.example.model

enum class CategoryType(
    val title: String,
    val subtitle: String
) {
    IMAGES("Images", "Photos, Wallpapers, Screenshots"),
    VIDEOS("Videos", "Movies, Clips, Camera"),
    AUDIO("Audio", "Music, Recordings, Podcasts"),
    DOCUMENTS("Documents", "PDFs, Docs, Sheets, Text"),
    APKS("APKs", "Installers, Package files"),
    ARCHIVES("Archives", "ZIP, RAR, 7Z, TAR"),
    DOWNLOADS("Downloads", "Browser & app downloads"),
    WHATSAPP_STATUS("Status Saver", "Statuses, media & saved files"),
    BLUETOOTH("Bluetooth", "Received files via Bluetooth"),
    MESSENGER("Messenger", "Messenger media & voice notes"),
    ZIPS("Zips", "Compressed ZIP & RAR archives"),
    XSHARE("XShare", "Fast local file transfer"),
    XHIDE("T-Hide", "Private vault & encrypted files"),
    HIDDEN_FILES("Hidden Files", "Secret files, .nomedia & cache");

    companion object {
        val T_HIDE: CategoryType get() = XHIDE
    }
}
