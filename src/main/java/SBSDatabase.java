import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SBSDatabase {
    private static final String APPLICATION_NAME = "SBSDatabase";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(PeopleServiceScopes.CONTACTS_READONLY, PeopleServiceScopes.DIRECTORY_READONLY, PeopleServiceScopes.USER_ORGANIZATION_READ);
    private static final String CREDENTIALS_FILE_PATH = "/c.json";

    public static List<Person> all_people = new ArrayList<>();
    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SBSDatabase.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        PeopleService service = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();




        List<String> sources = new ArrayList<>();
        sources.add("DIRECTORY_SOURCE_TYPE_DOMAIN_CONTACT");
        sources.add("DIRECTORY_SOURCE_TYPE_DOMAIN_PROFILE");

        ListDirectoryPeopleResponse response1 = service.people().listDirectoryPeople()
                .setSources(sources)
                .setPageSize(1000)
                .setReadMask("names,emailAddresses")
                .execute();

        for (Person p : response1.getPeople()) {
            //System.out.println(p.getNames().get(0).getDisplayName());
            FileWriter f = new FileWriter("everyone.txt");
            f.write(p.getNames().get(0).getDisplayName());

            f.flush();
            f.close();
            System.out.println(p.getNames().get(0).getDisplayName());

            System.out.println(p);
        }

        String token = response1.getNextPageToken();
        System.out.println("Next token: " + token);




        do {

            ListDirectoryPeopleResponse response2 = service.people().listDirectoryPeople()
                    .setSources(sources)
                    .setPageSize(1000)
                    .setPageToken(token)
                    .setReadMask("names,emailAddresses")
                    .execute();

            token = response2.getNextPageToken();
            System.out.println(response2.getNextPageToken());



            for (Person p : response2.getPeople()) {
                //System.out.println(p.getNames().get(0).getDisplayName());
                FileWriter f = new FileWriter("everyone.txt");
                f.write(p.getNames().get(0).getDisplayName());
                f.flush();
                f.close();
                System.out.println(p.getNames().get(0).getDisplayName());
                System.out.println(p.getEmailAddresses().get(0).get("value"));
            }



        } while (token != null);

    }




}