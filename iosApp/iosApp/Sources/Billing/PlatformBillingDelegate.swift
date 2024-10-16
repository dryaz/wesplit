//
//  PlatformBillingDelegate.swift
//  iosApp
//
//  Created by Dmitry Ryazantsyev on 16.10.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import ComposeApp
import Foundation
import StoreKit

let productIdentifiers: Set<String> = [
  "week",
  "month",
  "year"
]

@available(iOS 15.0, *)
class PlatformBillingDelegate: BillingIosNativeDelegate {
  func requestPricingUpdate() {
    Task {
      await fetchProductInfo()
    }
  }
  
  private var products: [String: Product] = [:]

  func subscribe(period: ModelSubscription.Period) {
    Task {
      await purchaseSubscription(period: period)
    }
  }
  
  func isBillingSupported() -> Bool {
    return true
  }
}

@available(iOS 15.0, *)
extension PlatformBillingDelegate {
  func fetchProductInfo() async {
    do {
      let storeProducts = try await Product.products(for: productIdentifiers)
      var subscriptions = [ModelSubscription]()
      
      for product in storeProducts {
        products[product.id] =   product
        
        guard let period = mapProductToPeriod(product: product) else { continue }
        
        let subscription = ModelSubscription(
          title: product.displayName,
          description: product.description,
          formattedPrice: product.displayPrice,
          monthlyPrice: calculateMonthlyPrice(product: product),
          period: period
        )
        subscriptions.append(subscription)
      }
      print("!@# get subs: \(subscriptions)")
      print("!@# controller: \(Dependencies.shared.billingRepositoryController)")
      Dependencies.shared.billingRepositoryController.update(pricingResult: subscriptions)
    } catch {
      Dependencies.shared.billingRepositoryController.onPurchaseError()
    }
  }
}

@available(iOS 15.0, *)
extension PlatformBillingDelegate {
  func mapProductToPeriod(product: Product) -> ModelSubscription.Period? {
    switch product.id {
    case "week":
      return .week
    case "month":
      return .month
    case "year":
      return .year
    default:
      return nil
    }
  }
  
  func calculateMonthlyPrice(product: Product) -> ModelAmount {
    // Ensure the product has subscription information
    guard let subscription = product.subscription else {
      // If it's not a subscription product, return 0 or handle accordingly
      return ModelAmount(value: 0.0, currencyCode: product.priceFormatStyle.currencyCode)
    }
    
    // Total price of the product
    let totalPrice = product.price as NSDecimalNumber
    // Subscription period unit (day, week, month, year)
    let periodUnit = subscription.subscriptionPeriod.unit
    // Number of units in the subscription period
    let periodCount = subscription.subscriptionPeriod.value
    
    var monthlyPrice: Double = 0.0
    
    switch periodUnit {
    case .day:
      // Assuming 30 days in a month
      let daysInMonth = 30.0
      let pricePerDay = totalPrice.doubleValue / Double(periodCount)
      monthlyPrice = pricePerDay * daysInMonth
    case .week:
      // Assuming 4 weeks in a month
      let weeksInMonth = 4.0
      let pricePerWeek = totalPrice.doubleValue / Double(periodCount)
      monthlyPrice = pricePerWeek * weeksInMonth
    case .month:
      // If it's per month, divide total price by number of months
      monthlyPrice = totalPrice.doubleValue / Double(periodCount)
    case .year:
      // Assuming 12 months in a year
      let monthsInYear = 12.0
      let pricePerYear = totalPrice.doubleValue / Double(periodCount)
      monthlyPrice = pricePerYear / monthsInYear
    @unknown default:
      // Handle any future cases
      monthlyPrice = totalPrice.doubleValue
    }
    
    return ModelAmount(value: monthlyPrice, currencyCode: product.priceFormatStyle.currencyCode)
  }
}

@available(iOS 15.0, *)
extension PlatformBillingDelegate {
  func purchaseSubscription(period: ModelSubscription.Period) async {
    guard let productID = productID(for: period),
          let product = products[productID] else {
      await MainActor.run {
        Dependencies.shared.billingRepositoryController.onPurchaseError()
      }
      return
    }
    
    do {
      let result = try await product.purchase()
      
      switch result {
      case .success(let verification):
        switch verification {
        case .verified(let transaction):
          await handleTransaction(transaction)
        case .unverified(_, let error):
          print("Transaction unverified: \(error.localizedDescription)")
          await MainActor.run {
            Dependencies.shared.billingRepositoryController.onPurchaseError()
          }
        }
      case .userCancelled:
        await MainActor.run {
          Dependencies.shared.billingRepositoryController.onPurchaseError()
        }
      case .pending:
        print("Purchase is pending approval")
      @unknown default:
        break
      }
    } catch {
      print("Purchase failed: \(error.localizedDescription)")
      await MainActor.run {
        Dependencies.shared.billingRepositoryController.onPurchaseError()
      }
    }
  }
}

@available(iOS 15.0, *)
extension PlatformBillingDelegate {
  func handleTransaction(_ transaction: Transaction) async {
    // Store originalTransactionId
    let originalTransactionId = String(transaction.originalID ?? transaction.id)
    
    // Notify KMM about purchase completion
    await MainActor.run {
      Dependencies.shared.billingRepositoryController.onPurchaseSuccess(purchaseId: originalTransactionId)
    
    }
    
    // Finish the transaction
    await transaction.finish()
  }
}

@available(iOS 15.0, *)
extension PlatformBillingDelegate {
  func productID(for period: ModelSubscription.Period) -> String? {
    switch period {
    case .week:
      return "week"
    case .month:
      return "month"
    case .year:
      return "year"
    default:
      return nil
    }
  }
}

@available(iOS 15.0, *)
extension PlatformBillingDelegate {
  func listenForTransactionUpdates() {
    Task {
      for await verificationResult in Transaction.updates {
        switch verificationResult {
        case .verified(let transaction):
          await handleTransaction(transaction)
        case .unverified(_, let error):
          print("Unverified transaction: \(error.localizedDescription)")
        }
      }
    }
  }
}
