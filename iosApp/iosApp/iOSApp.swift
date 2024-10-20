import SwiftUI
import GoogleSignIn
import FirebaseCore
import ComposeApp

class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  lazy var deeplinkHandler = Dependencies.shared.deepLinkHandler
  
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

extension UIApplication {
    class func topViewController(base: UIViewController? = UIApplication.shared.connectedScenes
                                    .filter { $0.activationState == .foregroundActive }
                                    .compactMap { $0 as? UIWindowScene }
                                    .first?.windows
                                    .filter { $0.isKeyWindow }
                                    .first?.rootViewController) -> UIViewController? {
        if let nav = base as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }

        if let tab = base as? UITabBarController, let selected = tab.selectedViewController {
            return topViewController(base: selected)
        }

        if let presented = base?.presentedViewController {
            return topViewController(base: presented)
        }

        return base
    }
}

@main
struct iOSApp: App {
  
  @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
  
  var body: some Scene {
    WindowGroup {
      ContentView().ignoresSafeArea().onOpenURL { url in
        delegate.deeplinkHandler.handleDeeplink(url: url.absoluteString)
      }.onAppear {
        Dependencies.shared.billingDelegate.listenForTransactionUpdates()
      }
    }
  }
}
