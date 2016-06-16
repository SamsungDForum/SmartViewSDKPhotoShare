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
    
  
    @IBOutlet var mImageView: UIImageView!
 

    class var defaultAssetsLibrary : ALAssetsLibrary {
        struct Static {
            static var onceToken : dispatch_once_t = 0
            static var instance : ALAssetsLibrary? = nil
        }
        dispatch_once(&Static.onceToken) {
            Static.instance = ALAssetsLibrary()
        }
        return Static.instance!
    }

    
    func loadImage(url :NSURL) {
        let assetLib = PhotoCell.defaultAssetsLibrary
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            [unowned self] in
            assetLib.assetForURL(url, resultBlock: {
                [unowned self]
                (asset: ALAsset!) in
                if asset != nil {
                    let iref = asset.aspectRatioThumbnail().takeUnretainedValue()
                    let image = UIImage(CGImage: iref)
                    dispatch_async(dispatch_get_main_queue(), {
                        self.mImageView!.image = image
                    })
                }
                }, failureBlock: { (error: NSError!) in
            })
            })
    }

}
