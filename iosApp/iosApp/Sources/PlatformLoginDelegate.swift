//
//  PlatformLoginDelegate.swift
//  iosApp
//
//  Created by Dmitry Ryazantsyev on 24.09.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import ComposeApp
import Foundation
import FirebaseCore
import GoogleSignIn
import FirebaseAuth
import UIKit

class PlatformLoginDelegate: LoginIosNativeDelegate {
  
  func googleLogin(onCredentialsReceived: @escaping (GooleCredentials?) -> Void) {
    guard let clientID = FirebaseApp.app()?.options.clientID else {
      print("Missing client ID")
      onCredentialsReceived(nil)
      return
    }
    
    let config = GIDConfiguration(clientID: clientID)
    GIDSignIn.sharedInstance.configuration = config
    
    guard let rootViewController = PlatformLoginDelegate.getRootViewController() else {
      print("No root view controller")
      onCredentialsReceived(nil)
      return
    }
    
    GIDSignIn.sharedInstance.signIn(
      withPresenting: rootViewController
    ) { result, error in
      if let error = error {
        print("Error during Google Sign-In: \(error.localizedDescription)")
        onCredentialsReceived(nil)
        return
      }
      
      guard
        let accessToken = result?.user.accessToken.tokenString,
        let idToken = result?.user.idToken?.tokenString
      else {
        print("Missing authentication object")
        onCredentialsReceived(nil)
        return
      }
      
      onCredentialsReceived(GooleCredentials(accessToken: accessToken, idToken: idToken))
    }
  }
  
  private static func getRootViewController() -> UIViewController? {
    // Supports apps with multiple scenes (iOS 13+)
    if #available(iOS 13.0, *) {
      return UIApplication.shared.connectedScenes
        .filter { $0.activationState == .foregroundActive }
        .compactMap { $0 as? UIWindowScene }
        .first?.windows
        .filter { $0.isKeyWindow }
        .first?.rootViewController
    } else {
      // Fallback on earlier versions
      return UIApplication.shared.keyWindow?.rootViewController
    }
  }
}
