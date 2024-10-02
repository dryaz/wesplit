//
//  PlatformLoginDelegate.swift
//  iosApp
//
//  Created by Dmitry Ryazantsyev on 24.09.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import ComposeApp
import Foundation
import UIKit

class PlatformShareDelegate: ShareDelegate {
    func supportPlatformSharing() -> Bool {
        return true
    }

    func share(data: ShareData) {
        if let linkData = data as? ShareDataLink {
            shareLink(linkData.value)
        }
    }
  
    func open(data: ShareData) {
        if let linkData = data as? ShareDataLink {
          UIApplication.shared.open(URL(string: linkData.value)!, options: [:], completionHandler: nil)
        }
    }

    private func shareLink(_ link: String) {
        DispatchQueue.main.async {
            let items = [link]
            let activityVC = UIActivityViewController(activityItems: items, applicationActivities: nil)

            // For iPad support
            if let popoverController = activityVC.popoverPresentationController {
                popoverController.sourceView = UIApplication.shared.windows.first?.rootViewController?.view
                popoverController.sourceRect = CGRect(x: UIScreen.main.bounds.midX, y: UIScreen.main.bounds.midY, width: 0, height: 0)
                popoverController.permittedArrowDirections = []
            }

            if let topVC = UIApplication.topViewController() {
                topVC.present(activityVC, animated: true, completion: nil)
            }
        }
    }
}
