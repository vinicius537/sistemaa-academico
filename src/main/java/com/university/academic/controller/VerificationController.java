package com.university.academic.controller;

import com.university.academic.model.Usuario;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VerificationController {
    @FXML private TextField txtVerificationCode;
    @FXML private Label lblMessage;

    private UserService userService;
    private SceneManager sceneManager;
    private Usuario userToVerify; // Usuário que precisa ser verificado
    private String generatedCode; // Código gerado para verificação (simulado)

    public VerificationController() {
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();
        // Em um sistema real, o código seria gerado e enviado por email/SMS
        // Para simulação, vamos gerar um código fixo ou simples
        this.generatedCode = "123456"; // Código de verificação fixo para teste
    }

    // Método para injetar o usuário a ser verificado
    public void setUserToVerify(Usuario user) {
        this.userToVerify = user;
        System.out.println("DEBUG: Usuário para verificação: " + user.getNomeUsuario());
        System.out.println("DEBUG: Código de verificação (simulado): " + generatedCode);
    }

    @FXML
    private void handleVerify(ActionEvent event) {
        String enteredCode = txtVerificationCode.getText();

        if (enteredCode.isEmpty()) {
            lblMessage.setText("Por favor, digite o código de verificação.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (userToVerify == null) {
            lblMessage.setText("Erro: Nenhum usuário para verificar. Volte para o login.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (enteredCode.equals(generatedCode)) {
            try {
                userService.verificarUsuario(userToVerify.getId());
                lblMessage.setText("Conta verificada com sucesso!");
                lblMessage.setTextFill(javafx.scene.paint.Color.GREEN);
                // Redireciona para a tela de login após a verificação
                sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
            } catch (Exception e) {
                e.printStackTrace();
                lblMessage.setText("Erro ao verificar conta.");
                lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            }
        } else {
            lblMessage.setText("Código de verificação incorreto.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
    }
}