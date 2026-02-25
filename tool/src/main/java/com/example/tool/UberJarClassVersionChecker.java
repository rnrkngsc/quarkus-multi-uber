package com.example.tool;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class UberJarClassVersionChecker {

    private static final int DEFAULT_MAX_MAJOR_VERSION = 65; // Java 21

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -jar tool-<version>.jar <uber-jar-path> [max-major-version]");
            System.exit(2);
        }

        Path jarPath = Path.of(args[0]);
        if (!Files.isRegularFile(jarPath)) {
            System.err.println("ERROR: File not found: " + jarPath);
            System.exit(2);
        }

        int maxMajorVersion = DEFAULT_MAX_MAJOR_VERSION;
        if (args.length == 2) {
            try {
                maxMajorVersion = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("ERROR: Invalid max-major-version: " + args[1]);
                System.exit(2);
            }
        }

        boolean ok = checkUberJar(jarPath, maxMajorVersion);
        System.exit(ok ? 0 : 1);
    }

    private static boolean checkUberJar(Path jarPath, int maxMajorVersion) throws IOException {
        boolean success = true;

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.endsWith(".class")) {
                    continue;
                }

                // Allow higher class versions inside multi-release jar locations
                if (name.startsWith("META-INF/versions/")) {
                    continue;
                }

                int major = readClassMajorVersion(jarFile, entry);
                if (major > maxMajorVersion) {
                    System.err.printf(
                            "ERROR: %s has major version %d > allowed %d outside of META-INF/versions/%n",
                            name, major, maxMajorVersion
                    );
                    success = false;
                }
            }
        }

        if (!success) {
            System.err.println("Found class files with disallowed major versions at JAR root.");
        }

        return success;
    }

    private static int readClassMajorVersion(JarFile jarFile, JarEntry entry) throws IOException {
        try (InputStream is = jarFile.getInputStream(entry);
             DataInputStream dis = new DataInputStream(is)) {

            // Class file header:
            // u4 magic; u2 minor_version; u2 major_version;
            int magic = dis.readInt();
            if (magic != 0xCAFEBABE) {
                throw new IOException("Invalid class file magic for " + entry.getName());
            }
            dis.readUnsignedShort(); // minor
            return dis.readUnsignedShort(); // major
        }
    }
}

