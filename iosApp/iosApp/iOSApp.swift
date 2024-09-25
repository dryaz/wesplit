import SwiftUI
import GoogleSignIn
import FirebaseCore
import ComposeApp

class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  lazy var deeplinkHandler = DeepLinkHandler()
  
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()
    if let userActDic = launchOptions?[.userActivityDictionary] as? [String: Any],
       let auserActivity = userActDic["UIApplicationLaunchOptionsUserActivityKey"] as? NSUserActivity {
      let urlString = auserActivity.webpageURL?.absoluteString ?? ""
      deeplinkHandler.handleDeeplink(url: urlString)
    }
    return true
  }
  
  func application(_ app: UIApplication,
                   open url: URL,
                   options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
    deeplinkHandler.handleDeeplink(url: url.absoluteString)
    return GIDSignIn.sharedInstance.handle(url)
  }
  
  // For Universal Links
  func application(_ application: UIApplication,
                   continue userActivity: NSUserActivity,
                   restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
    if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
       let url = userActivity.webpageURL {
      deeplinkHandler.handleDeeplink(url: url.absoluteString)
    }
    return true
  }
}

@main
struct iOSApp: App {
  
  @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
  
  var body: some Scene {
    WindowGroup {
      ContentView(deeplinkHandler: delegate.deeplinkHandler).ignoresSafeArea().onOpenURL { url in
        delegate.deeplinkHandler.handleDeeplink(url: url.absoluteString)
      }
    }
  }
}
