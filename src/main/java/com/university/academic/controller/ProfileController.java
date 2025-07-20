package com.university.academic.controller;

import com.university.academic.model.Usuario;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class ProfileController {
    @FXML private TextField txtUserId;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRole;
    @FXML private Label lblMatricula;
    @FXML private TextField txtMatricula;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmNewPassword;
    @FXML private Label lblMessage;

    private UserService userService;
    private SceneManager sceneManager;
    private Usuario currentUser;

    public ProfileController() {
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();
        this.currentUser = userService.getCurrentUser();
    }

    @FXML
    public void initialize() {
        if (currentUser != null) {
            txtUserId.setText(currentUser.getId());
            txtUsername.setText(currentUser.getNomeUsuario());
            txtEmail.setText(currentUser.getEmail());
            txtRole.setText(currentUser.getPapel());

            boolean isStudent = "Estudante".equalsIgnoreCase(currentUser.getPapel());
            lblMatricula.setVisible(isStudent);
            lblMatricula.setManaged(isStudent);
            txtMatricula.setVisible(isStudent);
            txtMatricula.setManaged(isStudent);
            if (isStudent && currentUser.getMatricula() != null) {
                txtMatricula.setText(currentUser.getMatricula());
            }
        } else {
            lblMessage.setText("Nenhum usuário logado.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            txtUsername.setDisable(true);
            txtEmail.setDisable(true);
            txtCurrentPassword.setDisable(true);
            txtNewPassword.setDisable(true);
            txtConfirmNewPassword.setDisable(true);
            // CORREÇÃO AQUI: Usando setManaged(false)
            lblMatricula.setVisible(false); lblMatricula.setManaged(false);
            txtMatricula.setVisible(false); txtMatricula.setManaged(false);
        }
    }

    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        if (currentUser == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para atualizar.");
            return;
        }

        String newUsername = txtUsername.getText();
        String newEmail = txtEmail.getText();
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmNewPassword = txtConfirmNewPassword.getText();
        String matricula = currentUser.getMatricula();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            lblMessage.setText("Nome de usuário e email não podem ser vazios.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            Usuario usuarioParaAtualizar = new Usuario(
                    currentUser.getId(),
                    newUsername,
                    currentUser.getSenha(),
                    newEmail,
                    currentUser.getPapel(),
                    currentUser.isVerified(),
                    matricula
            );
            userService.atualizarUsuario(usuarioParaAtualizar);

            if (!newPassword.isEmpty() || !confirmNewPassword.isEmpty()) {
                if (!newPassword.equals(confirmNewPassword)) {
                    lblMessage.setText("As novas senhas não coincidem.");
                    lblMessage.setTextFill(javafx.scene.paint.Color.RED);
                    return;
                }
                if (currentPassword.isEmpty()) {
                    lblMessage.setText("Para alterar a senha, a senha atual é obrigatória.");
                    lblMessage.setTextFill(javafx.scene.paint.Color.RED);
                    return;
                }
                userService.alterarSenha(currentUser.getId(), currentPassword, newPassword);
            }

            lblMessage.setText("Perfil atualizado com sucesso!");
            lblMessage.setTextFill(javafx.scene.paint.Color.GREEN);
            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmNewPassword.clear();

        } catch (IllegalArgumentException e) {
            lblMessage.setText(e.getMessage());
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Erro ao atualizar perfil.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        if (currentUser == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Nenhum usuário logado para excluir.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmação de Exclusão");
        dialog.setHeaderText("Excluir Conta Permanentemente");
        dialog.setContentText("Por favor, digite sua senha para confirmar a exclusão da conta:");
        dialog.getEditor().textProperty().addListener((obs, oldVal, newVal) -> dialog.getEditor().setText(newVal.replaceAll("\\s", "")));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(password -> {
            try {
                userService.excluirConta(currentUser.getId(), password);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Conta excluída com sucesso! Você será redirecionado para a tela de login.");
                sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Erro de Exclusão", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao excluir a conta.");
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/PlanoDeEnsinoListView.fxml", "Sistema de Gestão de Planos de Ensino", false);
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}