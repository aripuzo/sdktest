import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

/**
 * Authentication configuration options
 *
 * Based on VoiceCore MVP standards for telephony-aware authentication.
 */
export interface AuthConfig {
  /**
   * Title displayed during authentication
   */
  title?: string;

  /**
   * Background color in hex format (without #)
   */
  backgroundColor?: string;

  /**
   * Button color in hex format (without #)
   */
  buttonColor?: string;

  /**
   * Text color in hex format (without #)
   */
  textColor?: string;

  /**
   * Whether to show email field
   */
  showEmailField?: boolean;

  /**
   * Whether to show password field
   */
  showPasswordField?: boolean;

  /**
   * Whether to show username field
   */
  showUsernameField?: boolean;

  /**
   * Whether to show first name field
   */
  showFirstNameField?: boolean;

  /**
   * Whether to show last name field
   */
  showLastNameField?: boolean;

  /**
   * Text for submit button
   */
  submitButtonText?: string;

  /**
   * Additional custom parameters
   */
  [key: string]: any;
}

/**
 * Authentication result data
 *
 * Structure follows VoiceCore MVP standards for voice authentication results.
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
   * Token expiration timestamp (Unix timestamp in seconds)
   */
  expiresAt: number;
  
  /**
   * Voice match confidence score (0.0 - 1.0)
   */
  voiceMatch?: number;
  
  /**
   * Optional user information
   */
  email?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  
  /**
   * Additional result data
   */
  [key: string]: any;
}

/**
 * Event type for authentication progress updates
 */
export enum AuthProgressEvent {
  INITIALIZING = 'initializing',
  RECORDING = 'recording',
  PROCESSING = 'processing',
  COMPLETED = 'completed'
}

/**
 * Options for authentication
 */
export interface AuthOptions {
  /**
   * Enable debug logging
   */
  debug?: boolean;

  /**
   * Callback for progress events
   */
  onProgress?: (event: string) => void;
}

// Check if the module is available natively
const LINKING_ERROR =
  'The package \'react-native-voice-auth\' doesn\'t seem to be linked. Make sure: \n\n' +
  Platform.select({ ios: '- You have run \'pod install\'\n', default: '' }) +
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
 * Provides voice-based authentication capabilities following
 * the VoiceCore MVP standards for telephony-aware applications.
 */
export class VoiceAuthSDK {
  private listeners: Array<any> = [];
  private debug: boolean = false;

  /**
   * Initialize the VoiceAuthSDK
   * @param options Optional initialization parameters
   */
  constructor(options?: AuthOptions) {
    this.debug = options?.debug || false;

    if (this.debug) {
      console.log('VoiceAuthSDK initialized with debug mode');
      console.log('Native module version:', VoiceAuth.VERSION || 'unknown');
    }
  }

  /**
   * Launch the authentication flow
   * @param config Authentication configuration
   * @param options Authentication options
   * @returns Promise that resolves with authentication result
   */
  authenticate(config: AuthConfig, options?: AuthOptions): Promise<AuthResult> {
    return new Promise((resolve, reject) => {
      try {
        // Apply default options if not provided
        const mergedOptions = {
          ...{ debug: this.debug },
          ...(options || {}),
        };

        // Configure default config values following VoiceCore standards
        const enhancedConfig: AuthConfig = {
          title: 'Voice Authentication',
          backgroundColor: 'FFFFFF',
          textColor: '000000',
          buttonColor: '007BFF',
          showEmailField: true,
          showPasswordField: true,
          submitButtonText: 'Authenticate',
          ...config
        };
        
        // Set up success listener
        const successListener = eventEmitter.addListener(
          'onAuthSuccess',
          (resultString: string) => {
            try {
              const result = JSON.parse(resultString) as AuthResult;
              if (mergedOptions.debug) {
                console.log('Authentication successful:', result);
              }
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
            if (mergedOptions.debug) {
              console.error('Authentication error:', error);
            }
            reject(new Error(error));
            errorListener.remove();
            this.listeners = this.listeners.filter(l => l !== errorListener);
          }
        );
        
        // Set up progress listener if callback provided
        let progressListener: any = null;
        if (mergedOptions.onProgress) {
          progressListener = eventEmitter.addListener(
            'onAuthProgress',
            (progress: string) => {
              if (mergedOptions.debug) {
                console.log('Authentication progress:', progress);
              }
              mergedOptions.onProgress?.(progress);
            },
          );
          this.listeners.push(progressListener);
        }

        // Track listeners for cleanup
        this.listeners.push(successListener, errorListener);

        // Launch native authentication
        if (mergedOptions.debug) {
          console.log('Launching auth with config:', enhancedConfig);
        }
        VoiceAuth.launchAuth(enhancedConfig);
      } catch (error) {
        if (this.debug) {
          console.error('Error launching authentication:', error);
        }
        reject(error);
      }
    });
  }

  /**
   * Clear stored credentials for a user
   * @param username The username to clear credentials for
   * @returns Promise that resolves with boolean indicating success
   */
  clearCredentials(username: string): Promise<boolean> {
    return VoiceAuth.clearCredentials(username);
  }

  /**
   * Get the native module version
   * @returns Version string of the native module
   */
  getVersion(): string {
    return VoiceAuth.VERSION || 'unknown';
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

    if (this.debug) {
      console.log('VoiceAuthSDK resources cleaned up');
    }
    
    this.listeners = [];
  }
}

export default new VoiceAuthSDK();
