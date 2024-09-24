import SwiftUI
import GoogleSignIn
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
  
  var deeplink: String = ""
  
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()
    return true
  }
  
  func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
    if userActivity.activityType == NSUserActivityTypeBrowsingWeb, let incomingURL = userActivity.webpageURL {
      handleIncomingURL(url: incomingURL)
      return true
    }
    return false
  }
  
  func application(_ app: UIApplication,
                   open url: URL,
                   options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
    return GIDSignIn.sharedInstance.handle(url)
  }
  
  private func handleIncomingURL(url: URL) {
    deeplink = url.absoluteString
    // Parse the URL and navigate within your app
    print("Received URL: \(url.absoluteString)")
    
    // Example: Extract path components
    let path = url.path
    print("Path: \(path)")
    
    // Handle navigation based on path
    // e.g., navigate to a specific view controller
  }
  
}

@main
struct iOSApp: App {
  
  @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
  
  var body: some Scene {
    WindowGroup {
      ContentView(deeplink: delegate.deeplink).ignoresSafeArea()
    }
  }
}
