package main.java;

import main.java.CharClassBox.ClassJsonFormat;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tests {
    public static void main(String[] args) {
        try {
            Gson gson = new Gson();
            String json = new String(Files.readAllBytes(Paths.get("src/CharClassBox/CharClassBox.json")));
            ClassJsonFormat response = gson.fromJson(json, ClassJsonFormat.class);
            String potato = "";
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
