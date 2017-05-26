package filesutra

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.util.IOUtilities;
import com.flickr4java.flickr.Flickr as FlickrJ;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;
import com.flickr4java.flickr.photos.Extras;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.nio.file.Files;

import grails.util.Holders
import groovyx.net.http.RESTClient
import java.security.MessageDigest
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject;
import java.io.File;

import static groovyx.net.http.ContentType.URLENC

/**
 * Created by kartthik on 19/05/16.
 */
class Flickr {

    def static grailsApplication = Holders.getGrailsApplication()

    private static final String CLIENT_ID     = grailsApplication.config.auth.flickr.CLIENT_ID
    private static final String CLIENT_SECRET = grailsApplication.config.auth.flickr.CLIENT_SECRET
    private static final String REDIRECT_URI = grailsApplication.config.auth.flickr.REDIRECT_URI

    private static final String AUTH_URL = "https://flickr.com/services/auth"
    private static final String API_URL = "https://api.flickr.com"

    private static Map tokenSecretsCache = new HashMap();
    private static FlickrJ flickr;

    static FlickrJ getFlickrJ() {
        if(flickr == null) {
            flickr = new FlickrJ(CLIENT_ID, CLIENT_SECRET, new REST());
            FlickrJ.debugStream = true;
        }
        return flickr;
    }

     static def getLoginUrl() {
        /*
        def sig = getSignature();
        def params = [
                api_key    : CLIENT_ID,
                perms        : "read",
                api_sig      :sig
                ]
                def url = "$AUTH_URL?" + params.collect { k, v -> "$k=$v" }.join('&')
                return url
         */
        flickr = getFlickrJ();
        AuthInterface authInterface = flickr.getAuthInterface();

        Token token = authInterface.getRequestToken(REDIRECT_URI);
        tokenSecretsCache.put(token.getToken(), token.getSecret());
        System.out.println("token: " + token);

        String url = authInterface.getAuthorizationUrl(token, Permission.READ);
        return url;
    }

    static def exchangeCode(String oauth_token, String oauth_verifier) {
        /*
        def frobSig = getFrobSignature(code);
        def restClient = new RESTClient(API_URL)
        def resp = restClient.get(path: "/services/rest", params : [method: "flickr.auth.getToken",api_key: CLIENT_ID, frob: code, api_sig: frobSig])
        String val = resp.data;
        val = val.substring(0, val.length() - 5);
        return [
accessToken: val,
username:resp.data.auth.user.@username.text()
//refreshToken: resp.data.refresh_token
]
         */
        AuthInterface authInterface = getFlickrJ().getAuthInterface();

        //Token token = authInterface.getRequestToken(REDIRECT_URI);
        Token requestToken = authInterface.getAccessToken(new Token(oauth_token,tokenSecretsCache.get(oauth_token)), new Verifier(oauth_verifier));
        tokenSecretsCache.remove(oauth_token);
        System.out.println("Authentication success");

        Auth auth = authInterface.checkToken(requestToken);
        RequestContext.getRequestContext().setAuth(auth);
        return  [
        accessToken: requestToken.getToken(),
        username:auth.getUser().getId(),
        name:auth.getUser().getUsername(),
        auth:auth
        ]

    }

    static def listItems(String folderId, String after, Access access, Auth auth) {
        PhotosetsInterface pi = getFlickrJ().getPhotosetsInterface();
        PeopleInterface photoInt = getFlickrJ().getPeopleInterface();
        //PhotosInterface photoInt = getFlickrJ().getPhotosInterface();
        List allPhotos = [];
        List allPhotoSets =  [];
        int page = 1;
        try {
            page = Integer.parseInt(after);
        } catch(Exception e) {
            e.printStackTrace();
            page = 1;
        }
        int perPage = 25;

        String nsid = access.emailId;
        Iterator sets = pi.getList(nsid).getPhotosets().iterator();
        RequestContext.getRequestContext().setAuth(auth);
        while (sets.hasNext()) {
            Photoset set = (Photoset) sets.next();
            if(folderId.equals(set.getId())) {
                PhotoList photos = pi.getPhotos(set.getId(), perPage, page);
                allPhotos.addAll(photos);
                page++;
            } else {
                allPhotoSets << set;
            }
        }
        if(folderId.equals('untitled')) {
            try {
                Collection notInASet = new ArrayList();
                Set extras = new HashSet();
                extras.add(Extras.DATE_UPLOAD);
                extras.add(Extras.DATE_TAKEN);
                extras.add(Extras.LAST_UPDATE);
                //while (true) {
                    Collection nis = photoInt.getPhotos(nsid, null, null, null, null, null, null, null, extras, perPage, page);
                    //Collection nis = photoInt.getNotInSet(500, notInSetPage);
                    notInASet.addAll(nis);
        //            if (nis.size() < 500) {
        //                break;
        //            }
                    page++;
                //}
                allPhotos.addAll(notInASet);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return ['photoset':allPhotoSets, 'photo':allPhotos.sort{a,b-> b.datePosted<=>a.datePosted}, 'nextPage':page];
    }

/*
    private static String getSignature(){
        String claimedContent = CLIENT_SECRET+"api_key"+CLIENT_ID+"permsread";
        String hashFromContent = claimedContent.encodeAsMD5();
        return hashFromContent;
    }

    private static String getFrobSignature(String frobVal){
        String claimedContent = CLIENT_SECRET+"api_key"+CLIENT_ID+"frob"+frobVal+"methodflickr.auth.getToken";
        String hashFromContent = claimedContent.encodeAsMD5();
        return hashFromContent;
    }

    private static String getTokenSignature(String authToken, String method){
        String claimedContent = CLIENT_SECRET+"api_key"+CLIENT_ID+"auth_token"+authToken+"method"+method;
        String hashFromContent = claimedContent.encodeAsMD5();
        return hashFromContent;
    }

    static def getEmailId(String accessToken) {
        def frobSig = getTokenSignature(accessToken,"flickr.test.login");
        def restClient = new RESTClient(API_URL)
        def resp = restClient.get(path: "/services/rest", params : [method: "flickr.test.login",api_key: CLIENT_ID, auth_token: accessToken, api_sig: frobSig])
        String name = resp.data.user;      

        return name;
    }

    static def listItems(String folderId, String accessToken) {
      String newValue = accessToken+"extrasurl_mformatjson"
      def frobSig = getTokenSignature(newValue,"flickr.people.getPhotosnojsoncallback1per_page500user_idme");
      def restClient = new RESTClient(API_URL)
      def resp = restClient.get(path: "/services/rest/", params : [method: "flickr.people.getPhotos",api_key: CLIENT_ID, auth_token: accessToken, api_sig: frobSig,user_id:"me", format:"json",nojsoncallback:"1",extras:"url_m",per_page:"500"])
      return resp.data.photos.photo
}

    static def listItems(String folderId, String after, String accessToken) {

        def restClient = new RESTClient(API_URL)
        if(folderId == "flickr"){
            String newValue = accessToken+"formatjson"
            def frobSig = getTokenSignature(newValue,"flickr.photosets.getListnojsoncallback1");
            def resp = restClient.get(path: "/services/rest/", params : [method: "flickr.photosets.getList",api_key: CLIENT_ID, auth_token: accessToken, api_sig: frobSig,format:"json",nojsoncallback:"1"])

            return resp.data.photosets
        }else if(folderId == "untitled"){
            def pageNumber;
            if(after!=''){
                pageNumber = ++after
            }else{
                pageNumber = 1;
            }
            String newValue = accessToken+"extrasurl_mformatjson"
            def frobSig = getTokenSignature(newValue,"flickr.photos.getNotInSetnojsoncallback1page"+pageNumber+"per_page25photoset_id"+folderId+"privacy_filter%5B1%2C2%2C3%2C4%2C5%5D");

            def resp = restClient.get(path: "/services/rest/", params : [method: "flickr.photos.getNotInSet",api_key: CLIENT_ID, auth_token: accessToken, api_sig: frobSig, format:"json",nojsoncallback:"1",extras:"url_m", page:pageNumber, per_page:"25", photoset_id:folderId, privacy_filter:"%5B1%2C2%2C3%2C4%2C5%5D"])
            return resp.data.photos
        }else{
            def pageNumber;
            if(after!=''){
                pageNumber = ++after
            }else{
                pageNumber = 1;
            }
            String newValue = accessToken+"extrasurl_mformatjson"
            def frobSig = getTokenSignature(newValue,"flickr.photosets.getPhotosnojsoncallback1page"+pageNumber+"per_page25photoset_id"+folderId+"privacy_filter%5B1%2C2%2C3%2C4%2C5%5D");

            def resp = restClient.get(path: "/services/rest/", params : [method: "flickr.photosets.getPhotos",api_key: CLIENT_ID, auth_token: accessToken, api_sig: frobSig, format:"json",nojsoncallback:"1",extras:"url_m", page:pageNumber, per_page:"25", photoset_id:folderId, privacy_filter:"%5B1%2C2%2C3%2C4%2C5%5D"])
            return resp.data.photoset
        }
    }

    static def getFile(String fileId, String accessToken) {
        String newValu = accessToken+"formatjson"
        String newMethod = "flickr.photos.getSizesnojsoncallback1photo_id"+fileId
        def frobSig = getTokenSignature(newValu,newMethod);
        def restClient = new RESTClient(API_URL)
        def resp = restClient.get(path: "/services/rest/", params : [method: "flickr.photos.getSizes",api_key: CLIENT_ID, auth_token: accessToken, api_sig: frobSig, format:"json",nojsoncallback:"1",photo_id:fileId])
        return resp.data.sizes.size
    }

    static URLConnection getDownloadUrlConnection(String fileId, String accessToken) {
        def file = getFile(fileId, accessToken)
        String contentUrl
       file.each {
            if(it.label == "Original"){
               contentUrl = it.source;
            }
        }
        URL url = new URL(contentUrl)
        URLConnection connection = url.openConnection();
        connection.connect()
        return connection
    }

    static def refreshToken(String refreshToken) {
        def restClient = new RESTClient(API_URL)
        def resp = restClient.post(
                path: '/v2.6/oauth/access_token',
                body: [client_id   : CLIENT_ID, client_secret: CLIENT_SECRET,
                       refresh_token: refreshToken, grant_type: 'refresh_token'],
                requestContentType: URLENC)
        return resp.data.access_token
    }
*/

   static File downloadFile(input, Access access) {

        PhotosInterface photoInt = getFlickrJ().getPhotosInterface();
        Photo photo = photoInt.getPhoto(input.fileId);

        File file = new File(grailsApplication.config.fileOps.resources.rootDir+File.separator+photo.title+".jpg");
        OutputStream out = new FileOutputStream(file);
        def initialStream = photo.getOriginalAsStream();
        try {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = initialStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            out.close();
        }
        return file;
   }
}
