# Status Saver

Status Saver is a modern Android application built in Kotlin that enables users to view, download, manage, and share WhatsApp and WhatsApp Business statuses (images and videos) with ease. The app is designed with clean architecture using Jetpack Compose for UI and Dagger Hilt for dependency injection.

## Features

- **View WhatsApp & WhatsApp Business Statuses:** List and preview image/video statuses from both standard and business WhatsApp folders.
- **Download Statuses:** Save images and videos from WhatsApp statuses to your device for offline access.
- **Manage Saved Statuses:** View, share, and delete statuses downloaded using the app.
- **Share Functionality:** Instantly share downloaded statuses from within the app.
- **Clean Architecture:** Uses repository and domain layers for scalable, maintainable code.
- **Modern UI:** Built with Jetpack Compose and Material3 for a smooth, modern interface.
- **Dependency Injection:** Powered by Dagger Hilt for modular and testable architecture.
- **Permissions Handling:** Robust permission management for accessing external storage and media files.

## Technologies Used

- **Kotlin** – Primary language for all app logic and UI (100%)
- **Jetpack Compose** – Declarative UI framework
- **Material3** – Modern UI components and theming
- **Dagger Hilt** – Dependency injection
- **AndroidX Media3** – Video playback
- **AndroidX DocumentFile** – Safe and flexible file access
- **Coroutines** – Asynchronous operations and background processing

## Project Structure

```
app/
 └── src/
     └── main/
         ├── java/com/mg/statussaver/
         │   ├── data/
         │   │   └── repository/
         │   │        ├── StatusRepository.kt          # Data layer for fetching and managing statuses
         │   │        └── StatusRepositoryImpl.kt      # Implementation of status fetching, download, share
         │   ├── domain/
         │   │   └── repository/
         │   │        └── StatusRepository.kt          # Domain layer interface for repository
         │   ├── di/
         │   │   ├── AppModule.kt                      # DI for utilities
         │   │   └── RepositoryModule.kt               # DI for repository binding
         │   └── utils/
         │       └── PermissionManager.kt              # Permission handling utility
         └── res/
             ├── layout/                              # XML layouts (if any)
             ├── values/                              # Colors, strings, themes
             └── drawable/                            # Icons, images
```

- **data/repository/**: Contains the implementation and data source logic for status management.
- **domain/repository/**: Holds the repository interface (contract) following clean architecture.
- **di/**: Dependency injection modules using Dagger Hilt for providing and binding dependencies.
- **utils/**: Utility classes, such as permission management.
- **res/**: App resources including layouts, images, and themes.

## How It Works

1. The app scans WhatsApp and WhatsApp Business status folders for images and videos.
2. Statuses can be previewed, downloaded, deleted, or shared.
3. Downloaded statuses are saved to a public directory and optionally backed up to an app-specific folder.
4. Robust error handling ensures smooth file operations and user feedback.

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/mdgufranyt/Status-Saver.git
   ```
2. Open with Android Studio.
3. Build and run on your device.

> **Note:** The app requires permission to access external storage and media files.

## License

This project is licensed under the MIT License.

---

**Contributions and issues are welcome!**
