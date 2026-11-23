package com.example.bugreport.services;

import com.example.bugreport.records.BugReport;
import com.example.bugreport.records.Severity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/// Handles interfacing with Discord API and sending bug report
@Service
public class BugReportService {
    /// Valid image types
    private static final Set<String> IMAGE_TYPES = Set.of("image/png", "image/jpeg");
    private final RestClient DISCORD_CLIENT = RestClient.builder().requestFactory(timeout()).build();
    private final String WEB_HOOK_URL;
    private static final int CONNECTION_TIMEOUT = 5;
    private static final int READ_TIMEOUT = 15;

    public BugReportService(@Value("${discord.webhook-url}") String webhookUrl)
    {
        WEB_HOOK_URL = webhookUrl;
    }


    /// Interfaces with the Discord API to send a message in a channel
    public void send(BugReport report, MultipartFile screenshot, MultipartFile log) throws IOException {
        List<Map<String, Object>> fields = new ArrayList<>();

        // If severity wasn't set, default to medium
        Severity severity = report.severity() != null ? report.severity() : Severity.MEDIUM;

        // If no email is provided, set to severity instead
        fields.add(Map.of("name", "Severity", "value", severity.name(), "inline", true));
        if (report.email() != null) {
            fields.add(Map.of("name", "Contact", "value", report.email(), "inline", true));
        }

        // Build message that will display in Discord here
        Map<String, Object> embed = new HashMap<>(Map.of(
                "title", report.title(),
                "description", report.message(),
                "color", severity.getColor(),
                "fields", fields,
                "timestamp", Instant.now().toString()
        ));

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        int fileIndex = 0;

        // Embeds screenshot of bug to message
        if (screenshot != null && !screenshot.isEmpty()) {
            String type = screenshot.getContentType();

            if (type == null || !IMAGE_TYPES.contains(type)) {
                throw new IllegalArgumentException("Screenshot must be PNG or JPEG");
            }

            String name = type.equals("image/png") ? "screenshot.png" : "screenshot.jpg";
            embed.put("image", Map.of("url", "attachment://" + name));
            parts.add("files[" + fileIndex++ + "]", filePart(screenshot.getBytes(), name, MediaType.parseMediaType(type)));
        }

        // Attached log file to discord message
        if (log != null && !log.isEmpty()) {
            parts.add("files[" + fileIndex++ + "]", filePart(log.getBytes(), "log.txt", MediaType.TEXT_PLAIN));
        }

        // This is the permissions of the sender
        Map<String, Object> body = Map.of(
                "embeds", List.of(embed),
                "allowed_mentions", Map.of("parse", List.of())
        );

        // Adds rest of the body to the POST request
        parts.add("payload_json", jsonPart(body));

        DISCORD_CLIENT.post()
                .uri(WEB_HOOK_URL + "?wait=true")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .toBodilessEntity();
    }

    /// Stops connections that take too long to send a POST request
    private static SimpleClientHttpRequestFactory timeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT));
        factory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT));
        return factory;
    }

    /// Builds the Multipart file for images or attachments
    private static HttpEntity<ByteArrayResource> filePart(byte[] data, String filename, MediaType type) {
        ByteArrayResource resource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        return new HttpEntity<>(resource, headers);
    }

    /// Builds the JSON part of the message like the title, message, and severity
    private static HttpEntity<Map<String, Object>> jsonPart(Map<String, Object> json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }
}