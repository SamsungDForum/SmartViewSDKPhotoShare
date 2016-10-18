//
//  PhotoCollectionView.swift
//  PhotoShare
//
//  Created by Amit Khoth on 5/24/16.
//  Copyright © 2016 Samsung. All rights reserved.
//

import UIKit

import AssetsLibrary
private let reuseIdentifier = "collectionCell"
enum CastStatus: String {
    case notReady = "notReady"
    case readyToConnect = "readyToConnect"
    case connecting = "connecting"
    case connected = "connected"
}
class PhotoCollectionView: UICollectionViewController ,UIPopoverPresentationControllerDelegate,UICollectionViewDelegateFlowLayout {

    @IBOutlet weak var  deviceListButton: UIButton!
    
    @IBOutlet weak var mDisConnectTvButton: UIButton!
    
    var didFindServiceObserver: AnyObject? = nil
    
    var didRemoveServiceObserver: AnyObject? = nil
    
    var castStatus: CastStatus = CastStatus.notReady {
        didSet {
            if deviceListButton.imageView!.isAnimating {
                deviceListButton.imageView!.stopAnimating()
            }
            
            
            switch castStatus {
            case .notReady:
                let castImage = UIImage(named: "cast_off.png")?.withRenderingMode(.alwaysTemplate)
                deviceListButton.setImage(castImage, for: UIControlState())
                deviceListButton.tintColor = UIColor.black
                deviceListButton.isEnabled = false
                mDisConnectTvButton.setTitleColor(UIColor.gray,for: UIControlState())
                mDisConnectTvButton.isEnabled = false
            case .readyToConnect:
                let castImage = UIImage(named: "cast_off.png")?.withRenderingMode(.alwaysTemplate)
                deviceListButton.setImage(castImage, for: UIControlState())
                deviceListButton.tintColor = UIColor.black
                deviceListButton.isEnabled = true
                deviceListButton.tintColor = UIColor.gray
                 mDisConnectTvButton.setTitleColor(UIColor.gray,for: UIControlState())
                mDisConnectTvButton.isEnabled = false
            case .connecting:
                deviceListButton.imageView!.animationImages = [UIImage(named: "cast_on0.png")!.withRenderingMode(.alwaysTemplate) ,UIImage(named: "cast_on1.png")!.withRenderingMode(.alwaysTemplate), UIImage(named: "cast_on2.png")!.withRenderingMode(.alwaysTemplate), UIImage(named: "cast_on1.png")!.withRenderingMode(.alwaysTemplate)]
                deviceListButton.imageView!.animationDuration = 2
                deviceListButton.imageView!.startAnimating()
                mDisConnectTvButton.setTitleColor(UIColor.gray,for: UIControlState())
                mDisConnectTvButton.isEnabled = false
                
            case .connected:
                if deviceListButton.imageView!.isAnimating {
                    deviceListButton.imageView!.stopAnimating()
                }
                let castImage = UIImage(named: "cast_on.png")!.withRenderingMode(.alwaysTemplate)
                deviceListButton.setImage(castImage, for: UIControlState())
                deviceListButton.tintColor = UIColor.blue
                deviceListButton.isEnabled = true
                mDisConnectTvButton.setTitleColor(UIColor.blue,for: UIControlState())
                mDisConnectTvButton.isEnabled = true

            }
        }
        
    }
    
    
    func statusDidChange(_ notification: Notification!) {
        let status = notification.userInfo?["status"] as! NSString
        self.castStatus = CastStatus(rawValue: status as String)!
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    
  
    var objects = NSMutableArray()
    var gridStyle = true
    let assets: NSMutableArray = NSMutableArray()
    
    static var defaultAssetsLibrary = ALAssetsLibrary()
    
    override func awakeFromNib() {
        super.awakeFromNib()
        if UIDevice.current.userInterfaceIdiom == UIUserInterfaceIdiom.phone {
            (self.collectionView!.collectionViewLayout as! UICollectionViewFlowLayout).itemSize = CGSize(width: 100, height: 100)
        } else {
            (self.collectionView!.collectionViewLayout as! UICollectionViewFlowLayout).itemSize = CGSize(width: 140, height: 140)
        }
        
    }
   
    override func viewDidLoad() {
        
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(PhotoCollectionView.statusDidChange(_:)), name: NSNotification.Name(rawValue: "CastStatusDidChange"), object: nil)
        
       
        automaticallyAdjustsScrollViewInsets = true
        collectionView!.isPagingEnabled = true
        collectionView!.collectionViewLayout.invalidateLayout()
        
        loadAlbums()
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
  
    
    @IBAction func disConnectTvTouchUp(_ sender: AnyObject) {
        PhotoShareController.sharedInstance.disconnect()
    }
    
    @IBAction func DeviceListButtonTouchDown(_ sender: AnyObject) {
        let deviceList = DeviceListController(style: UITableViewStyle.plain)
        presentPopover(deviceList)
    }
    
    func presentPopover(_ viewController: UIViewController) {
        viewController.preferredContentSize = CGSize(width: 320, height: 186)
        viewController.modalPresentationStyle = UIModalPresentationStyle.popover
        let presentationController = viewController.popoverPresentationController
        presentationController!.sourceView = deviceListButton
        presentationController!.sourceRect = deviceListButton.bounds
        presentationController!.delegate = self
        present(viewController, animated: false, completion: {})
    }
    
    
    
    func adaptivePresentationStyle(for controller: UIPresentationController) -> UIModalPresentationStyle {
        // Return no adaptive presentation style, use default presentation behaviour
        return .none
    }
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using [segue destinationViewController].
        // Pass the selected object to the new view controller.
    }
    */

    // MARK: UICollectionViewDataSource

    override func numberOfSections(in collectionView: UICollectionView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }


    override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of items
        return assets.count
    }

    override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath) as!PhotoCell
        let media : Media = self.assets[(indexPath as NSIndexPath).row] as! Media
        cell.loadImage(media.URL)
        return cell;
    }
    override func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath)
         cell.layer.borderWidth = 2.0
         cell.layer.borderColor = UIColor.gray.cgColor
        
        let media : Media = self.assets[(indexPath as NSIndexPath).row] as! Media
        let url = media.URL
        
        PhotoShareController.sharedInstance.castImage(url!)
        dismiss(animated: true, completion: nil) 
    
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: 100, height: 100)
    }
    
    func loadAlbums(){
        let assetLib = PhotoCollectionView.defaultAssetsLibrary
        
        let enumBlock: (_ group: ALAssetsGroup?, _ stop: UnsafeMutablePointer<ObjCBool>?) -> Void = {
            [unowned self]
            (group: ALAssetsGroup?, stop: UnsafeMutablePointer<ObjCBool>?) in
            if group != nil && group!.numberOfAssets() > 0 {
                group!.setAssetsFilter(ALAssetsFilter.allPhotos())
                
                
                group!.enumerateAssets({ ( asset, index, stop) -> Void in
                    if((asset) != nil)
                    {
                        let alAssetRapresentation: ALAssetRepresentation = asset!.defaultRepresentation()
                        
                        let media : Media = Media.init(url: alAssetRapresentation.url())
                        self.assets.add(media)
                        
                    }
                })
                
                
            } else if group == nil {
                DispatchQueue.main.async(execute: {
                    self.collectionView!.reloadData()
                })
            }
        }
        
        
        DispatchQueue.global(qos: .background).async(execute: {
            assetLib.enumerateGroups(withTypes: ALAssetsGroupType(ALAssetsGroupSavedPhotos), using: enumBlock, failureBlock: { (error: Error?) in
                
            })
        })
    }

}
