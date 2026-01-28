package com.agrowmart.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Prevent duplicate initialization
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            String base64Credentials = System.getenv("FIREBASE_CREDENTIALS_JSON");

            if (base64Credentials == null || base64Credentials.isBlank()) {
                System.out.println("⚠️ Firebase credentials not found, skipping Firebase init");
                return; // <-- app will still start
            }

            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            InputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            System.out.println("✅ Firebase initialized successfully");

        } catch (Exception e) {
            throw new RuntimeException("❌ Firebase initialization failed", e);
        }
    }
}