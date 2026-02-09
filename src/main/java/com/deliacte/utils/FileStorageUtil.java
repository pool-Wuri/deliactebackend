package com.deliacte.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

public final class FileStorageUtil {

    private FileStorageUtil() {}

    public static String saveFile(
            MultipartFile file,
            String outputDirectory
    ) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier vide ou inexistant");
        }

        try {
            String normalizedFilename =
                 FileNameUtil.normalize(file.getOriginalFilename());

            Path directoryPath = Paths.get(outputDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(directoryPath);

            Path targetPath = directoryPath.resolve(normalizedFilename);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return normalizedFilename;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }
}
