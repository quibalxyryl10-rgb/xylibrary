package com.xyryl.library;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.io.File;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.net.URL;
import java.util.Scanner;

public class GoogleAuthService {

    private static final String CLIENT_ID = "237963619153-9bk2b3sh3o9sq7pmta5jv8gu0f8bevq1.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );
    private static final File TOKENS_DIRECTORY = new File("tokens");

    public static Credential getCredential() throws Exception {
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        String clientSecretJson = "{\"installed\":{\"client_id\":\"" + CLIENT_ID +
                "\",\"client_secret\":\"" + CLIENT_SECRET +
                "\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\"," +
                "\"token_uri\":\"https://oauth2.googleapis.com/token\"}}";

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(clientSecretJson));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIRECTORY))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    // Get the logged-in user's name and email from Google
    public static String[] getUserInfo(Credential credential) throws Exception {
        URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + credential.getAccessToken());
        Scanner scanner = new Scanner(url.openStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        String json = response.toString();
        String email = extractJsonValue(json, "email");
        String name = extractJsonValue(json, "name");

        return new String[]{name, email};
    }

    private static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return "";
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return "";
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return "";
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return "";
        return json.substring(startQuote + 1, endQuote);
    }
}