package filesutra

import grails.converters.JSON
import grails.transaction.Transactional
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.Drive.Builder
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
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

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

@Transactional
class GoogleService {

    //TODO:handle token expiration
    def callAPI(Closure c, Access access) {
        def accessInfo = JSON.parse(access.accessInfo)
        println "Google call API ${access}"
        try {
            c(accessInfo.accessToken)
        } catch (GoogleJsonResponseException e) {
            log.error e;
            if (e.getStatusCode() == 401) {
                log.debug "Calling refresh token"
                accessInfo.accessToken = Google.refreshToken(accessInfo.refreshToken)
                access.accessInfo = Utils.jsonToString(accessInfo)
                log.debug "Got fresh accessToken.Saving"
                access.save(flush: true, failOnError: true)
                println "calling clousure"
                c(accessInfo.accessToken)
            } else {
                throw e
            }
        }
    }

    def listItems(String folderId, String afterVal, Access access) {
//        callAPI({ 
            return Google.listItems(folderId, afterVal, access)
//        }, access)

   }

    def getDownloadUrlConnection(String fileId, Access access) {
        callAPI({ accessToken ->
            return Google.getDownloadUrlConnection(fileId, accessToken)
        }, access)
    }

    java.io.File downloadFile(input, Access access) {
       return Google.downloadFile(input, access); 
    }
}
