package com.university.academic.controller;

import com.university.academic.model.Usuario;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class UserManagementController {
    @FXML private TextField txtUserId;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbRole;
    @FXML private CheckBox chkVerified;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtNewPassword;
    @FXML private Label lblMessage;
    @FXML private TableView<Usuario> tabelaUsuarios;
    @FXML private TableColumn<Usuario, String> colUserId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRole;
    @FXML private TableColumn<Usuario, Boolean> colVerified;
    @FXML private TableColumn<Usuario, String> colMatricula;

    private UserService userService;
    private SceneManager sceneManager;
    private ObservableList<Usuario> listaObservavelUsuarios;

    public UserManagementController() {
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList("Estudante", "Professor", "Coordenador", "Diretor"));
        configurarTabela();

        if (!isDiretor()) {
            bloquearInterfaceNaoDiretor();
        } else {
            carregarUsuariosNaTabela();

            tabelaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    preencherCamposEdicao(newSelection);
                } else {
                    limparCampos();
                }
            });
        }
    }

    private void configurarTabela() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("nomeUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("papel"));
        colVerified.setCellValueFactory(new PropertyValueFactory<>("verified"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));

        listaObservavelUsuarios = FXCollections.observableArrayList();
        tabelaUsuarios.setItems(listaObservavelUsuarios);
    }

    private void carregarUsuariosNaTabela() {
        listaObservavelUsuarios.clear();
        // Apenas Diretor pode carregar todos os usuários
        Usuario usuarioLogado = userService.getUsuarioLogado();
        if (usuarioLogado != null && "Diretor".equalsIgnoreCase(usuarioLogado.getPapel())) {
            listaObservavelUsuarios.addAll(userService.buscarTodosUsuarios());
        }
    }

    private void preencherCamposEdicao(Usuario usuario) {
        txtUserId.setText(usuario.getId());
        txtUsername.setText(usuario.getNomeUsuario());
        txtEmail.setText(usuario.getEmail());
        cmbRole.getSelectionModel().select(usuario.getPapel());
        chkVerified.setSelected(usuario.isVerified());
        txtMatricula.setText(usuario.getMatricula() != null ? usuario.getMatricula() : "");
        txtNewPassword.clear();
        lblMessage.setText("");
    }

    @FXML
    private void handleUpdateUser(ActionEvent event) {
        if (txtUserId.getText().isEmpty()) {
            setErrorMessage("Selecione um usuário na tabela para atualizar.");
            return;
        }

        String userId = txtUserId.getText();
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String role = cmbRole.getValue();
        boolean verified = chkVerified.isSelected();
        String newPassword = txtNewPassword.getText();
        String matricula = txtMatricula.getText();

        try {
            Usuario usuarioLogado = userService.getUsuarioLogado();
            if (usuarioLogado == null || !"Diretor".equalsIgnoreCase(usuarioLogado.getPapel())) {
                setErrorMessage("Você não tem permissão para atualizar usuários.");
                return;
            }

            Optional<Usuario> originalUserOpt = userService.buscarTodosUsuarios().stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst();
            if (!originalUserOpt.isPresent()) {
                setErrorMessage("Usuário não encontrado para atualização.");
                return;
            }

            Usuario originalUser = originalUserOpt.get();

            if (usuarioLogado.getId().equals(userId) && !"Diretor".equalsIgnoreCase(role)) {
                setErrorMessage("Você não pode remover seu próprio papel de Diretor.");
                return;
            }

            Usuario usuarioAtualizado = new Usuario(
                    userId,
                    username,
                    originalUser.getSenha(),
                    email,
                    role,
                    verified,
                    matricula
            );

            userService.atualizarUsuario(usuarioAtualizado);

            if (!newPassword.isEmpty()) {
                userService.alterarSenha(userId, originalUser.getSenha(), newPassword);
            }

            setSuccessMessage("Usuário atualizado com sucesso!");
            carregarUsuariosNaTabela();
            limparCampos();

        } catch (IllegalArgumentException e) {
            setErrorMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            setErrorMessage("Erro ao atualizar usuário.");
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        if (txtUserId.getText().isEmpty()) {
            setErrorMessage("Selecione um usuário na tabela para excluir.");
            return;
        }

        Usuario usuarioLogado = userService.getUsuarioLogado();
        if (usuarioLogado == null || !"Diretor".equalsIgnoreCase(usuarioLogado.getPapel())) {
            setErrorMessage("Você não tem permissão para excluir usuários.");
            return;
        }

        String userId = txtUserId.getText();
        String username = txtUsername.getText();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja excluir o usuário " + username + "? Esta ação é irreversível.",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                userService.excluirContaPorAdmin(userId);
                setSuccessMessage("Usuário " + username + " excluído com sucesso!");
                carregarUsuariosNaTabela();
                limparCampos();
            } catch (IllegalArgumentException e) {
                setErrorMessage(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                setErrorMessage("Erro ao excluir usuário.");
            }
        }
    }

    @FXML
    private void handleClearFields(ActionEvent event) {
        limparCampos();
    }

    private void limparCampos() {
        txtUserId.clear();
        txtUsername.clear();
        txtEmail.clear();
        cmbRole.getSelectionModel().clearSelection();
        chkVerified.setSelected(false);
        txtMatricula.clear();
        txtNewPassword.clear();
        lblMessage.setText("");
        tabelaUsuarios.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/PlanoDeEnsinoListView.fxml", "Sistema de Gestão de Planos de Ensino", true);
    }

    private boolean isDiretor() {
        Usuario usuario = userService.getUsuarioLogado();
        return usuario != null && "Diretor".equalsIgnoreCase(usuario.getPapel());
    }

    private void bloquearInterfaceNaoDiretor() {
        tabelaUsuarios.setDisable(true);
        txtUserId.setDisable(true);
        txtUsername.setDisable(true);
        txtEmail.setDisable(true);
        cmbRole.setDisable(true);
        chkVerified.setDisable(true);
        txtMatricula.setDisable(true);
        txtNewPassword.setDisable(true);
        lblMessage.setText("Acesso restrito: apenas diretores podem gerenciar usuários.");
        lblMessage.setTextFill(javafx.scene.paint.Color.RED);
    }

    private void setErrorMessage(String msg) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(javafx.scene.paint.Color.RED);
    }

    private void setSuccessMessage(String msg) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(javafx.scene.paint.Color.GREEN);
    }
}
