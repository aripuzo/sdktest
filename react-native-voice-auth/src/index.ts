import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

/**
 * Authentication configuration options
 */
export interface AuthConfig {
  /**
   * Title displayed during authentication
   */
  title?: string;

  /**
   * Additional custom parameters
   */
  [key: string]: any;
}

/**
 * Authentication result data
 */
export interface AuthResult {
  /**
   * User identifier
   */
  userId: string;
  
  /**
   * Authentication token
   */
  token: string;
  
  /**
   * Token expiration timestamp
   */
  expiresAt: number;
  
  /**
   * Additional result data
   */
  [key: string]: any;
}

// Check if the module is available natively
const LINKING_ERROR =
  `The package 'react-native-voice-auth' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// Get the native module
const VoiceAuth = NativeModules.VoiceAuthModule
  ? NativeModules.VoiceAuthModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// Create event emitter for native callbacks
const eventEmitter = new NativeEventEmitter(VoiceAuth);

/**
 * VoiceAuth SDK for React Native
 * 
 * Provides voice-based authentication capabilities in line with the VoiceCore MVP.
 */
export class VoiceAuthSDK {
  private listeners: Array<any> = [];

  /**
   * Initialize the VoiceAuthSDK
   * @param options Optional initialization parameters
   */
  constructor(options?: { debug?: boolean }) {
    if (options?.debug) {
      console.log('VoiceAuthSDK initialized with debug mode');
    }
  }

  /**
   * Launch the authentication flow
   * @param config Authentication configuration
   * @returns Promise that resolves with authentication result
   */
  authenticate(config: AuthConfig): Promise<AuthResult> {
    return new Promise((resolve, reject) => {
      try {
        // Set up success listener
        const successListener = eventEmitter.addListener(
          'onAuthSuccess',
          (resultString: string) => {
            try {
              const result = JSON.parse(resultString) as AuthResult;
              resolve(result);
              successListener.remove();
              this.listeners = this.listeners.filter(l => l !== successListener);
            } catch (e) {
              reject(new Error('Failed to parse authentication result'));
            }
          }
        );
        
        // Set up error listener
        const errorListener = eventEmitter.addListener(
          'onAuthError',
          (error: string) => {
            reject(new Error(error));
            errorListener.remove();
            this.listeners = this.listeners.filter(l => l !== errorListener);
          }
        );
        
        // Track listeners for cleanup
        this.listeners.push(successListener, errorListener);
        
        // Launch native authentication
        VoiceAuth.launchAuth(config);
      } catch (error) {
        reject(error);
      }
    });
  }

  /**
   * Clean up event listeners
   */
  cleanup(): void {
    this.listeners.forEach(listener => {
      if (listener && typeof listener.remove === 'function') {
        listener.remove();
      }
    });
    this.listeners = [];
  }
}

export default new VoiceAuthSDK();
