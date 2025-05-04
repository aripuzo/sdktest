# Authentication SDK Integration Guide

This guide explains how to integrate, customize, and utilize the Authentication SDK in your React Native application, following the VoiceCore MVP standards for telephony-aware authentication.

## Table of Contents

- [Setup](#setup)
- [Basic Usage](#basic-usage)
- [Customizing Authentication UI](#customizing-authentication-ui)
- [Handling Authentication Results](#handling-authentication-results)
- [Advanced Configuration](#advanced-configuration)
- [Troubleshooting](#troubleshooting)

## Setup

### Prerequisites

- React Native project (version 0.65 or higher)
- Android SDK (API level 21+)
- JDK 11+

### Installation

1. If you haven't already, initialize your React Native project:

```bash
npx react-native init MyApp
```

2. Include the Auth SDK dependencies in your project's `android/app/build.gradle`:

```gradle
dependencies {
    // ... other dependencies
    implementation project(':auth-sdk')  // Add this line
}
```

3. Configure your `android/settings.gradle` to include the auth-sdk:

```gradle
include ':app', ':auth-sdk'
project(':auth-sdk').projectDir = new File(rootProject.projectDir, './auth-sdk')
```

4. Create the interface in your project (e.g., `src/AuthSDK.ts`):

```typescript
import { NativeModules, NativeEventEmitter } from 'react-native';

const { AuthModule } = NativeModules;
const emitter = new NativeEventEmitter(AuthModule);

let authListener: any;

export const launchAuth = (config: object, onSuccess: (data: any) => void) => {
  AuthModule.launchAuth(config);
  authListener = emitter.addListener('onAuthSuccess', (result: string) => {
    try {
      const parsed = JSON.parse(result);
      onSuccess(parsed);
    } catch (e) {
      console.error('Invalid result format', e);
    }
  });
};

export const removeAuthListener = () => {
  if (authListener) {
    authListener.remove();
    authListener = null;
  }
};
```

## Basic Usage

To launch the authentication flow:

```typescript
import React, { useEffect } from 'react';
import { Button } from 'react-native';
import { launchAuth, removeAuthListener } from './AuthSDK';

const LoginScreen = () => {
  useEffect(() => {
    // Clean up when component unmounts
    return () => {
      removeAuthListener();
    };
  }, []);

  const handleLogin = () => {
    launchAuth(
      {
        title: 'Authentication',
        backgroundColor: 'FFFFFF',  // White background
        buttonColor: '007BFF',      // Blue buttons
        textColor: '000000',        // Black text
        showEmailField: true,
        showPasswordField: true
      },
      (result) => {
        console.log('Authentication successful:', result);
        // Handle successful authentication
        // e.g., store user token, navigate to main screen
      }
    );
  };

  return (
    <Button title="Authenticate" onPress={handleLogin} />
  );
};

export default LoginScreen;
```

## Customizing Authentication UI

You can customize the authentication UI by passing configuration options:

```typescript
launchAuth(
  {
    // Basic customization
    title: 'Company Auth',
    backgroundColor: 'F5F5F5',    // Light gray background
    buttonColor: 'E91E63',        // Pink buttons
    buttonTextColor: 'FFFFFF',    // White button text
    textColor: '333333',          // Dark gray text
    
    // Control which fields are displayed
    showEmailField: true,
    showPasswordField: true,
    showUsernameField: true,
    showFirstNameField: false,    // Hide first name field
    showLastNameField: false,     // Hide last name field
    
    // Custom labels
    submitButtonText: 'Verify',
    emailLabel: 'Work Email',
    passwordLabel: 'Secret Password',
    usernameLabel: 'Employee ID',
  },
  (result) => {
    // Handle result
  }
);
```

### Customization Options

| Option | Type | Description |
|--------|------|-------------|
| `title` | String | Title displayed at the top of the authentication screen |
| `backgroundColor` | String | Background color in hex format (without #) |
| `buttonColor` | String | Color for the submit button |
| `buttonTextColor` | String | Color for text on the submit button |
| `textColor` | String | Color for regular text and labels |
| `showEmailField` | Boolean | Whether to show the email input field |
| `showPasswordField` | Boolean | Whether to show the password input field |
| `showUsernameField` | Boolean | Whether to show the username input field |
| `showFirstNameField` | Boolean | Whether to show the first name input field |
| `showLastNameField` | Boolean | Whether to show the last name input field |
| `submitButtonText` | String | Text for the submit button |
| `emailLabel` | String | Label for the email field |
| `passwordLabel` | String | Label for the password field |
| `usernameLabel` | String | Label for the username field |
| `firstNameLabel` | String | Label for the first name field |
| `lastNameLabel` | String | Label for the last name field |

## Handling Authentication Results

The authentication results are passed to the callback function you provide to `launchAuth()`. The result object contains several properties:

```typescript
interface AuthResult {
  // User identifiers
  userId: string;       // Unique identifier for the user
  username?: string;    // Username if collected
  email?: string;       // Email if collected
  firstName?: string;   // First name if collected
  lastName?: string;    // Last name if collected
}
```

Example of handling the result:

```typescript
launchAuth(
  {
    title: 'Authentication',
  },
  (result) => {
    // Check if authentication was successful
    if (result && result.token) {
      // Store authentication token
      saveToken(result.token);
      
      // Store user data
      saveUserData({
        id: result.userId,
        email: result.email,
        name: `${result.firstName || ''} ${result.lastName || ''}`.trim(),
      });
      
      // Check token expiration
      const expiresInSeconds = result.expiresAt - Math.floor(Date.now() / 1000);
      console.log(`Token expires in ${expiresInSeconds} seconds`);
      
      // Navigate to authenticated area
      navigation.navigate('Home');
    }
  }
);
```

### Error Handling

The current implementation does not have direct error callbacks in the React Native interface. However, you can implement error handling by:

1. Adding an error listener in the `AuthSDK.ts` file:

```typescript
let errorListener: any;

export const launchAuth = (
  config: object, 
  onSuccess: (data: any) => void,
  onError?: (error: string) => void
) => {
  AuthModule.launchAuth(config);
  
  authListener = emitter.addListener('onAuthSuccess', (result: string) => {
    try {
      const parsed = JSON.parse(result);
      onSuccess(parsed);
    } catch (e) {
      if (onError) onError('Invalid result format');
      console.error('Invalid result format', e);
    }
  });
  
  if (onError) {
    errorListener = emitter.addListener('onAuthError', (error: string) => {
      onError(error);
    });
  }
};

export const removeAuthListener = () => {
  if (authListener) {
    authListener.remove();
    authListener = null;
  }
  if (errorListener) {
    errorListener.remove();
    errorListener = null;
  }
};
```

2. Using the error callback in your code:

```typescript
launchAuth(
  { title: 'Authentication' },
  (result) => {
    console.log('Success:', result);
  },
  (error) => {
    console.error('Authentication failed:', error);
    // Show error message to user
    Alert.alert('Authentication Failed', error);
  }
);
```

## Advanced Configuration

### Adding the Auth Module to a New React Native Project

To add the Auth Module to a new React Native project:

1. Copy the `android/auth-sdk` directory to your project's `android` folder
2. Register the module in your `MainApplication.java`:

```java
// MainApplication.java
@Override
protected List<ReactPackage> getPackages() {
  @SuppressWarnings("UnnecessaryLocalVariable")
  List<ReactPackage> packages = new PackageList(this).getPackages();
  // Add the AuthPackage
  packages.add(new AuthPackage());
  return packages;
}
```

3. Create the AuthPackage.kt file:

```kotlin
// AuthPackage.kt
package com.yourpackage

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class AuthPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(AuthModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
```

### Customizing the Authentication Logic

The current implementation simulates authentication. To implement real biometrics:

1. Modify `AuthenticationActivity.kt` to capture and process input:

```kotlin
// Inside AuthenticationScreen.kt, add capture button and processing
Button(
    onClick = { 
        // Start voice recording
        startVoiceCapture() 
    },
    colors = ButtonDefaults.buttonColors(
        backgroundColor = Color.fromHex(authConfig.buttonColor)
    ),
    modifier = Modifier.fillMaxWidth(),
) {
    Text(
        "Capture Sample",
        color = Color.fromHex(authConfig.buttonTextColor)
    )
}
```

## Troubleshooting

### Common Issues

1. **Module not found**: Ensure the auth-sdk is correctly linked in your Gradle files

2. **Authentication Activity not showing**: Check that you've registered the activity in your AndroidManifest.xml:

```xml
<activity
    android:name="com.test.auth_sdk.AuthenticationActivity"
    android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|screenSize|smallestScreenSize|uiMode"
    android:exported="false"
    android:label="@string/app_name"
    android:windowSoftInputMode="adjustResize" />
```

3. **Results not received in React Native**: Make sure you're correctly setting up the event listener and the event names match between native and JS code

4. **Customization not applied**: Verify the config object properties match what the native module expects

### Debugging Tips

1. Add logging in the native module:

```kotlin
Log.d("AuthModule", "Received config: ${Gson().toJson(config)}")
```

2. Enable debug mode in your React Native app:

```typescript
if (__DEV__) {
  console.log('Launching auth with config:', config);
}
```

3. Monitor logcat for native errors:

```bash
adb logcat | grep AuthModule
```

## Security Considerations

- The authentication token is transmitted from native code to JavaScript. Ensure your app follows security best practices for storing sensitive tokens.
- Consider implementing token refresh mechanisms when tokens expire.
- For production deployments, implement proper certificate pinning and secure storage of credentials.

---

*This integration guide adheres to the Semantic Seed Coding Standards (SSCS) for the VoiceCore MVP project.*
