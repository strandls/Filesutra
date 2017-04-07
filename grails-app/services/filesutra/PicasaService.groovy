package filesutra

import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class PicasaService {

    def callAPI(Closure c, Access access) {
        def accessInfo = JSON.parse(access.accessInfo)
        try {
            c(accessInfo.accessToken)
        } catch (e) {
            e.printStackTrace();
            if (e.hasProperty("response") && e.response?.status == 403) {
                println "REFRESH TOKEN SERVICE"
                accessInfo.accessToken = Picasa.refreshToken(accessInfo.refreshToken)
                access.accessInfo = Utils.jsonToString(accessInfo)
                access.save(flush: true, failOnError: true)
                c(accessInfo.accessToken)
            } else {
               throw e
            }
        }
    }

    def listItems(String folderId, String afterVal, Access access) {
        callAPI({ accessToken->
            return Picasa.listItems(folderId, afterVal, accessToken)
        }, access)
    }

    def getDownloadUrlConnection(String fileId, Access access) {
        callAPI({ accessToken ->
            return Picasa.getDownloadUrlConnection(fileId, accessToken)
        }, access)
    }
}
