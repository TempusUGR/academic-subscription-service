package com.calendarugr.academic_subscription_service.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FTPService {

    private static final Logger logger = LoggerFactory.getLogger(FTPService.class);

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private String ftpPort;

    @Value("${ftp.username}")
    private String ftpUser;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.base-directory}")
    private String ftpDirectory;

    public String uploadFileUsingScript(String fileName, byte[] fileContent) throws IOException {
        logger.info("Iniciando la subida del archivo: {}", fileName);

        // Crear un archivo temporal para el array de bytes
        File tempFile = new File("/tmp/"+fileName);
        if (tempFile.exists()) {
            logger.warn("El archivo temporal ya existe y será sobrescrito: {}", tempFile.getAbsolutePath());
        } else {
            logger.info("Creando un nuevo archivo temporal: {}", tempFile.getAbsolutePath());
        }
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileContent);
            logger.info("Archivo temporal creado: {}", tempFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error al escribir el archivo temporal", e);
            throw e;
        }

        // Ruta al script
        String scriptPath = getClass().getClassLoader().getResource("scripts/upload_ftp.sh").getPath();
        logger.info("Ruta del script: {}", scriptPath);        

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            logger.error("El script no existe en la ruta especificada: {}", scriptPath);
            throw new IOException("El script no existe en la ruta especificada: " + scriptPath);
        }

        // Construir el comando
        ProcessBuilder processBuilder = new ProcessBuilder(
            "zsh",
            scriptPath,
            ftpHost,
            ftpPort,
            ftpUser,
            ftpPassword,
            ftpDirectory,
            tempFile.getAbsolutePath() // Pasar la ruta del archivo temporal
        );

        logger.info("Parametros del script: {}, {}, {}, {}, {}", ftpHost, ftpPort, ftpUser, ftpDirectory, tempFile.getAbsolutePath());

        Process process;
        try {
            logger.info("Ejecutando el script...");
            process = processBuilder.start();
        } catch (IOException e) {
            logger.error("Error al iniciar el script", e);
            throw e;
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Script ejecutado correctamente. Archivo subido: {}", fileName);
                return "https://calendarugr.alwaysdata.net/" + fileName;
            } else {
                logger.error("Error al ejecutar el script. Código de salida: {}", exitCode);
                throw new IOException("Error al ejecutar el script. Código de salida: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("El proceso fue interrumpido", e);
            throw new IOException("El proceso fue interrumpido", e);
        } finally {
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    logger.info("Archivo temporal eliminado: {}", tempFile.getAbsolutePath());
                } else {
                    logger.warn("No se pudo eliminar el archivo temporal: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}