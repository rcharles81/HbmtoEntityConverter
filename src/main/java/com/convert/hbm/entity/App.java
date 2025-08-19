package com.convert.hbm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar hbm2entityconverter.jar <hbm-files-dir> <output-dir>");
            System.exit(1);
        }

        String hbmDir = args[0].replace('\\', '/');
        String outputDir = args[1].replace('\\', '/');

        App app = new App();
        app.convertHbmToEntities(hbmDir, outputDir);
    }

    public void convertHbmToEntities(String hbmDir, String outputDir) {
        try {
            // Create HBM parser
            HbmParser parser = new HbmParser();

            // Add all HBM files from the directory
            File hbmDirFile = new File(hbmDir);
            if (!hbmDirFile.isDirectory()) {
                throw new RuntimeException("HBM directory does not exist or is not a directory: " + hbmDir);
            }

            File[] hbmFiles = hbmDirFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".hbm.xml"));
            if (hbmFiles == null || hbmFiles.length == 0) {
                throw new RuntimeException("No HBM files found in directory: " + hbmDir);
            }

            for (File hbmFile : hbmFiles) {
                parser.addHbmFile(hbmFile);
            }

            // Parse metadata
            List<EntityMetadata> entities = parser.parseMetadata();

            // Generate entity classes
            EntityGenerator generator = new EntityGenerator(outputDir);
            for (EntityMetadata entity : entities) {
                generator.generateEntity(entity);
            }

            logger.info("Successfully converted {} entities", entities.size());

        } catch (Exception e) {
            logger.error("Conversion failed", e);
            System.exit(1);
        }
    }
}
