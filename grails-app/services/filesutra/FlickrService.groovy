package filesutra

import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class FlickrService {

    def callAPI(Closure c, Access access) {
        def accessInfo = JSON.parse(access.accessInfo)
        c(accessInfo.accessToken)
    }

    def listItems(String folderId, String after, Access access, flickrAuth) {
        callAPI({ accessToken->
            return Flickr.listItems(folderId, after, access, flickrAuth)
        }, access)
    }

    def getDownloadUrlConnection(String fileId, Access access) {
        callAPI({ accessToken ->
            return Flickr.getDownloadUrlConnection(fileId, accessToken)
        }, access)
    }

    java.io.File downloadFile(input, Access access) {
       return Flickr.downloadFile(input, access); 
    }
}
