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
