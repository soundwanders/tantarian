# Tantarian

An educational dive into Kotlin and the Android development environment, Tantarian is a digital library management application for organizing and reading e-books and other PDFs.

Powered by Kotlin, Firebase, and Material Design principles.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

## 📱 Features

### Core Functionality
- **PDF Library Management**: Upload, organize, and categorize PDF documents
- **Built-in PDF Reader**: Read documents directly within the app with page navigation
- **User Authentication**: Secure Firebase-based login and registration system
- **Real-time Search**: Search through your library by title, description, or category
- **Favorites System**: Mark and manage your favorite documents
- **Download Management**: Download PDFs to device storage for offline access

### User Roles
- **Regular Users**: Browse, read, favorite, and download documents
- **Admin Users**: Full library management including upload, edit, and delete capabilities
- **Guest Access**: Browse library without authentication

### UI/UX
- **Material Design**: Clean, modern interface following Android design guidelines
- **Dark Theme**: Optimized for comfortable reading in low-light conditions
- **Responsive Layout**: Adapts to different screen sizes and orientations
- **Intuitive Navigation**: Tab-based browsing with category organization

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **Platform**: Android (API 21+)
- **Backend**: Firebase (Authentication, Realtime Database, Storage, Crashlytics)
- **PDF Rendering**: AndroidPdfViewer library
- **Image Loading**: Glide
- **UI Framework**: Android ViewBinding with Material Components
- **Architecture Pattern**: MVVM with Repository pattern

### Project Structure
```
app/src/main/java/com/soundwanders/tantarian/
├── auth/                 # Authentication activities
├── books/               # Book management and reading features
├── dashboard/           # User and admin dashboards
├── profile/             # User profile management
├── adapter/             # RecyclerView adapters
├── models/              # Data models
├── filter/              # Search and filtering logic
└── TantarianApplication # Application utilities
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or later
- Android SDK API 21 (Android 5.0) or higher
- Firebase project with the following services enabled:
  - Authentication
  - Realtime Database
  - Storage
  - Crashlytics (optional)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/soundwanders/tantarian.git
   cd tantarian
   ```

2. **Set up Firebase**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your project
   - Download the `google-services.json` file
   - Place it in the `app/` directory

3. **Configure Firebase Services**
   
   **Authentication:**
   - Enable Email/Password authentication in Firebase Console
   
   **Realtime Database:**
   - Create a database with the following structure:
   ```json
   {
     "Categories": {
       "categoryId": {
         "id": "string",
         "category": "string",
         "timestamp": "number",
         "uid": "string"
       }
     },
     "Books": {
       "bookId": {
         "id": "string",
         "uid": "string",
         "title": "string",
         "description": "string",
         "categoryId": "string",
         "url": "string",
         "timestamp": "number",
         "viewsCount": "number",
         "downloadsCount": "number"
       }
     },
     "Users": {
       "userId": {
         "uid": "string",
         "email": "string",
         "name": "string",
         "userType": "user|admin",
         "timestamp": "number",
         "profileAvatar": "string",
         "Favorites": {
           "bookId": {
             "bookId": "string",
             "timestamp": "number"
           }
         }
       }
     }
   }
   ```

4. **Build and Run**
   - Open the project in Android Studio
   - Sync the project with Gradle files
   - Run the app on an emulator or physical device

### Firebase Security Rules

**Realtime Database Rules:**
```json
{
  "rules": {
    "Users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "Categories": {
      ".read": true,
      ".write": "auth != null"
    },
    "Books": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

**Storage Rules:**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## 📖 Usage

### For Users
1. **Registration/Login**: Create an account or sign in with existing credentials
2. **Browse Library**: Explore books by category using the tab navigation
3. **Search**: Use the search bar to find specific documents
4. **Read Books**: Tap on any book to view details, then tap "Read" to open the PDF viewer
5. **Manage Favorites**: Add books to favorites for quick access
6. **Download**: Download books for offline reading

### For Administrators
1. **Category Management**: Add, edit, or delete book categories
2. **Book Management**: Upload new PDFs, edit existing book information, or remove books
3. **User Oversight**: Monitor library usage through view and download counts

## 🔧 Configuration

### Constants
Key configuration values are defined in `Constants.kt`:
- `MAX_ALLOWABLE_BYTES_PDF`: Maximum PDF file size (50MB)

### Permissions
The app requires the following permissions:
- `INTERNET`: For Firebase connectivity
- `WRITE_EXTERNAL_STORAGE`: For downloading PDFs to device storage

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Follow existing project structure and patterns


## 🔧 Troubleshooting

### Common Issues

**Firebase Connection Issues:**
- Ensure `google-services.json` is in the correct location
- Verify Firebase project configuration matches your app's package name
- Check internet connectivity

**PDF Upload/Download Issues:**
- Verify Firebase Storage is properly configured
- Check file size limits (max 50MB)
- Ensure proper storage permissions

**Build Errors:**
- Clean and rebuild the project (`Build > Clean Project`, then `Build > Rebuild Project`)
- Invalidate caches (`File > Invalidate Caches and Restart`)
- Check Android SDK and build tools versions

### Performance Optimization
- Large PDF files may take time to load; consider implementing progress indicators
- Images and PDFs are cached for better performance
- Regular cleanup of cached data is recommended

## 📞 Support

If you encounter any issues or have questions:
- Check existing [Issues](https://github.com/soundwanders/tantarian/issues)
- Create a new issue with detailed information
- Include device information, Android version, and error logs when reporting bugs

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


---

<p align="center">
  Made with ❤️ by <a href="https://github.com/soundwanders">soundwanders</a>
</p>