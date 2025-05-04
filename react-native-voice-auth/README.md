# React Native Voice Auth

[![npm version](https://img.shields.io/npm/v/react-native-voice-auth.svg)](https://www.npmjs.com/package/react-native-voice-auth)
[![License](https://img.shields.io/npm/l/react-native-voice-auth.svg)](https://github.com/aripuzo/sdktest/blob/master/LICENSE)

A React Native module for voice authentication, designed in accordance with VoiceCore MVP standards. This module provides a secure, self-hosted voice authentication solution for React Native applications.

## Features

- 🔒 Self-hosted & open source voice authentication
- 🌐 Cross-platform implementation (iOS & Android)
- 🔊 Secure voice biometric verification
- 📱 Native integration with telephony services
- ✅ Comprehensive test coverage

## Installation

```sh
npm install react-native-voice-auth
# or
yarn add react-native-voice-auth
```

### iOS

```sh
cd ios && pod install
```

### Android

No additional steps required for Android.

## Usage

```javascript
import VoiceAuth from 'react-native-voice-auth';

// ...

try {
  // Launch authentication flow
  const result = await VoiceAuth.authenticate({
    title: 'Voice Authentication',
    // Additional configuration options
  });
  
  console.log('Authentication successful', result);
  // Handle authentication success
  // result contains { userId, token, expiresAt, ... }
} catch (error) {
  console.error('Authentication failed', error);
  // Handle authentication failure
}

// Don't forget to clean up resources when done
VoiceAuth.cleanup();
```

## API Reference

### VoiceAuth.authenticate(config)

Launches the native voice authentication flow.

**Parameters:**

- `config` (Object): Configuration options for the authentication flow
  - `title` (String): Title displayed during authentication
  - Additional parameters can be passed based on your specific requirements

**Returns:**

- `Promise<AuthResult>`: A promise that resolves with the authentication result or rejects with an error

### VoiceAuth.cleanup()

Cleans up any resources used by the authentication module.

**Returns:**

- `void`

## Types

```typescript
// Authentication configuration
interface AuthConfig {
  title?: string;
  [key: string]: any;
}

// Authentication result
interface AuthResult {
  userId: string;
  token: string;
  expiresAt: number;
  [key: string]: any;
}
```

## Security Considerations

This module implements the following security practices:

- Secure voice data handling
- Token-based authentication
- Rate limiting for authentication attempts
- Clean data disposal after authentication

## Testing

```sh
# Run tests
yarn test
```

## Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT

## Support

For any issues or questions, please [open an issue](https://github.com/aripuzo/sdktest/issues) on our GitHub repository.
