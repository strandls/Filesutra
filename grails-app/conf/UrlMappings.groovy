class UrlMappings {

    static mappings = {

        "/api/files/google"               (controller: "FilesAPI", action: "googleFiles")
        "/api/files/facebook"             (controller: "FilesAPI", action: "facebookFiles")
        "/api/files/flickr"               (controller: "FilesAPI", action: "flickrFiles")
        "/api/files/photos"               (controller: "FilesAPI", action: "picasaFiles")
        "/api/files/dropbox"              (controller: "FilesAPI", action: "dropboxFiles")
        "/api/files/box"                  (controller: "FilesAPI", action: "boxFiles")
        "/api/files/onedrive"             (controller: "FilesAPI", action: "onedriveFiles")
        "/api/files/amazonclouddrive"     (controller: "FilesAPI", action: "amazonFiles")
        "/api/files/wikipedia"              (controller: "FilesAPI", action: "wikipediaFiles")

        "/api/import/google"                (controller: "FilesAPI", action: "importGoogleFile")
        "/api/import/facebook"              (controller: "FilesAPI", action: "importFacebookFile")
        "/api/import/flickr"              (controller: "FilesAPI", action: "importFlickrFile")   
        "/api/import/photos"              (controller: "FilesAPI", action: "importPicasaFile")      
        "/api/import/box"                   (controller: "FilesAPI", action: "importBoxFile")
        "/api/import/dropbox"               (controller: "FilesAPI", action: "importDropboxFile")
        "/api/import/onedrive"              (controller: "FilesAPI", action: "importOnedriveFile")
        "/api/import/amazonclouddrive"      (controller: "FilesAPI", action: "importAmazonFile")
        "/api/import/wikipedia"              (controller: "FilesAPI", action: "importWikipediaFile")

        "/download/$id"         (controller: "File", action: "downloadFile")

        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: 'Picker', action: "importFiles")
        "500"(view: '/error')
    }
}
