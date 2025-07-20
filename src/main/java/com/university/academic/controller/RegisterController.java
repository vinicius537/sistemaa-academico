package com.university.academic.controller;

import com.university.academic.model.Usuario;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.Optional;

public class RegisterController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Label lblMessage;
    @FXML private Label lblMatricula;
    @FXML private TextField txtMatricula;

    private UserService userService;
    private SceneManager sceneManager;

    public RegisterController() {
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList("Estudante", "Professor", "Coordenador", "Diretor"));
        cmbRole.getSelectionModel().select("Estudante");

        cmbRole.getSelectionModel().selectedItemProperty().addListener((obs, oldRole, newRole) -> {
            boolean isStudent = "Estudante".equalsIgnoreCase(newRole);
            lblMatricula.setVisible(isStudent);
            lblMatricula.setManaged(isStudent);
            txtMatricula.setVisible(isStudent);
            txtMatricula.setManaged(isStudent);
        });
        boolean isStudentInitial = "Estudante".equalsIgnoreCase(cmbRole.getValue());
        lblMatricula.setVisible(isStudentInitial);
        lblMatricula.setManaged(isStudentInitial);
        txtMatricula.setVisible(isStudentInitial);
        txtMatricula.setManaged(isStudentInitial);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String role = cmbRole.getValue();
        String matricula = txtMatricula.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            lblMessage.setText("Todos os campos são obrigatórios.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (!password.equals(confirmPassword)) {
            lblMessage.setText("As senhas não coincidem.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if ("Estudante".equalsIgnoreCase(role) && (matricula == null || matricula.trim().isEmpty())) {
            lblMessage.setText("Matrícula é obrigatória para Estudantes.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            userService.registrarNovoUsuario(username, password, email, role, matricula.trim()); // trim() para remover espaços extras
            lblMessage.setText("Usuário registrado com sucesso! Redirecionando para verificação...");
            lblMessage.setTextFill(javafx.scene.paint.Color.GREEN);

            Optional<Usuario> registeredUserOpt = userService.buscarUsuarioPorNomeUsuario(username);

            if (registeredUserOpt.isPresent()) {
                Usuario registeredUser = registeredUserOpt.get();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/academic/view/VerificationView.fxml"));
                Parent root = loader.load();
                VerificationController verificationController = loader.getController();
                verificationController.setUserToVerify(registeredUser);

                Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(new Scene(root));
                currentStage.setTitle("Verificação de Conta");
                currentStage.show();

            } else {
                sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
            }

        } catch (IllegalArgumentException e) {
            lblMessage.setText(e.getMessage());
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (IOException e) {
            e.printStackTrace();
            lblMessage.setText("Erro ao carregar tela de verificação.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Erro ao registrar usuário.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
    }
}