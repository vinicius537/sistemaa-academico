package com.university.academic.controller;

import com.university.academic.model.Usuario;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private final UserService userService;
    private final SceneManager sceneManager;

    public LoginController() {
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            Optional<Usuario> usuarioOpt = userService.autenticar(username, password);

            if (usuarioOpt.isPresent()) {
                userService.setUsuarioLogado(usuarioOpt.get());

                lblMessage.setText("Login bem-sucedido!");
                // CORREÇÃO AQUI: Adicionado o terceiro argumento 'false'
                sceneManager.loadScene("/com/university/academic/view/PlanoDeEnsinoListView.fxml", "Sistema de Gestão de Planos de Ensino", false);
            } else {
                lblMessage.setText("Usuário ou senha inválidos.");
            }
        } catch (IllegalStateException e) {
            lblMessage.setText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Ocorreu um erro inesperado.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // CORREÇÃO AQUI: Adicionado o terceiro argumento 'false'
        sceneManager.loadScene("/com/university/academic/view/RegisterView.fxml", "Registrar Novo Usuário", false);
    }

    @FXML
    private void handleEsqueciSenha(ActionEvent event) {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Recuperação de Senha");
        emailDialog.setHeaderText("Para iniciar a recuperação, por favor, insira o e-mail da sua conta.");
        emailDialog.setContentText("E-mail:");

        Optional<String> emailResult = emailDialog.showAndWait();

        emailResult.ifPresent(email -> {
            try {
                Optional<String> codigoOpt = userService.iniciarResetDeSenha(email);
                codigoOpt.ifPresent(this::mostrarDialogoDeReset);
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", e.getMessage());
            }
        });
    }

    private void mostrarDialogoDeReset(String codigoGerado) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Código Enviado");
        infoAlert.setHeaderText("Um código de recuperação foi enviado para o console.");
        infoAlert.setContentText("Por favor, verifique o console, copie o código e insira-o na próxima tela junto com sua nova senha.");
        infoAlert.showAndWait();

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Redefinir Senha");
        dialog.setHeaderText("Insira o código de recuperação e sua nova senha.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codigoField = new TextField();
        codigoField.setPromptText("Código recebido no console");
        PasswordField novaSenhaField = new PasswordField();
        novaSenhaField.setPromptText("Nova Senha");

        grid.add(new Label("Código:"), 0, 0);
        grid.add(codigoField, 1, 0);
        grid.add(new Label("Nova Senha:"), 0, 1);
        grid.add(novaSenhaField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(codigoField.getText(), novaSenhaField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(dados -> {
            String codigoInserido = dados.getKey();
            String novaSenha = dados.getValue();
            try {
                userService.finalizarResetDeSenha(codigoInserido, novaSenha);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Sua senha foi redefinida com sucesso! Você já pode fazer o login com a nova senha.");
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Falha na Redefinição", e.getMessage());
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}