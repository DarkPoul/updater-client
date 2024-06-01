package com.dekanat.ntu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;




public class Main {

    private static final String VERSION_URL = "http://212.111.203.173/version-info";
    private static final String UPDATE_URL = "http://212.111.203.173/download";
    private static final String LOCAL_VERSION_FILE = "C:\\Users\\poulp\\Downloads\\version.json";
    private static final String UPDATE_DIR = "C:\\Users\\poulp\\Downloads\\";

    public static void main(String[] args) throws IOException {
        System.out.println("Start");
        System.out.println(readRemoteVersion());
        System.out.println(readLocalVersion());

        if (!readRemoteVersion().equals(readLocalVersion())) {
            downloadUpdate(readRemoteVersion());
        }

    }

    private static String readLocalVersion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(LOCAL_VERSION_FILE));
        return rootNode.path("version").asText();
    }

    private static String readRemoteVersion() throws IOException {
        Document doc = Jsoup.connect(VERSION_URL).get();
        Element versionElement = doc.selectFirst("p");
        assert versionElement != null;
        String versionText = versionElement.text();
        return versionText.split("№:")[1].trim();
    }

    private static void downloadUpdate(String newVersion) throws IOException {
        URL url = new URL(UPDATE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        InputStream in = connection.getInputStream();
        Files.copy(in, Paths.get(UPDATE_DIR + "update.zip"), StandardCopyOption.REPLACE_EXISTING);
        in.close();
        connection.disconnect();
        System.out.println("Downloaded successfully");

        rewriteFileVersion(newVersion);
    }

    //перезаписати файл на версію readRemoteVersion() version.json
    private static void rewriteFileVersion(String newVersion) throws IOException {
        FileReader reader = new FileReader(LOCAL_VERSION_FILE);
        StringBuilder jsonContent = new StringBuilder();
        int i;
        while ((i = reader.read()) != -1) {
            jsonContent.append((char) i);
        }
        reader.close();

        // Парсимо JSON
        JSONObject jsonObject = new JSONObject(jsonContent.toString());
        jsonObject.put("version", newVersion);
        FileWriter writer = new FileWriter(LOCAL_VERSION_FILE);
        writer.write(jsonObject.toString(4)); // Форматування з відступами для читабельності
        writer.close();

        System.out.println("Файл version.json успішно оновлено!");


    }






}