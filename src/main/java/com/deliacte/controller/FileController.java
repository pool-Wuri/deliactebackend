package com.deliacte.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/v1/files")
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

    /**
     * Prévisualiser ou télécharger un fichier soumis dans un dossier.
     * Le {relativePath} correspond au chemin retourné lors de l'upload
     * (ex. fichiers/champ_xxx/monFichier.pdf).
     */
    @GetMapping("/dossiers/**")
    @Operation(
            summary = "Accéder à un fichier de dossier",
            description = "Retourne le contenu d'un fichier soumis. Passer ?download=true pour forcer le téléchargement."
    )
    public ResponseEntity<Resource> getDossierFile(
            HttpServletRequest request,
            @RequestParam(value = "download", defaultValue = "false") boolean download
    ) {
        try {
            // Extraire le chemin relatif après "/api/v1/files/dossiers/"
            String uri = request.getRequestURI();
            String prefix = "/api/v1/files/dossiers/";
            String relativePath = uri.contains(prefix) ? uri.substring(uri.indexOf(prefix) + prefix.length()) : "";

            Path filePath = Paths.get(OUTPUT_DIRECTORY, relativePath.replace("/", java.io.File.separator)).normalize();

            // Sécurité : vérifier que le chemin reste dans le répertoire autorisé
            Path baseDir = Paths.get(OUTPUT_DIRECTORY).toAbsolutePath().normalize();
            if (!filePath.toAbsolutePath().normalize().startsWith(baseDir)) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            String disposition = download
                    ? "attachment; filename=\"" + resource.getFilename() + "\""
                    : "inline; filename=\"" + resource.getFilename() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
