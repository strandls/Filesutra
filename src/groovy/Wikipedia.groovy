package filesutra

import grails.util.Holders
import groovyx.net.http.RESTClient

import static groovyx.net.http.ContentType.URLENC
import grails.converters.JSON
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import wikipedia.Wiki;
import wikipedia.Wiki.*;

/**
 * Created by vishesh on 06/05/15.
 */
class Wikipedia {

    def static grailsApplication = Holders.getGrailsApplication()

    //private static final String CLIENT_ID     = grailsApplication.config.auth.wikimedia.CLIENT_ID
    //private static final String CLIENT_SECRET = grailsApplication.config.auth.wikimedia.CLIENT_SECRET
    private static final String REDIRECT_URI = grailsApplication.config.auth.wikimedia.REDIRECT_URI
    private static final String AUTH_URL = grailsApplication.config.auth.wikimedia.AUTH_URL

    private static final String API_URL = "commons.wikimedia.org"

    static def getLoginUrl() {
        def url = AUTH_URL;
        return url
    }

    static def exchangeCode(String username, String password) {
        try {
            Wiki wiki = new Wiki(API_URL); // create a new wiki connection to en.wikipedia.org
            wiki.login(username, password); // log in as user ExampleBot, with the specified password
            File sessions_dir = new File(grailsApplication.config.fileOps.resources.rootDir+"/wikipedia_sessions");
            if(!sessions_dir.exists()) sessions_dir.mkdir();
            File session = new File(sessions_dir, username);
            if(session.exists()) {
                session.delete();
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(session));
            try {
                out.writeObject(wiki);
                out.flush();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if(out != null) out.close();
            }
       return [
                accessToken: username+"_wikiepdia",
                username:username,
                wiki:session.getAbsolutePath()
        ]

        }
        catch (Exception ex)
        {
            // deal with failed login attempt
            ex.printStackTrace();
            render (['msg':ex.getMessage()] as JSON)
        }

     }

    static def getEmailId(String accessToken) {
        return accessToken;
    }

    static def listItems(String folderId, Access access) {
        List images = [];
        try {
            def accessInfo = JSON.parse(access.accessInfo);
            String username = accessInfo.username;
            File accessFile = new File(accessInfo.wiki)
            Wiki wiki;
            accessFile.withObjectInputStream(Wiki.class.classLoader) { is ->
                wiki = (wikipedia.Wiki) is.readObject();
            }
            if(wiki) {
                Wiki.User user = wiki.getUser(username);
                Wiki.LogEntry[] uploads = wiki.getUploads(user);
                println uploads
                println uploads.details;

                uploads.each {
                    def metadata = wiki.getFileMetadata(it.target);
                    println "Image Metadata ${metadata}";
                    images << ['title':it.target, 'name':it.target, 'size':metadata.size, 'contentType':metadata.mime, 'url':metadata.url, 'iconurl':metadata.thumburl];
                }
            }
            println images;
        } catch(Exception e) {
            e.printStackTrace(); 
        } 
        return images;
    }

   static File downloadFile(input, Access access) {
        File file = new File(grailsApplication.config.fileOps.resources.rootDir+File.separator+input.fileName);
        //OutputStream out = new FileOutputStream(file);
        try {
            def accessInfo = JSON.parse(access.accessInfo);
            String username = accessInfo.username;
            File accessFile = new File(accessInfo.wiki)
            Wiki wiki;
            accessFile.withObjectInputStream(Wiki.class.classLoader) { is ->
                wiki = (wikipedia.Wiki) is.readObject();
            }
            if(wiki) {
                byte[] image = wiki.getImage(input.fileId);
                file.setBytes(image);
            } else {
                return null;
            }
        } catch(Exception e) {
            e.printStackTrace(); 
            return null;
        } 
        return file
   }

}
