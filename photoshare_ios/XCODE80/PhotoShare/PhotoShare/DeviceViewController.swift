//
//  DeviceViewController.swift
//  PhotoShare
//
//  Created by Amit Khoth on 5/3/16.
//  Copyright © 2016 Samsung. All rights reserved.
//

import Foundation
import UIKit
import SmartView
class  DeviceViewController: UIViewController,UIPopoverPresentationControllerDelegate{
    
    
   // @IBOutlet weak var mTableView: UITableView!
    
    
    @IBOutlet weak var  deviceListButton: UIButton!
    
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
            case .readyToConnect:
                let castImage = UIImage(named: "cast_off.png")?.imageWithRenderingMode(.AlwaysTemplate)
                deviceListButton.setImage(castImage, forState: UIControlState.Normal)
                deviceListButton.tintColor = UIColor.blackColor()
                deviceListButton.enabled = true
            case .connecting:
                deviceListButton.imageView!.animationImages = [UIImage(named: "cast_on0.png")!.imageWithRenderingMode(.AlwaysTemplate) ,UIImage(named: "cast_on1.png")!.imageWithRenderingMode(.AlwaysTemplate), UIImage(named: "cast_on2.png")!.imageWithRenderingMode(.AlwaysTemplate), UIImage(named: "cast_on1.png")!.imageWithRenderingMode(.AlwaysTemplate)]
                deviceListButton.imageView!.animationDuration = 2
                deviceListButton.imageView!.startAnimating()
                deviceListButton.tintColor = UIColor.blackColor()
            case .connected:
                if deviceListButton.imageView!.isAnimating() {
                    deviceListButton.imageView!.stopAnimating()
                }
                let castImage = UIImage(named: "cast_on.png")!.imageWithRenderingMode(.AlwaysTemplate)
                deviceListButton.setImage(castImage, forState: UIControlState.Normal)
                deviceListButton.tintColor = UIColor.blueColor()
                deviceListButton.enabled = true
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
    
    
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self.deviceListButton, selector: "statusDidChange:", name: "CastStatusDidChange", object: nil)

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
    
//    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
//        if PhotoShareController.sharedInstance.search.isSearching {
//            return PhotoShareController.sharedInstance.services.count
//        } else {
//            return 1
//        }
//    }
//    
//    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
//        let cell = tableView.dequeueReusableCellWithIdentifier("DeviceNameCell", forIndexPath: indexPath)
//        
//
//        
//        if let namelabel = cell.viewWithTag(100) as? UILabel{
//            namelabel.text = PhotoShareController.sharedInstance.services[indexPath.row].name
//        }
//        
//        return cell
//    }
    

    
    
  
    // MARK: - Table view data source
    
  //  func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
   //     return 1
  //  }
    
 
}