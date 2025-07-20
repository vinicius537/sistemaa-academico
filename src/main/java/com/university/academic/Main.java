// src/main/java/com/university/academic/Main.java
package com.university.academic;

import com.university.academic.service.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Obtém a instância Singleton do SceneManager
        // O SceneManager é responsável por carregar e gerenciar as diferentes telas (cenas) da aplicação.
        SceneManager sceneManager = SceneManager.getInstance();

        // Define o Stage principal da aplicação no SceneManager.
        // Isso permite que o SceneManager controle qual cena será exibida nesta janela principal.
        sceneManager.setPrimaryStage(primaryStage);

        // Carrega a tela de login como a primeira cena da aplicação.
        // O caminho para o FXML é relativo à pasta     'resources'.
        // O segundo argumento é o título da janela.
        // O terceiro argumento (false) indica que esta cena não será armazenada em cache (não é necessário para a tela de login).
        sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
    }

    public static void main(String[] args) {
        // Método principal que inicia a aplicação JavaFX.
        // O método 'launch()' chama o método 'start()' da classe Application.
        launch();
    }
}