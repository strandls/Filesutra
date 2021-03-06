package filesutra

import grails.util.Holders
import groovyx.net.http.RESTClient
import java.io.File;
import static groovyx.net.http.ContentType.URLENC
import org.springframework.social.facebook.api.Facebook as FB;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import grails.converters.JSON
import org.springframework.social.facebook.api.ImageType;

/**
 * Created by karthik on 10/05/16.
 */
class Facebook {

    def static grailsApplication = Holders.getGrailsApplication()

    private static final String CLIENT_ID     = grailsApplication.config.auth.facebook.CLIENT_ID
    private static final String CLIENT_SECRET = grailsApplication.config.auth.facebook.CLIENT_SECRET
    private static final String REDIRECT_URI = grailsApplication.config.auth.facebook.REDIRECT_URI

    private static final String AUTH_URL = "https://www.facebook.com/dialog/oauth"
    private static final String API_URL = "https://graph.facebook.com"

    static def getLoginUrl() {
        def params = [
                response_type: "code",
                client_id    : CLIENT_ID,
                redirect_uri : REDIRECT_URI,
                scope        : "email,user_photos"
        ]
        def url = "$AUTH_URL?" + params.collect { k, v -> "$k=$v" }.join('&')
        return url
    }

    static def exchangeCode(String code) {
        def restClient = new RESTClient(API_URL)
        def resp = restClient.post(
                path: '/v2.8/oauth/access_token',
                body: [client_id   : CLIENT_ID, client_secret: CLIENT_SECRET,
                       redirect_uri: REDIRECT_URI, code: code],
                requestContentType: URLENC)
        return [
                accessToken: resp.data.access_token,
                //refreshToken: resp.data.refresh_token
        ]
    }

    static def getEmailId(String accessToken) {
        def restClient = new RESTClient(API_URL)
        restClient.headers.Authorization = "Bearer $accessToken"
        def resp = restClient.get(path: "/v2.8/me", params : [fields: "id,name,email"])
        if(resp.data.email){
            return resp.data.email;
        }else {
            return resp.data.name;
        }
        
    }

    static def listItems(String folderId, String afterToken, String accessToken) {
        def restClient = new RESTClient(API_URL)
        restClient.headers.Authorization = "Bearer $accessToken"
        if(folderId == "facebook"){
             def resp = restClient.get(path: "/v2.8/me/albums")
             return resp.data
        } else {
            def resp = restClient.get(path: "/v2.8/$folderId/photos", params : [fields: "name,icon,picture",after: afterToken])
            return resp.data
        }
    }

    static def getFile(String fileId, String accessToken) {
        def restClient = new RESTClient(API_URL)
        restClient.headers.Authorization = "Bearer $accessToken"
        def resp = restClient.get(path: "/v2.8/$fileId", params : [fields: "images"])
        return resp.data
    }

    static URLConnection getDownloadUrlConnection(String fileId, String accessToken) {
        def file = getFile(fileId, accessToken)
        String contentUrl = file.images[0]['source'];
        URL url = new URL(contentUrl)

        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Authorization", 'Bearer ' + accessToken);
        connection.connect()
        return connection
    }

    static def refreshToken(String refreshToken) {
        def restClient = new RESTClient(API_URL)
        def resp = restClient.post(
                path: '/v2.8/oauth/access_token',
                body: [client_id   : CLIENT_ID, client_secret: CLIENT_SECRET,
                       refresh_token: refreshToken, grant_type: 'refresh_token'],
                requestContentType: URLENC)
        return resp.data.access_token
    }

    static File downloadFile(input, Access access) {
        println input
        println access
        File file = new File(grailsApplication.config.fileOps.resources.rootDir+File.separator+input.fileId);
        //OutputStream out = new FileOutputStream(file);
        try {
        def accessInfo = JSON.parse(access.accessInfo)
        def facebook = new FacebookTemplate(accessInfo.accessToken, "Filesutra");
        byte[] image = facebook.mediaOperations().getPhotoImage(input.fileId,ImageType.NORMAL);
        file.setBytes(image);
        println file.getAbsolutePath();
        } catch(Exception e) {
            e.printStackTrace(); 
            return null;
        } 
        return file
   }


}
