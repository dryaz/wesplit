import UIKit
import FirebaseMessaging
import GoogleSignIn
import FirebaseCore
import ComposeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {

  var window: UIWindow?

  lazy var deeplinkHandler = Dependencies.shared.deepLinkHandler

  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    FirebaseApp.configure()

    UNUserNotificationCenter.current().delegate = self

    DispatchQueue.main.async {
      application.registerForRemoteNotifications()
    }

    // Создаем UIWindow и устанавливаем корневой view controller
    let window = UIWindow(frame: UIScreen.main.bounds)
    let contentViewController = MainViewControllerKt.mainViewController(
      iosDiHelper: Dependencies.shared.sharedDi
    )
    window.rootViewController = contentViewController
    self.window = window
    window.makeKeyAndVisible()

    Dependencies.shared.billingDelegate.listenForTransactionUpdates()

    if let userActDic = launchOptions?[.userActivityDictionary] as? [String: Any],
       let auserActivity = userActDic["UIApplicationLaunchOptionsUserActivityKey"] as? NSUserActivity {
      let urlString = auserActivity.webpageURL?.absoluteString ?? ""
      deeplinkHandler.handleDeeplink(url: urlString)
    }
    return true
  }

  func application(
    _ app: UIApplication,
    open url: URL,
    options: [UIApplication.OpenURLOptionsKey: Any] = [:]
  ) -> Bool {
    deeplinkHandler.handleDeeplink(url: url.absoluteString)
    return GIDSignIn.sharedInstance.handle(url)
  }

  // MARK: - Universal Links
  func application(_ application: UIApplication,
                   continue userActivity: NSUserActivity,
                   restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
    if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
       let url = userActivity.webpageURL {
      deeplinkHandler.handleDeeplink(url: url.absoluteString)
    }
    return true
  }

  // MARK: - APNS
  func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    Messaging.messaging().apnsToken = deviceToken

    // Optionally, fetch FCM token after setting the APNs token
    Messaging.messaging().token { token, error in
      if let error = error {
        print("Error fetching FCM token: \(error)")
      } else if let token = token {
        print("FCM token: \(token)")
        // Use the FCM token
      }
    }
  }

  // MARK: - Shortcuts
  func application(
    _ application: UIApplication,
    performActionFor shortcutItem: UIApplicationShortcutItem,
    completionHandler: @escaping (Bool) -> Void
  ) {
    if let userInfo = shortcutItem.userInfo,
       let value = userInfo["group_deeplink"] as? String?,
       let deeplink = value {
      deeplinkHandler.handleDeeplink(url: deeplink)
    }
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
