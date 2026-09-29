# Google Play Console Submission Guide for Ticno File Manager

Use this exact text and metadata when filling out your Google Play Console listing.

---

## 1. Store Listing Details

### App Title (Max 30 characters - Google Play Policy Compliant)
```text
Ticno File Manager: Explorer
```

### Short Description (Max 80 characters)
```text
Fast, secure file manager with SD card, cleaner, vault & WhatsApp status saver.
```

### Full Description (Max 4,000 characters)
```text
Ticno File Manager is a lightning-fast, secure, and modern file explorer designed for Android. Easily manage files on internal storage and SD card, clean junk files, organize documents, and protect private photos in a secure vault.

⚡ KEY FEATURES

📂 Full File Management
• Browse, copy, move, rename, delete, and share any file or folder.
• Seamless dual storage support: Internal Storage and Removable SD Card.
• Create new folders, text documents, and extract/compress ZIP files with ease.

⚡ Lightning-Fast Category Browsing
• Instant access to Images, Videos, Audio, Documents, APKs, and Archives.
• Smart search bar with real-time filtering as you type.
• Grid and List views with customizable sorting (by Name, Date, Size).

🧹 Storage Cleaner & Junk Remover
• Reclaim valuable space with one-tap junk cleaner.
• Detect app cache, temporary log files, and obsolete APKs.
• Find and remove large files taking up storage.

🔒 THide Private Vault
• Lock private photos, videos, and sensitive files inside an encrypted local vault.
• Keep confidential documents invisible from the gallery and other apps.

📱 WhatsApp Status Saver
• View and save WhatsApp & WhatsApp Business photo/video statuses.
• Saved statuses are instantly transferred to your Gallery (Pictures/TicnoSaver) so they never expire.

📁 Hidden Files Explorer
• Easily toggle visibility of hidden files (.nomedia, cache, dot files) to manage hidden app data.

✨ Clean & Modern Interface
• Built with Google Jetpack Compose and Material 3 design.
• Dark Mode and Light Mode with vibrant high-contrast controls.
• 100% Offline-First: Your personal data stays strictly on your device.

---

🔒 PRIVACY & PERMISSIONS
Ticno File Manager respects your privacy. All your files remain on your phone and are NEVER uploaded to external servers.
• All Files Access (MANAGE_EXTERNAL_STORAGE): Required for core file management across internal and SD storage.
• Internet / Network State: Required solely for Google AdMob standard advertising.

📧 Support & Feedback: amir03182055632.ticno@blogger.com
🌐 Privacy Policy: https://ticnodevelopers.blogspot.com/p/file-manager-privacy-policy.html
```

---

## 2. Google Play Console Setup & Declarations

### A. App Category & Tags
- **Category:** Tools / Productivity
- **Tags:** File manager, Storage cleaner, SD card, File explorer, WhatsApp status saver

### B. Content Rating (IARC Questionnaire)
- Violence, Sexual Content, Controlled Substances: **No**
- Shares user location: **No**
- Allows users to interact or exchange text messages: **No**
- Shares personal info: **No**
- Result: **Rated for 3+ / Everyone**

### C. Target Audience
- Age: **13 and older** (or 18+)
- Not directed to children under 13.

### D. App Access (Reviewer Login)
- Select: **"All functionality is available without special access"** (no username/password needed).

### E. Ads Declaration
- Select: **"Yes, my app contains ads"** (since Google AdMob is integrated).

---

## 3. Data Safety Form Answers (Mandatory)

1. **Does your app collect or share any user data?**
   - Select: **"Yes"** (due to Google AdMob SDK, not file contents).

2. **Is all user data collected encrypted in transit?**
   - Select: **"Yes"** (HTTPS).

3. **Do you provide a way for users to request data deletion?**
   - Select: **"Not applicable"** (No user accounts are created).

4. **Data Types (Select only Google AdMob items):**
   - **Device or other IDs:**
     - Device or other IDs (Advertising ID / GAID)
     - Collected: **Yes**
     - Shared: **Yes** (Shared with Google AdMob)
     - Purpose: **Advertising or marketing, Analytics**
   - **Personal Files / Photos / Videos / Audio / Documents:**
     - Select: **NO** (Ticno File Manager does not collect, store, or transmit any user files).

---

## 4. `MANAGE_EXTERNAL_STORAGE` Declaration (Google Play Review Justification)

Google Play enforces strict scrutiny for the "All Files Access" permission. When filling out the declaration form in Play Console, use this exact description:

### Core Purpose of the App:
```text
File Management (Document and File Explorer)
```

### Justification Text for Google Reviewers:
```text
Ticno File Manager is a dedicated file management tool whose primary purpose is to allow users to browse, search, copy, move, delete, rename, compress (ZIP), and extract files across both internal device storage and removable SD cards. 

Without the MANAGE_EXTERNAL_STORAGE permission, the app cannot access files outside of its private app-specific directory, rendering its core functionality as a universal Android file manager inoperable. Scoped Storage / Storage Access Framework (SAF) does not provide the multi-file batch operations, comprehensive media categorization, hidden file browsing, and storage cleaning required by a full-fledged file explorer. 

All file operations are performed 100% locally on-device. No user files are ever collected, uploaded, or transmitted off the device.
```

---

## 5. Required Graphical Assets

| Asset | Size | Format | Status |
| :--- | :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | 32-bit PNG (no alpha for Play Store icon upload) | Generated in `res/drawable/app_folder_icon.png` |
| **Feature Graphic** | 1024 x 500 px | JPG or 24-bit PNG | Can be created from the branding logo & folder icon |
| **Phone Screenshots** | Min 2, Max 8 (e.g. 1080 x 2400 px) | JPG / PNG | Capture: Home screen, Explorer screen, Storage Cleaner, WhatsApp Status Saver, Settings |
