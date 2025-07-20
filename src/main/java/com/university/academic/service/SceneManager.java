package com.university.academic.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.university.academic.Main;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private Map<String, Parent> sceneCache = new HashMap<>();

    private SceneManager() {}

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void loadScene(String fxmlPath, String title, boolean cacheScene) {
        try {
            Parent root;
            if (cacheScene && sceneCache.containsKey(fxmlPath)) {
                root = sceneCache.get(fxmlPath);
                System.out.println("DEBUG: Carregando cena do cache: " + fxmlPath);
            } else {
                URL fxmlUrl = Main.class.getResource(fxmlPath);

                System.out.println("DEBUG - Tentando carregar FXML:");
                System.out.println("→ Caminho FXML: " + fxmlPath);
                System.out.println("→ URL encontrada: " + fxmlUrl);


                if (fxmlUrl == null) {
                    throw new IOException("FXML file not found: " + fxmlPath + ". Please check the file path and Maven resources configuration. URL returned null.");
                }

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(fxmlUrl);
                root = fxmlLoader.load();

                if (cacheScene) {
                    sceneCache.put(fxmlPath, root);
                    System.out.println("DEBUG: Armazenando cena em cache: " + fxmlPath);
                }
                System.out.println("DEBUG: Carregando nova cena: " + fxmlPath);
            }

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root));
            } else {
                primaryStage.getScene().setRoot(root);
            }
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            System.exit(1);
        }
    }
}