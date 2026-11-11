
# EndeKise – Personal Finance Manager

**Take control of your money. Track your expenses. Manage your financial life.**

EndeKise is an Android personal finance management application designed to help users organize their finances, monitor income and expenses, manage multiple wallets, create spending budgets, and understand their financial activities through an intuitive dashboard.

The application combines local data storage with optional Firebase cloud synchronization, providing users with tools to manage their everyday financial activities.

---

## ✨ Features

### 1. Financial Dashboard
- View total available balance across all wallets.
- Monitor monthly income and expenses.
- Visualize financial activities through interactive charts.
- Filter financial statistics by month.
- View recent transactions directly from the dashboard.
- Hide or reveal financial amounts using Privacy Mode.
- Access daily financial summaries.

### 2. Transaction Management
- Record income and expenses.
- Categorize transactions for better organization.
- Add notes to financial transactions.
- Select the wallet associated with each transaction.
- View complete transaction history.
- Delete transactions with an Undo option.
- Automatically update wallet balances based on financial activities.

### 3. Wallet Management
Create and manage multiple financial accounts, including:
- Bank accounts
- Mobile Money accounts
- Cash wallets
- Credit cards
- Savings accounts

Additional capabilities:
- Set an initial balance for each wallet.
- Monitor individual wallet balances.
- View combined balances across all wallets.
- Transfer money between wallets.
- Prevent transfers and expenses that exceed available wallet balances.

### 4. Budget Management
- Create custom income and expense categories.
- Set daily or monthly budget limits.
- Monitor spending against category budgets.
- View income and expenses within selected dates.
- Receive alerts when spending approaches budget limits.
- Track financial activities using an interactive calendar.

### 5. Security and Privacy
- Set up a personal four-digit PIN.
- Enable or disable application lock.
- Require PIN verification when accessing a locked application.
- Configure a custom PIN reminder hint.
- Hide sensitive financial information using Privacy Mode.

### 6. Cloud Synchronization
EndeKise integrates Firebase services to provide optional cloud functionality.

- Sign in using a Google account.
- Authenticate users through Firebase Authentication.
- Upload transaction records to Cloud Firestore.
- Restore transaction records from cloud storage.
- Organize cloud transaction data under individual user accounts.
- Access cloud synchronization through the Settings section.
- Includes a seven-day trial mechanism for cloud synchronization.

### 7. Data Export
- Export transaction history into CSV format.
- Save exported files in the device's Downloads folder.
- Use exported financial records for external analysis and record keeping.

### 8. User Profile and Settings
- Customize profile information.
- Select a profile picture.
- Manage application security settings.
- Access cloud synchronization options.
- Export transaction data.
- Access application support information.

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Primary application programming language |
| XML | Android user interface layouts |
| Android SDK | Native Android application development |
| Android Studio | Development environment |
| Room Database | Local SQLite database management |
| Firebase Authentication | Google account authentication |
| Cloud Firestore | Cloud transaction storage and synchronization |
| Google Sign-In | Google account integration |
| MPAndroidChart | Financial charts and visualizations |
| Material Design Components | Modern Android user interface |
| Android Jetpack | ViewModel, LiveData, and application architecture |
| Gson | JSON serialization and deserialization |
| Gradle | Build automation and dependency management |

---

## 🏗️ Application Architecture

EndeKise follows a layered Android application architecture that separates user interface components, application logic, and data management.

The main architectural components include:

- **Activities:** Manage application entry points, navigation, splash screen, and PIN authentication.
- **Fragments:** Handle individual application sections such as Home, Wallets, Budget, and Settings.
- **ViewModel:** Provides and manages application data for UI components.
- **Repository:** Acts as an intermediary between the ViewModel and the local database.
- **Room Database:** Provides persistent local storage for financial records.
- **Firebase Services:** Handle Google authentication and optional cloud transaction synchronization.

### Project Structure

```text
EndeKise/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/endekise/
│           │       ├── MainActivity.java
│           │       ├── SplashActivity.java
│           │       ├── PinLockActivity.java
│           │       ├── adapter/
│           │       ├── database/
│           │       ├── entity/
│           │       ├── repository/
│           │       ├── ui/
│           │       ├── utils/
│           │       └── viewmodel/
│           │
│           ├── res/
│           │   ├── drawable/
│           │   ├── layout/
│           │   ├── mipmap/
│           │   └── values/
│           │
│           └── AndroidManifest.xml
│
├── gradle/
│   └── libs.versions.toml
│
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 🚀 Getting Started

Follow these instructions to set up EndeKise on your local machine.

### Prerequisites

Make sure you have the following installed:

- Android Studio
- JDK 11 or a compatible JDK supported by your Android Gradle Plugin version
- Android SDK
- An Android device or emulator running Android 7.0 (API 24) or higher
- An internet connection for downloading dependencies and using Firebase services

### 1. Clone the Repository

```bash
git clone https://github.com/feysel2003/EndeKise.git
```

### 2. Open the Project

1. Launch Android Studio.
2. Select **Open**.
3. Navigate to the cloned `EndeKise` directory.
4. Open the project.
5. Allow Android Studio to synchronize Gradle dependencies.

### 3. Configure Firebase

Firebase configuration is required for Google authentication and cloud synchronization.

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Create a Firebase project.
3. Register an Android application using the package name:

   `com.example.endekise`

4. Download the `google-services.json` configuration file.
5. Place the file inside the application's `app/` directory.
6. Enable Google as a sign-in provider in Firebase Authentication.
7. Create a Cloud Firestore database.
8. Configure appropriate Firestore security rules to protect user-specific data.

**Important:** Use your own Firebase project configuration. Do not commit private credentials or sensitive configuration files to a public repository.

### 4. Build and Run

You can run the application directly from Android Studio:

1. Connect an Android device or start an emulator.
2. Select the `app` run configuration.
3. Click **Run**.

Alternatively, use the Gradle wrapper.

**Windows:**

```bash
gradlew.bat assembleDebug
```

**macOS / Linux:**

```bash
./gradlew assembleDebug
```

---

## 📱 Minimum Requirements

| Requirement | Configuration |
|---|---|
| Platform | Android |
| Minimum Android Version | Android 7.0 (Nougat) |
| Minimum SDK | API 24 |
| Compile SDK | API 36 |
| Target SDK | API 36 |
| Application Language | Java |
| Java Compatibility | Java 11 |
| Application ID | `com.example.endekise` |
| Version | 1.0 |
| Version Code | 1 |

---

## ☁️ Data Storage

EndeKise uses a combination of local and cloud storage technologies.

### Local Storage
- Room Database (SQLite) stores financial records on the device.
- Wallets, transactions, and categories are managed through the local data layer.
- Application preferences store settings such as the PIN lock and profile information.

### Cloud Storage
- Firebase Authentication handles Google sign-in.
- Cloud Firestore stores transaction records under individual user accounts.
- Users can manually upload and restore transaction records.

**Note:** Cloud synchronization currently focuses on transactions; wallet and category synchronization is not implemented as a complete cloud backup.

---

## 🧪 Testing

The project includes dependencies for:

- JUnit 4 for unit testing.
- AndroidX JUnit for Android instrumentation testing.
- Espresso for UI testing.

---

## 🔮 Future Improvements

Potential areas for further development include:

- Complete cloud backup and synchronization for wallets, categories, and transactions.
- Automatic background synchronization.
- Enhanced financial reports and analytics.
- Recurring income and expense management.
- Multiple currency support with exchange-rate conversion.
- Improved data encryption and security.
- Automated financial insights and spending recommendations.
- Integrated Google Play Billing for premium subscriptions.

---

## 👨‍💻 Developer

**Developed by Feysel Mifta**

- GitHub: [@feysel2003](https://github.com/feysel2003)
- Repository: [EndeKise](https://github.com/feysel2003/EndeKise)

---

## 📄 License

No license has currently been specified for this repository. All rights are reserved by the repository owner unless a license is added.

---

**EndeKise – Manage your money. Make better financial decisions.**
