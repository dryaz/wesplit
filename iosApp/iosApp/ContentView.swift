import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
  var deeplinkHandler: DeepLinkHandler
  
  func makeUIViewController(context: Context) -> UIViewController {
    MainViewControllerKt.mainViewController(
      iosDiHelper: IosDiHelper(
        loginDelegate: PlatformLoginDelegate(),
        deepLinkHandler: deeplinkHandler,
        shareDelegate: PlatformShareDelegate()
      )
    )
  }
  
  func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
  var deeplinkHandler: DeepLinkHandler
  
  var body: some View {
    ComposeView(deeplinkHandler: deeplinkHandler).ignoresSafeArea() // Compose has own keyboard handler
  }
}



