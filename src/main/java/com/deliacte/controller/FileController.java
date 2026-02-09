package com.deliacte.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.outputs-dir}")
    private String OUTPUT_DIRECTORY;

    /**
     * Télécharger un fichier template associé à une opération
     */
    @GetMapping("/templates/{operationId}/{fileName:.+}")
    @Operation(
            summary = "Télécharger un fichier template",
            description = "Permet de télécharger un fichier template associé à une opération"
    )
    public ResponseEntity<Resource> getTemplateFile(
            @PathVariable UUID operationId,
            @PathVariable String fileName
    ) {

        try {
            Path filePath = Paths.get(
                    OUTPUT_DIRECTORY,
                    "operation_" + operationId,
                    fileName
            ).normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\""
                    )
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
