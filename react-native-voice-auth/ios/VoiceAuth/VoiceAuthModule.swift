import Foundation
import AVFoundation

@objc(VoiceAuthModule)
class VoiceAuthModule: NSObject {
  
  // MARK: - Properties
  private var hasListeners: Bool = false
  private let eventAuthSuccess = "onAuthSuccess"
  private let eventAuthError = "onAuthError"
  
  // MARK: - React Native Methods
  
  @objc(launchAuth:)
  func launchAuth(_ config: NSDictionary) -> Void {
    DispatchQueue.main.async {
      guard let rootViewController = RCTPresentedViewController() else {
        self.sendErrorEvent(message: "No view controller is available")
        return
      }
      
      // In a real implementation, we would integrate with a voice authentication SDK
      // For now, we'll simulate the authentication process
      
      let title = config["title"] as? String ?? "Voice Authentication"
      
      let alertController = UIAlertController(
        title: title,
        message: "Voice authentication is being simulated. In a real implementation, this would use the microphone to authenticate the user.",
        preferredStyle: .alert
      )
      
      alertController.addAction(UIAlertAction(title: "Authenticate", style: .default) { _ in
        // Simulate successful authentication
        let result: [String: Any] = [
          "userId": UUID().uuidString,
          "token": UUID().uuidString,
          "expiresAt": Int(Date().timeIntervalSince1970 + 3600),
          "voiceMatch": 0.95
        ]
        
        if let jsonData = try? JSONSerialization.data(withJSONObject: result),
           let jsonString = String(data: jsonData, encoding: .utf8) {
          self.sendSuccessEvent(data: jsonString)
        } else {
          self.sendErrorEvent(message: "Failed to serialize result")
        }
      })
      
      alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel) { _ in
        self.sendErrorEvent(message: "Authentication was canceled")
      })
      
      rootViewController.present(alertController, animated: true, completion: nil)
    }
  }
  
  // MARK: - RCTEventEmitter Methods
  
  @objc
  func supportedEvents() -> [String] {
    return [eventAuthSuccess, eventAuthError]
  }
  
  @objc
  func startObserving() {
    hasListeners = true
  }
  
  @objc
  func stopObserving() {
    hasListeners = false
  }
  
  // MARK: - Private Methods
  
  private func sendSuccessEvent(data: String) {
    if hasListeners {
      sendEvent(withName: eventAuthSuccess, body: data)
    }
  }
  
  private func sendErrorEvent(message: String) {
    if hasListeners {
      sendEvent(withName: eventAuthError, body: message)
    }
  }
  
  @objc
  func sendEvent(withName name: String, body: Any) {
    RCTEventEmitter().sendEvent(withName: name, body: body)
  }
  
  // MARK: - Required Methods
  
  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }
}
