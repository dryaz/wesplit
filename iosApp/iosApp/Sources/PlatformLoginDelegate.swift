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
import AuthenticationServices
import CryptoKit

class PlatformLoginDelegate: NSObject, LoginIosNativeDelegate {
  private var onAppleCredentialsReceived: ((AppleCredentials?) -> Void)?
  private var currentNonce: String?
  
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
  
  func appleLogin(onCredentialsReceived: @escaping (AppleCredentials?) -> Void) {
          self.onAppleCredentialsReceived = onCredentialsReceived

          // Generate nonce for Firebase
          let nonce = randomNonceString()
          currentNonce = nonce

          let appleIDProvider = ASAuthorizationAppleIDProvider()
          let request = appleIDProvider.createRequest()
          request.requestedScopes = [.fullName, .email]
          request.nonce = sha256(nonce)

          let authController = ASAuthorizationController(authorizationRequests: [request])
          authController.delegate = self
          authController.presentationContextProvider = self
          authController.performRequests()
      }

  func randomNonceString(length: Int = 32) -> String {
      precondition(length > 0)
      let charset: Array<Character> =
          Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")

      var result = ""
      var remainingLength = length

      while remainingLength > 0 {
          let randoms = (0..<16).map { _ in UInt8.random(in: 0...255) }
          randoms.forEach { random in
              if remainingLength == 0 {
                  return
              }
              if random < charset.count {
                  result.append(charset[Int(random)])
                  remainingLength -= 1
              }
          }
      }

      return result
  }

  func sha256(_ input: String) -> String {
      let inputData = Data(input.utf8)
      let hashedData = SHA256.hash(data: inputData)
      let hashString = hashedData.compactMap { String(format: "%02x", $0) }.joined()
      return hashString
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

extension PlatformLoginDelegate: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        // Handle successful authorization
        guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let identityTokenData = appleIDCredential.identityToken,
              let identityToken = String(data: identityTokenData, encoding: .utf8),
              let nonce = currentNonce else {
            onAppleCredentialsReceived?(nil)
            return
        }
      
      let fullNameString: String? = {
          if let fullNameComponents = appleIDCredential.fullName {
              return PersonNameComponentsFormatter().string(from: fullNameComponents)
          } else {
              return nil
          }
      }()

      let credentials = AppleCredentials(idToken: identityToken, nonce: nonce, name: fullNameString)
      onAppleCredentialsReceived?(credentials)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        // Handle error
        print("Sign in with Apple failed: \(error.localizedDescription)")
        onAppleCredentialsReceived?(nil)
    }
}

extension PlatformLoginDelegate: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        // Provide the window to present the sign-in UI
        return UIApplication.shared.windows.first { $0.isKeyWindow }!
    }
}
