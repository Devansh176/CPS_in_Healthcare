#include <WiFi.h>
#include <HTTPClient.h>

const char* ssid = "LAB-ONLY";
const char* password = "Labs@dtf";
const char* serverAddress = "http://localhost:8080/heartRate/data"; 

void setup() {
    Serial.begin(74880);
    WiFi.begin(ssid, password);

    Serial.print("Connecting to WiFi");
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.print(".");
    }
    Serial.println("\nConnected to WiFi!");

    Serial.print("ESP32 IP Address: ");
    Serial.println(WiFi.localIP());
}

void loop() {
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        http.begin(serverAddress);
        http.addHeader("Content-Type", "application/json");

        // JSON payload
        String jsonPayload = "{\"patientId\":3, \"startTime\":\"2025-02-28T12:00:00Z\", \"endTime\":\"2025-02-28T12:10:00Z\", \"heartRate\":80}";

        Serial.print("🔗 Sending POST request to: ");
        Serial.println(serverAddress);
        Serial.print("📦 Payload: ");
        Serial.println(jsonPayload);

        // Send request
        int httpResponseCode = http.POST(jsonPayload);

        // Print response
        if (httpResponseCode > 0) {
            Serial.print("✅ Response Code: ");
            Serial.println(httpResponseCode);
            Serial.println(http.getString());
        } else {
            Serial.print("❌ Error sending POST request: ");
            Serial.println(httpResponseCode);
        }

        http.end();

    } else {
        Serial.println("WiFi Disconnected!");
    }

    delay(5000);
}