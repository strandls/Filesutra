package filesutra

import grails.converters.JSON

class PickerController {

    def importFiles() {
        def connectedApps = []
        if (session.googleAccessId) {
            connectedApps.push("Google")
        }
        if (session.facebookAccessId) {
            connectedApps.push("Facebook")
        }
        if (session.flickrAccessId) {
            connectedApps.push("Flickr")
        }
        if (session.picasaAccessId) {
            connectedApps.push("Photos")
        }
        if (session.boxAccessId) {
            connectedApps.push("Box")
        }
        if (session.dropboxAccessId) {
            connectedApps.push("Dropbox")
        }
        if (session.onedriveAccessId) {
            connectedApps.push("OneDrive")
        }
        if (session.amazonAccessId) {
            connectedApps.push("AmazonCloudDrive")
        }
        if (session.wikipediaAccessId) {
            connectedApps.push("Wikimedia")
        }
        def appSettings = [
                connectedApps : connectedApps
        ]
        [appSettings: appSettings as JSON]
    }

}
