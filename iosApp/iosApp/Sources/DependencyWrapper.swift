//
//  DependencyWrapper.swift
//  iosApp
//
//  Created by Dmitry Ryazantsyev on 16.10.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import ComposeApp
import Foundation
import SwiftUI

class Dependencies {
  static let shared = Dependencies()

  lazy var loginDelegate: LoginIosNativeDelegate = { sharedDi.loginDelegate }()
  lazy var deepLinkHandler: DeepLinkHandler = { sharedDi.deepLinkHandler }()
  lazy var shareDelegate: ShareDelegate = { sharedDi.shareDelegate }()
  lazy var billingRepositoryController: BillingIosRepositoryController = { sharedDi.diHolder().billingRepository }()

  let sharedDi: IosDiHelper = IosDiHelper(
    loginDelegate: PlatformLoginDelegate(),
    deepLinkHandler: DeepLinkHandler(),
    shareDelegate: PlatformShareDelegate(),
    billingDelegate: PlatformBillingDelegate()
  )
}
