import { NativeModules, NativeEventEmitter } from 'react-native';
import { VoiceAuthSDK } from '../index';

// Mock the native modules
jest.mock('react-native', () => {
  const eventEmitter = {
    addListener: jest.fn(),
    removeAllListeners: jest.fn(),
  };
  
  const RN = jest.requireActual('react-native');
  
  return {
    ...RN,
    NativeModules: {
      ...RN.NativeModules,
      VoiceAuthModule: {
        launchAuth: jest.fn(),
      },
    },
    NativeEventEmitter: jest.fn(() => eventEmitter),
  };
});

describe('VoiceAuthSDK', () => {
  let voiceAuthSDK: VoiceAuthSDK;
  let mockEmitter: any;
  
  beforeEach(() => {
    jest.clearAllMocks();
    voiceAuthSDK = new VoiceAuthSDK({ debug: true });
    mockEmitter = new NativeEventEmitter(NativeModules.VoiceAuthModule);
  });
  
  it('should initialize properly', () => {
    expect(voiceAuthSDK).toBeDefined();
  });
  
  it('should call native launchAuth when authenticate is called', () => {
    const config = { title: 'Test Authentication' };
    voiceAuthSDK.authenticate(config);
    
    expect(NativeModules.VoiceAuthModule.launchAuth).toHaveBeenCalledWith(config);
  });
  
  it('should resolve promise when authentication succeeds', async () => {
    // Set up test data
    const mockResult = {
      userId: '123',
      token: 'test-token',
      expiresAt: 1715544000, // 2024-05-13T00:00:00Z
      voiceMatch: 0.95,
    };
    
    // Mock the addListener implementation to trigger success event
    const mockAddListener = jest.fn((event, callback) => {
      if (event === 'onAuthSuccess') {
        // Simulate event firing after a short delay
        setTimeout(() => {
          callback(JSON.stringify(mockResult));
        }, 100);
      }
      return { remove: jest.fn() };
    });
    
    mockEmitter.addListener.mockImplementation(mockAddListener);
    
    // Call the authenticate method and await the result
    const resultPromise = voiceAuthSDK.authenticate({ title: 'Test' });
    const result = await resultPromise;
    
    // Verify the result matches our expected data
    expect(result).toEqual(mockResult);
  });
  
  it('should reject promise when authentication fails', async () => {
    // Mock the addListener implementation to trigger error event
    const mockAddListener = jest.fn((event, callback) => {
      if (event === 'onAuthError') {
        // Simulate event firing after a short delay
        setTimeout(() => {
          callback('Authentication failed');
        }, 100);
      }
      return { remove: jest.fn() };
    });
    
    mockEmitter.addListener.mockImplementation(mockAddListener);
    
    // Call the authenticate method and expect it to reject
    await expect(voiceAuthSDK.authenticate({ title: 'Test' })).rejects.toThrow('Authentication failed');
  });
  
  it('should clean up listeners when cleanup is called', () => {
    // Create some mock listeners
    const mockListener1 = { remove: jest.fn() };
    const mockListener2 = { remove: jest.fn() };
    
    // Add them to the SDK instance
    (voiceAuthSDK as any).listeners = [mockListener1, mockListener2];
    
    // Call cleanup
    voiceAuthSDK.cleanup();
    
    // Verify all listeners were removed
    expect(mockListener1.remove).toHaveBeenCalled();
    expect(mockListener2.remove).toHaveBeenCalled();
    expect((voiceAuthSDK as any).listeners).toEqual([]);
  });
});
