//
//  PhotoCell.swift
//  PhotoShare
//
//  Created by Amit Khoth on 5/25/16.
//  Copyright © 2016 Samsung. All rights reserved.
//

import UIKit
import AssetsLibrary

class PhotoCell: UICollectionViewCell {
    
  
//    private static var __once: () = {
//            Static.instance = ALAssetsLibrary()
//        }()
    
  
    @IBOutlet var mImageView: UIImageView!
 

    static var defaultAssetsLibrary = ALAssetsLibrary()
    
//    class var defaultAssetsLibrary : ALAssetsLibrary {
//        struct Static {
//            static var onceToken : Int = 0
//            static var instance : ALAssetsLibrary? = nil
//        }
//        _ = PhotoCell.__once
//        return Static.instance!
//    }

    
    func loadImage(_ url :URL) {
        let assetLib = PhotoCell.defaultAssetsLibrary
        //DispatchQueue.global(priority: DispatchQueue.GlobalQueuePriority.default).async(execute: {
        DispatchQueue.global().async(execute: {
            [unowned self] in
            assetLib.asset(for: url, resultBlock: {
                [unowned self]
                (asset: ALAsset?) in
                if asset != nil {
                    let iref = asset?.aspectRatioThumbnail().takeUnretainedValue()
                    let image = UIImage(cgImage: iref!)
                    DispatchQueue.main.async(execute: {
                        self.mImageView!.image = image
                    })
                }
                }, failureBlock: { (error: Error?) in
            })
            })
    }
    
//    func loadImage(_ url :URL) {
//        let assetLib = PhotoCell.defaultAssetsLibrary
//        DispatchQueue.global().async(execute: {
//            [unowned self] in
//            assetLib.asset(for: url, resultBlock: {
//                [unowned self]
//                (asset: ALAsset?) in
//                if asset != nil {
//                    let iref = asset!.aspectRatioThumbnail().takeUnretainedValue()
//                    let image = UIImage(cgImage: iref)
//                    DispatchQueue.main.async(execute: {
//                        self.mImageView!.image = image
//                    })
//                }
//                }, failureBlock: { (error: Error?) in
//            })
//            })
//    }

}
