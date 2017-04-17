package filesutra

import grails.util.Holders
import groovyx.net.http.RESTClient

import static groovyx.net.http.ContentType.URLENC
import grails.converters.JSON
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.drive.Drive
import com.google.api.services.drive.Drive.Builder
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenErrorResponse;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.client.googleapis.media.*;
import com.google.api.client.googleapis.media.MediaHttpDownloader.DownloadState;
import com.google.api.client.http.GenericUrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.io.File;

/**
 * Created by vishesh on 06/05/15.
 */
class Google {

    def static grailsApplication = Holders.getGrailsApplication()

    private static final String CLIENT_ID     = grailsApplication.config.auth.google.CLIENT_ID
    private static final String CLIENT_SECRET = grailsApplication.config.auth.google.CLIENT_SECRET
    private static final String REDIRECT_URI = grailsApplication.config.auth.google.REDIRECT_URI

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth"
    private static final String API_URL = "https://www.googleapis.com"

    /** Application name. */
    private static final String APPLICATION_NAME =
    "Drive API Java Quickstart";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES =
    Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_PHOTOS_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(Access access) throws IOException {
        def accessInfo = JSON.parse(access.accessInfo)

        GoogleClientSecrets clientSecrets =new GoogleClientSecrets().setWeb((new GoogleClientSecrets.Details())
        .setClientId(CLIENT_ID)
        .setClientSecret(CLIENT_SECRET)
        .setAuthUri(AUTH_URL)
        .setRedirectUris([REDIRECT_URI])
        .setTokenUri(API_URL+'/oauth2/v3/token'));

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessInfo.accessToken)
        .setRefreshToken(accessInfo.refreshToken);

        GoogleCredential credential = createCredentialWithRefreshToken(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, tokenResponse)
println credential.getAccessToken();
println credential.getRefreshToken();
println credential.getExpiresInSeconds();
credential.executeRefreshToken();
        //Credential credential = new AuthorizationCodeInstalledApp(
        //    flow, new LocalServerReceiver()).authorize("user");

        return credential;
    }

    public static GoogleCredential createCredentialWithRefreshToken(HttpTransport transport,
    JsonFactory jsonFactory, GoogleClientSecrets clientSecrets, TokenResponse tokenResponse) {
        return new GoogleCredential.Builder()
        .setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setClientSecrets(clientSecrets)
        .addRefreshListener(new CredentialRefreshListener() {
            @Override
            public void onTokenResponse(final Credential credential, final TokenResponse tokenResponse1) throws IOException {
                println "Token refreshed";
            }

            @Override
            public void onTokenErrorResponse(final Credential credential, final TokenErrorResponse tokenErrorResponse) throws IOException {
                println "Token refresh error ${tokenErrorResponse}";
            }
        })
        .build()
        .setFromTokenResponse(tokenResponse)
        //.createScoped(["email","https://www.googleapis.com/auth/drive"]);
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService(Access access) throws IOException {
        Credential credential = authorize(access);
        return new Drive.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    static def getLoginUrl() {
        def params = [
                response_type: "code",
                client_id    : CLIENT_ID,
                redirect_uri : REDIRECT_URI,
                access_type  : "offline",
                scope        : "email https://www.googleapis.com/auth/drive",
                approval_prompt:"force"
        ]
        def url = "$AUTH_URL?" + params.collect { k, v -> "$k=$v" }.join('&')
        return url
    }

    static def exchangeCode(String code) {
        def restClient = new RESTClient(API_URL)
        def resp = restClient.post(
                path: '/oauth2/v3/token',
                body: [client_id   : CLIENT_ID, client_secret: CLIENT_SECRET,
                       redirect_uri: REDIRECT_URI, code: code, grant_type: 'authorization_code'],
                requestContentType: URLENC)
        println resp.data;
        return [
                accessToken: resp.data.access_token,
                refreshToken: resp.data.refresh_token
        ]
    }

    static def getEmailId(String accessToken) {
        def restClient = new RESTClient(API_URL)
        restClient.headers.Authorization = "Bearer $accessToken"
        def resp = restClient.get(path: '/plus/v1/people/me')
        return resp.data.emails[0].value
    }

    /*static def listItems(String folderId, String accessToken) {
        def restClient = new RESTClient(API_URL)
        restClient.headers.Authorization = "Bearer $accessToken"
        def resp = restClient.get(path: "/drive/v2/files", params : [q: "'$folderId' in parents and trashed=false"])
        return resp.data.items
    }*/

    static def listItems(String folderId, String afterVal, Access access) {
        // Build a new authorized API client service.
        Drive service = Google.getDriveService(access);

        // Print the names and IDs for up to 10 files.
        FileList result;
        def folders = service.files().list()
        .setQ("'$folderId' in parents and trashed=false and mimeType = 'application/vnd.google-apps.folder'")
        .setFields("nextPageToken, files(id,name,size,mimeType,thumbnailLink,webContentLink)")


        def files = service.files().list()
        .setQ("'$folderId' in parents and trashed=false")
        .setFields("nextPageToken, files(id,name,size,mimeType,thumbnailLink,webContentLink)")
        .setPageSize(25);
println afterVal
println "========================++"
        if(afterVal)
            files.setPageToken(afterVal);

        def f = [];
        result = folders.execute();
        f.addAll(result.getFiles());
        result = files.execute();
println "========================++"
println result.getFiles().size();
        f.addAll(result.getFiles());

        return ['files':f,'nextPageToken':files.getPageToken()];
    }

    static def getFile(String fileId, String accessToken) {
        def restClient = new RESTClient(API_URL)
        restClient.headers.Authorization = "Bearer $accessToken"
        def resp = restClient.get(path: "/drive/v2/files/$fileId")
        return resp.data
    }

    static def getDownloadUrlConnection(String fileId, String accessToken) {
        def file = getFile(fileId, accessToken)
        String contentUrl = file.downloadUrl ? file.downloadUrl : file.exportLinks?."application/pdf"
        URL url = new URL(contentUrl)
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Authorization", 'Bearer ' + accessToken);
        def resp = [connection: connection]
        if (!file.downloadUrl && contentUrl) {
            resp.extension = "pdf"
        }
        return resp
    }

    static def refreshToken(String refreshToken) {
        def restClient = new RESTClient(API_URL)
        def resp = restClient.post(
                path: '/oauth2/v3/token',
                body: [client_id   : CLIENT_ID, client_secret: CLIENT_SECRET,
                       refresh_token: refreshToken, grant_type: 'refresh_token'],
                requestContentType: URLENC)
        println resp.data
        return resp.data.access_token
    }

   static File downloadFile(input, Access access) {
        File file = new File(grailsApplication.config.fileOps.resources.rootDir+File.separator+input.fileName);
        OutputStream out = new FileOutputStream(file);

        Drive.Files.Get request = getDriveService(access).files().get(input.fileId);
        request.getMediaHttpDownloader().setProgressListener(new CustomProgressListener());
        request.executeMediaAndDownloadTo(out);
        return file
   }

   static class CustomProgressListener implements MediaHttpDownloaderProgressListener {
       public void progressChanged(MediaHttpDownloader downloader) {
           switch (downloader.getDownloadState()) {
               case DownloadState.MEDIA_IN_PROGRESS:
               System.out.println(downloader.getProgress());
               break;
               case DownloadState.MEDIA_COMPLETE:
               System.out.println("Download is complete!");
           }
       }
   }

}
