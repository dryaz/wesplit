import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
  var deeplink: String = ""
  func makeUIViewController(context: Context) -> UIViewController {
    MainViewControllerKt.mainViewController(
      iosDiHelper: IosDiHelper(
        loginDelegate: PlatformLoginDelegate(),
        deeplink: deeplink
      )
    )
  }
  
  func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
  var deeplink: String = ""
  var body: some View {
    ComposeView(deeplink: deeplink).ignoresSafeArea(.keyboard) // Compose has own keyboard handler
  }
}



