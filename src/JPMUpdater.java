import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JPMUpdater {

    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/ZayzX/JavaPackageManager/refs/heads/main/src/main/java/json/version.json";

    private static final String LOCAL_JAR_PATH = "wrapper/JavaPackageManager.jar";
    private static final String NEW_JAR_PATH = "wrapper/JavaPackageManager-new.jar";
    private static final String LOCAL_VERSION = "1.0.1"; 

    public static void main(String[] args) {
        try {
            System.out.println("Checking for updates...");

            JsonObject remote = fetchRemoteVersion();

            String remoteVersion = remote.get("version").getAsString();
            String downloadUrl = remote.get("jar").getAsString();

            if (!LOCAL_VERSION.equals(remoteVersion)) {
                System.out.println("Update found: " + remoteVersion);

                downloadFile(downloadUrl, NEW_JAR_PATH);

                replaceJar();

                System.out.println("Update installed !");
            } else {
                System.out.println("Already up to date");
            }

            launchJPM(args);

        } catch (Exception e) {
            System.out.println("Update failed, launching anyway...");
            e.printStackTrace();
            launchJPM(args);
        }
    }

    private static JsonObject fetchRemoteVersion() throws Exception {
        try (InputStream is = new URL(VERSION_URL).openStream()) {
            InputStreamReader reader = new InputStreamReader(is);
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void downloadFile(String url, String outputPath) throws Exception {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void replaceJar() throws Exception {
        Path oldJar = Paths.get(LOCAL_JAR_PATH);
        Path newJar = Paths.get(NEW_JAR_PATH);

        Files.deleteIfExists(oldJar);
        Files.move(newJar, oldJar, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void launchJPM(String[] args) {
    try {
        Path currentJar = Paths.get(LOCAL_JAR_PATH);
        Path newJar = Paths.get(NEW_JAR_PATH);

        if (Files.exists(newJar)) {
            System.out.println("Applying pending update...");

            Files.deleteIfExists(currentJar);
            Files.move(newJar, currentJar, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Update applied successfully.");
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(LOCAL_JAR_PATH);

        command.addAll(Arrays.asList(args));

        new ProcessBuilder(command)
                .inheritIO()
                .start()
                .waitFor();

    } catch (Exception e) {
        System.err.println("Failed to launch JPM");
        e.printStackTrace();
    }
}
}