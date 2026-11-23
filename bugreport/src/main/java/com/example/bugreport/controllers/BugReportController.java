package com.example.bugreport.controllers;

import com.example.bugreport.exceptions.InvalidAttachmentException;
import com.example.bugreport.records.BugReport;
import com.example.bugreport.services.BugReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/// Handles external requests
@RestController
@RequestMapping("/api/report")
public class BugReportController {
    private final BugReportService service;
    /// NOTE: DI service
    public BugReportController(BugReportService service){
        this.service = service;
    }

    // We don't need GET requests, just POST requests for sending info

    /// Request without log file or screenshot
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BugReport createBugReport(@Valid @RequestBody BugReport report) throws IOException {
        // We will return the created bug report here
        service.send(report, null, null);
        return report;
    }

    /// Request with attached screenshot or log files
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BugReport createBugReportWithFiles(
            @Valid @RequestPart("report") BugReport report,
            @RequestPart(value = "screenshot", required = false) MultipartFile screenshot,
            @RequestPart(value = "log", required = false) MultipartFile log) throws IOException {
        service.send(report, screenshot, log);
        return report;
    }

    @ExceptionHandler(InvalidAttachmentException.class)
    public ResponseEntity<Map<String, String>> badFile(InvalidAttachmentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> discordFailed(RestClientException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "Could not deliver bug report"));
    }
}