import SwiftUI
import GoogleSignIn
import FirebaseCore
import ComposeApp

class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  // Store the launched shortcut item if the app is not active
  var launchedShortcutItem: UIApplicationShortcutItem?
  
  lazy var deeplinkHandler = DeepLinkHandler()
  
  func application(
    _ application: UIApplication,
    performActionFor shortcutItem: UIApplicationShortcutItem,
    completionHandler: @escaping (Bool) -> Void
  ) {
    let handled = handleShortcutItem(shortcutItem)
    completionHandler(handled)
  }
  
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()
    
    print("!@# a1")
    
    var shouldPerformAdditionalDelegateHandling = true

    if let shortcutItem = launchOptions?[.shortcutItem] as? UIApplicationShortcutItem {
      print("!@# a2")
      launchedShortcutItem = shortcutItem
      shouldPerformAdditionalDelegateHandling = false
    }
    
    if let userActDic = launchOptions?[.userActivityDictionary] as? [String: Any],
       let auserActivity = userActDic["UIApplicationLaunchOptionsUserActivityKey"] as? NSUserActivity {
      let urlString = auserActivity.webpageURL?.absoluteString ?? ""
      deeplinkHandler.handleDeeplink(url: urlString)
    }
    
    return shouldPerformAdditionalDelegateHandling
  }
  
  func application(_ app: UIApplication,
                   open url: URL,
                   options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
    print("!@# b1")
    deeplinkHandler.handleDeeplink(url: url.absoluteString)
    return GIDSignIn.sharedInstance.handle(url)
  }
  
  // For Universal Links
  func application(_ application: UIApplication,
                   continue userActivity: NSUserActivity,
                   restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
    print("!@# c1")
    if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
       let url = userActivity.webpageURL {
      deeplinkHandler.handleDeeplink(url: url.absoluteString)
    }
    return true
  }
  
  func applicationDidBecomeActive(_ application: UIApplication) {
    guard let shortcutItem = launchedShortcutItem else { return }
    _ = handleShortcutItem(shortcutItem)
    print("!@# d1")
    launchedShortcutItem = nil
  }
  
  func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {

      if let shortcutItem = connectionOptions.shortcutItem {
        launchedShortcutItem = shortcutItem
      }
  }
  
  func sceneDidBecomeActive(_ scene: UIScene) {
      if let shortcutItem = launchedShortcutItem {
        handleShortcutItem(shortcutItem)
        launchedShortcutItem = nil
      }
  }
  
  private func handleShortcutItem(_ shortcutItem: UIApplicationShortcutItem) -> Bool {
    // Parse the shortcut item and navigate accordingly
    print("!@# e1")
    print("Handle shortcut")
    let type = shortcutItem.type
    let userInfo = shortcutItem.userInfo
    print("\(userInfo)")
    switch type {
    case let t where t.starts(with: "add_exp_"):
      if let deeplink = userInfo?["group_deeplink"] as? String {
        // Navigate to the specific screen
        deeplinkHandler.handleDeeplink(url: deeplink)
        return true
      }
    default:
      return false
    }
    return false
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
      ContentView(deeplinkHandler: delegate.deeplinkHandler).ignoresSafeArea().onOpenURL { url in
        delegate.deeplinkHandler.handleDeeplink(url: url.absoluteString)
      }
    }
  }
}
