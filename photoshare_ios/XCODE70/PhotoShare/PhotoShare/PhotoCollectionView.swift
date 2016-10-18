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
            if deviceListButton.imageView!.isAnimating() {
                deviceListButton.imageView!.stopAnimating()
            }
            
            
            switch castStatus {
            case .notReady:
                let castImage = UIImage(named: "cast_off.png")?.imageWithRenderingMode(.AlwaysTemplate)
                deviceListButton.setImage(castImage, forState: UIControlState.Normal)
                deviceListButton.tintColor = UIColor.blackColor()
                deviceListButton.enabled = false
                mDisConnectTvButton.setTitleColor(UIColor.grayColor(),forState: .Normal)
                mDisConnectTvButton.enabled = false
            case .readyToConnect:
                let castImage = UIImage(named: "cast_off.png")?.imageWithRenderingMode(.AlwaysTemplate)
                deviceListButton.setImage(castImage, forState: UIControlState.Normal)
                deviceListButton.tintColor = UIColor.blackColor()
                deviceListButton.enabled = true
                deviceListButton.tintColor = UIColor.grayColor()
                 mDisConnectTvButton.setTitleColor(UIColor.grayColor(),forState: .Normal)
                mDisConnectTvButton.enabled = false
            case .connecting:
                deviceListButton.imageView!.animationImages = [UIImage(named: "cast_on0.png")!.imageWithRenderingMode(.AlwaysTemplate) ,UIImage(named: "cast_on1.png")!.imageWithRenderingMode(.AlwaysTemplate), UIImage(named: "cast_on2.png")!.imageWithRenderingMode(.AlwaysTemplate), UIImage(named: "cast_on1.png")!.imageWithRenderingMode(.AlwaysTemplate)]
                deviceListButton.imageView!.animationDuration = 2
                deviceListButton.imageView!.startAnimating()
                mDisConnectTvButton.setTitleColor(UIColor.grayColor(),forState: .Normal)
                mDisConnectTvButton.enabled = false
                
            case .connected:
                if deviceListButton.imageView!.isAnimating() {
                    deviceListButton.imageView!.stopAnimating()
                }
                let castImage = UIImage(named: "cast_on.png")!.imageWithRenderingMode(.AlwaysTemplate)
                deviceListButton.setImage(castImage, forState: UIControlState.Normal)
                deviceListButton.tintColor = UIColor.blueColor()
                deviceListButton.enabled = true
                mDisConnectTvButton.setTitleColor(UIColor.blueColor(),forState: .Normal)
                mDisConnectTvButton.enabled = true

            }
        }
        
    }
    
    
    func statusDidChange(notification: NSNotification!) {
        let status = notification.userInfo?["status"] as! NSString
        self.castStatus = CastStatus(rawValue: status as String)!
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    
  
    var objects = NSMutableArray()
    var gridStyle = true
    let assets: NSMutableArray = NSMutableArray()
    
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
    
    override func awakeFromNib() {
        super.awakeFromNib()
        if UIDevice.currentDevice().userInterfaceIdiom == UIUserInterfaceIdiom.Phone {
            (self.collectionView!.collectionViewLayout as! UICollectionViewFlowLayout).itemSize = CGSizeMake(100, 100)
        } else {
            (self.collectionView!.collectionViewLayout as! UICollectionViewFlowLayout).itemSize = CGSizeMake(140, 140)
        }
        
    }
   
    override func viewDidLoad() {
        
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "statusDidChange:", name: "CastStatusDidChange", object: nil)
        
       
        automaticallyAdjustsScrollViewInsets = true
        collectionView!.pagingEnabled = true
        collectionView!.collectionViewLayout.invalidateLayout()
        
        loadAlbums()
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
  
    
    @IBAction func disConnectTvTouchUp(sender: AnyObject) {
        PhotoShareController.sharedInstance.disconnect()
    }
    
    @IBAction func DeviceListButtonTouchDown(sender: AnyObject) {
        let deviceList = DeviceListController(style: UITableViewStyle.Plain)
        presentPopover(deviceList)
    }
    
    func presentPopover(viewController: UIViewController) {
        viewController.preferredContentSize = CGSize(width: 320, height: 186)
        viewController.modalPresentationStyle = UIModalPresentationStyle.Popover
        let presentationController = viewController.popoverPresentationController
        presentationController!.sourceView = deviceListButton
        presentationController!.sourceRect = deviceListButton.bounds
        presentationController!.delegate = self
        presentViewController(viewController, animated: false, completion: {})
    }
    
    
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        // Return no adaptive presentation style, use default presentation behaviour
        return .None
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

    override func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }


    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of items
        return assets.count
    }

    override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier(reuseIdentifier, forIndexPath: indexPath) as!PhotoCell
        let media : Media = self.assets[indexPath.row] as! Media
        cell.loadImage(media.URL)
        return cell;
    }
    override func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier(reuseIdentifier, forIndexPath: indexPath)
         cell.layer.borderWidth = 2.0
         cell.layer.borderColor = UIColor.grayColor().CGColor
        
        let media : Media = self.assets[indexPath.row] as! Media
        let url = media.URL
        
        PhotoShareController.sharedInstance.castImage(url)
        dismissViewControllerAnimated(true, completion: nil) 
    
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        return CGSize(width: 100, height: 100)
    }
    
    
    func loadAlbums(){
        let assetLib = PhotoCollectionView.defaultAssetsLibrary
        
        let enumBlock: (group: ALAssetsGroup?, stop: UnsafeMutablePointer<ObjCBool>) -> Void = {
            [unowned self]
            (group: ALAssetsGroup?, stop: UnsafeMutablePointer<ObjCBool>) in
            if group != nil && group!.numberOfAssets() > 0 {
                group!.setAssetsFilter(ALAssetsFilter.allPhotos())
                
                
                group!.enumerateAssetsUsingBlock({ ( asset, index, stop) -> Void in
                    if((asset) != nil)
                    {
                        let alAssetRapresentation: ALAssetRepresentation = asset.defaultRepresentation()

                        let media : Media = Media.init(url: alAssetRapresentation.url())
                        self.assets.addObject(media)

                    }
                })
                

            } else if group == nil {
                dispatch_async(dispatch_get_main_queue(), {
                    self.collectionView!.reloadData()
                })
            }
        }
        
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            assetLib.enumerateGroupsWithTypes(ALAssetsGroupType(ALAssetsGroupSavedPhotos), usingBlock: enumBlock, failureBlock: { (error: NSError!) in
                
            })
        })
    }

    
    // MARK: UICollectionViewDelegate

    /*
    // Uncomment this method to specify if the specified item should be highlighted during tracking
    override func collectionView(collectionView: UICollectionView, shouldHighlightItemAtIndexPath indexPath: NSIndexPath) -> Bool {
        return true
    }
    */

    /*
    // Uncomment this method to specify if the specified item should be selected
    override func collectionView(collectionView: UICollectionView, shouldSelectItemAtIndexPath indexPath: NSIndexPath) -> Bool {
        return true
    }
    */

    /*
    // Uncomment these methods to specify if an action menu should be displayed for the specified item, and react to actions performed on the item
    override func collectionView(collectionView: UICollectionView, shouldShowMenuForItemAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }

    override func collectionView(collectionView: UICollectionView, canPerformAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) -> Bool {
        return false
    }

    override func collectionView(collectionView: UICollectionView, performAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) {
    
    }
    */

}
