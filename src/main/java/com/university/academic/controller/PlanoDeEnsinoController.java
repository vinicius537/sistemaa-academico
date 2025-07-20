package com.university.academic.controller;

import com.university.academic.model.PlanoDeEnsino;
import com.university.academic.model.Usuario;
import com.university.academic.service.PlanoDeEnsinoService;
import com.university.academic.service.Observer;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PlanoDeEnsinoController implements Observer {
    @FXML private TableView<PlanoDeEnsino> tabelaPlanos;
    @FXML private TableColumn<PlanoDeEnsino, String> colCodigoDisciplina;
    @FXML private TableColumn<PlanoDeEnsino, String> colNomeDisciplina;
    @FXML private TableColumn<PlanoDeEnsino, String> colSemestre;
    @FXML private TableColumn<PlanoDeEnsino, String> colProfessor;
    @FXML private TableColumn<PlanoDeEnsino, String> colStatus;
    @FXML private TableColumn<PlanoDeEnsino, Void> colAcoes;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtId;
    @FXML private TextField txtCodigoDisciplina;
    @FXML private TextField txtNomeDisciplina;
    @FXML private TextField txtSemestreLetivo;
    @FXML private TextField txtProfessorResponsavel;
    @FXML private TextArea txtEmenta;
    @FXML private TextArea txtObjetivos;
    @FXML private TextArea txtConteudoProgramatico;
    @FXML private TextArea txtMetodologia;
    @FXML private TextArea txtAvaliacao;
    @FXML private TextArea txtBibliografiaBasica;
    @FXML private TextArea txtBibliografiaComplementar;
    @FXML private TextField txtCaminhoPdf;
    @FXML private Button btnSelecionarPdf;

    @FXML private Button btnMeuPerfil;
    @FXML private Button btnNovoPlano;
    @FXML private Button btnGerenciarUsuarios;
    @FXML private Button btnGerenciarDisciplinas;
    @FXML private Button btnLogout;

    private PlanoDeEnsinoService planoDeEnsinoService;
    private UserService userService;
    private SceneManager sceneManager;
    private ObservableList<PlanoDeEnsino> listaObservavelPlanos;
    private Stage formStage;
    private File selectedPdfFile;

    public PlanoDeEnsinoController() {
        this.planoDeEnsinoService = PlanoDeEnsinoService.getInstance();
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();
        this.planoDeEnsinoService.addObserver(this);
    }

    @FXML
    public void initialize() {
        if (tabelaPlanos != null) {
            configurarTabela();
            carregarPlanosNaTabela();
            configurarAcessoPorPapel();
        }
        if (txtProfessorResponsavel != null) {
            Usuario currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                txtProfessorResponsavel.setText(currentUser.getNomeUsuario());
            }
        }
    }

    private void configurarTabela() {
        colCodigoDisciplina.setCellValueFactory(new PropertyValueFactory<>("codigoDisciplina"));
        colNomeDisciplina.setCellValueFactory(new PropertyValueFactory<>("nomeDisciplina"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestreLetivo"));
        colProfessor.setCellValueFactory(new PropertyValueFactory<>("professorResponsavel"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        listaObservavelPlanos = FXCollections.observableArrayList();
        tabelaPlanos.setItems(listaObservavelPlanos);

        colAcoes.setCellFactory(param -> new TableCell<PlanoDeEnsino, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final Button btnSubmeter = new Button("Submeter");
            private final Button btnAprovar = new Button("Aprovar");
            private final Button btnVerPdf = new Button("Ver PDF");
            private final Button btnVerDetalhes = new Button("Detalhes");

            {
                btnEditar.setOnAction(event -> {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    abrirFormularioPlano(plano);
                });
                btnExcluir.setOnAction(event -> {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    excluirPlano(plano.getId());
                });
                btnSubmeter.setOnAction(event -> {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    submeterPlano(plano.getId());
                });
                btnAprovar.setOnAction(event -> {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    aprovarPlano(plano.getId());
                });
                btnVerPdf.setOnAction(event -> {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    verPdf(plano.getCaminhoPdf());
                });
                btnVerDetalhes.setOnAction(event -> {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    handleVerDetalhes(plano);
                });

                btnEditar.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
                btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
                btnSubmeter.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
                btnAprovar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
                btnVerPdf.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
                btnVerDetalhes.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PlanoDeEnsino plano = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);

                    Usuario currentUser = userService.getCurrentUser();
                    if (currentUser != null) {
                        String papel = currentUser.getPapel();

                        if ("Professor".equalsIgnoreCase(papel) || "Coordenador".equalsIgnoreCase(papel) || "Diretor".equalsIgnoreCase(papel)) {
                            buttons.getChildren().addAll(btnEditar, btnExcluir);
                            if ("Rascunho".equals(plano.getStatus()) && "Professor".equalsIgnoreCase(papel) && plano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                                buttons.getChildren().add(btnSubmeter);
                            }
                            if ("Submetido".equals(plano.getStatus()) && ("Coordenador".equalsIgnoreCase(papel) || "Diretor".equalsIgnoreCase(papel))) {
                                buttons.getChildren().add(btnAprovar);
                            }
                        }
                        if (plano.getCaminhoPdf() != null && !plano.getCaminhoPdf().isEmpty() && "Aprovado".equals(plano.getStatus())) {
                            buttons.getChildren().add(btnVerPdf);
                        }
                        buttons.getChildren().add(btnVerDetalhes);
                    }
                    setGraphic(buttons);
                }
            }
        });
    }

    private void carregarPlanosNaTabela() {
        listaObservavelPlanos.clear();
        listaObservavelPlanos.addAll(planoDeEnsinoService.buscarTodosPlanos());
    }

    private void configurarAcessoPorPapel() {
        Usuario currentUser = userService.getCurrentUser();
        System.out.println("DEBUG - configurarAcessoPorPapel: Início."); // DEBUG
        if (currentUser != null) {
            String papel = currentUser.getPapel();
            System.out.println("DEBUG - Usuário logado: " + currentUser.getNomeUsuario() + ", Papel: " + papel); // DEBUG

            boolean canCreatePlan = "Professor".equalsIgnoreCase(papel) || "Coordenador".equalsIgnoreCase(papel) || "Diretor".equalsIgnoreCase(papel);
            boolean isCoordinatorOrDirector = "Coordenador".equalsIgnoreCase(papel) || "Diretor".equalsIgnoreCase(papel);
            boolean isDirector = "Diretor".equalsIgnoreCase(papel);

            System.out.println("DEBUG - canCreatePlan: " + canCreatePlan); // DEBUG
            System.out.println("DEBUG - isCoordinatorOrDirector: " + isCoordinatorOrDirector); // DEBUG
            System.out.println("DEBUG - isDirector: " + isDirector); // DEBUG

            btnNovoPlano.setVisible(canCreatePlan);
            btnNovoPlano.setManaged(canCreatePlan);

            btnGerenciarUsuarios.setVisible(isDirector);
            btnGerenciarUsuarios.setManaged(isDirector);

            btnGerenciarDisciplinas.setVisible(isCoordinatorOrDirector);
            btnGerenciarDisciplinas.setManaged(isCoordinatorOrDirector);

            btnLogout.setVisible(true);
            btnLogout.setManaged(true);
        } else {
            System.out.println("DEBUG - Nenhum usuário logado. Ocultando todos os botões de função."); // DEBUG
            btnNovoPlano.setVisible(false);
            btnNovoPlano.setManaged(false);
            btnGerenciarUsuarios.setVisible(false);
            btnGerenciarUsuarios.setManaged(false);
            btnGerenciarDisciplinas.setVisible(false);
            btnGerenciarDisciplinas.setManaged(false);
            btnLogout.setVisible(false);
            btnLogout.setManaged(false);
        }
        System.out.println("DEBUG - configurarAcessoPorPapel: Fim."); // DEBUG
    }

    @FXML
    private void handleNovoPlano(ActionEvent event) {
        abrirFormularioPlano(null);
    }

    @FXML
    private void handleMeuPerfil(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/ProfileView.fxml", "Meu Perfil de Usuário", false);
    }

    @FXML
    private void handleGerenciarUsuarios(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/UserManagementView.fxml", "Gerenciamento de Usuários (Diretor)", false);
    }

    @FXML
    private void handleGerenciarDisciplinas(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/DisciplinaAssociacaoView.fxml", "Gerenciamento de Disciplinas e Associações", false);
    }

    private void handleVerDetalhes(PlanoDeEnsino plano) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/academic/view/PlanoDeEnsinoDetailView.fxml"));
            Parent root = loader.load();

            PlanoDeEnsinoDetailController detailController = loader.getController();
            Stage stage = new Stage();
            detailController.setDetailStage(stage);
            detailController.setPlanoDeEnsino(plano);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detalhes do Plano de Ensino: " + plano.getNomeDisciplina());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de detalhes do plano.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        userService.logout();
        sceneManager.loadScene("/com/university/academic/view/LoginView.fxml", "Login no Sistema", false);
    }

    @FXML
    private void handleSelecionarPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Arquivo PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf")
        );
        Stage stage = (Stage) btnSelecionarPdf.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedPdfFile = file;
            txtCaminhoPdf.setText(file.getName());
        }
    }

    private void abrirFormularioPlano(PlanoDeEnsino plano) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/university/academic/view/PlanoDeEnsinoFormView.fxml"));
            Parent root = loader.load();

            PlanoDeEnsinoController formController = loader.getController();
            formController.planoDeEnsinoService = this.planoDeEnsinoService;
            formController.userService = this.userService;
            formController.formStage = new Stage();
            formController.formStage.initModality(Modality.APPLICATION_MODAL);
            formController.formStage.setTitle(plano == null ? "Novo Plano de Ensino" : "Editar Plano de Ensino");
            formController.formStage.setScene(new Scene(root));

            if (plano != null) {
                formController.preencherFormulario(plano);
                formController.lblTituloForm.setText("Editar Plano de Ensino");
                if (plano.getCaminhoPdf() != null && !plano.getCaminhoPdf().isEmpty()) {
                    formController.txtCaminhoPdf.setText(plano.getCaminhoPdf());
                }
            } else {
                formController.lblTituloForm.setText("Novo Plano de Ensino");
                formController.txtProfessorResponsavel.setText(userService.getCurrentUser().getNomeUsuario());
            }

            formController.formStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao abrir formulário", "Não foi possível carregar a tela do formulário.");
        }
    }

    private void preencherFormulario(PlanoDeEnsino plano) {
        txtId.setText(plano.getId());
        txtCodigoDisciplina.setText(plano.getCodigoDisciplina());
        txtNomeDisciplina.setText(plano.getNomeDisciplina());
        txtSemestreLetivo.setText(plano.getSemestreLetivo());
        txtProfessorResponsavel.setText(plano.getProfessorResponsavel());
        txtEmenta.setText(plano.getEmenta());
        txtObjetivos.setText(plano.getObjetivos());
        txtConteudoProgramatico.setText(plano.getConteudoProgramatico());
        txtMetodologia.setText(plano.getMetodologia());
        txtAvaliacao.setText(plano.getAvaliacao());
        txtBibliografiaBasica.setText(plano.getBibliografiaBasica());
        txtBibliografiaComplementar.setText(plano.getBibliografiaComplementar());
        txtCaminhoPdf.setText(plano.getCaminhoPdf() != null ? plano.getCaminhoPdf() : "");
    }

    @FXML
    private void handleSalvarPlano(ActionEvent event) {
        try {
            String id = txtId.getText();
            PlanoDeEnsino plano = new PlanoDeEnsino(
                    id.isEmpty() ? null : id,
                    txtCodigoDisciplina.getText(),
                    txtNomeDisciplina.getText(),
                    txtSemestreLetivo.getText(),
                    txtEmenta.getText(),
                    txtObjetivos.getText(),
                    txtConteudoProgramatico.getText(),
                    txtMetodologia.getText(),
                    txtAvaliacao.getText(),
                    txtBibliografiaBasica.getText(),
                    txtBibliografiaComplementar.getText(),
                    txtProfessorResponsavel.getText(),
                    selectedPdfFile != null ? selectedPdfFile.getName() : (id.isEmpty() ? null : planoDeEnsinoService.buscarPlanoPorId(id).map(p -> p.getCaminhoPdf()).orElse(null))
            );

            planoDeEnsinoService.criarOuAtualizarPlano(plano, selectedPdfFile);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Plano de Ensino salvo com sucesso!");
            if (formStage != null) {
                formStage.close();
            }
        } catch (SecurityException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de Permissão", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Erro de Validação", e.getMessage());
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de Arquivo", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao salvar o plano de ensino.");
        } finally {
            selectedPdfFile = null;
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        if (formStage != null) {
            formStage.close();
        }
        selectedPdfFile = null;
    }

    private void excluirPlano(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir este plano?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                planoDeEnsinoService.excluirPlano(id);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Plano de Ensino excluído com sucesso!");
            } catch (SecurityException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro de Permissão", e.getMessage());
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Erro", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao excluir o plano.");
            }
        }
    }

    private void submeterPlano(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja submeter este plano para aprovação?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                planoDeEnsinoService.submeterParaAprovacao(id);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Plano de Ensino submetido para aprovação!");
            } catch (SecurityException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro de Permissão", e.getMessage());
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Erro de Status", e.getMessage());
            }
        }
    }

    private void aprovarPlano(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja aprovar este plano?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                planoDeEnsinoService.aprovarPlano(id);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Plano de Ensino aprovado!");
            } catch (SecurityException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro de Permissão", e.getMessage());
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Erro de Status", e.getMessage());
            }
        }
    }

    private void verPdf(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Não Encontrado", "Nenhum arquivo PDF associado a este plano ou o caminho está vazio.");
            return;
        }
        File pdfFile = new File(planoDeEnsinoService.getPdfUploadDir(), fileName);
        if (pdfFile.exists() && pdfFile.isFile()) {
            try {
                java.awt.Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao Abrir PDF", "Não foi possível abrir o arquivo PDF. Verifique se você tem um leitor de PDF instalado e se o arquivo não está corrompido.");
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao Abrir PDF", "Operação não suportada pelo sistema operacional para abrir arquivos.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Arquivo PDF Ausente", "O arquivo PDF '" + fileName + "' não foi encontrado na pasta de uploads.");
        }
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Object data) {
        if (data instanceof List) {
            List<PlanoDeEnsino> planosAtualizados = (List<PlanoDeEnsino>) data;
            if (listaObservavelPlanos != null) {
                listaObservavelPlanos.setAll(planosAtualizados);
            }
        }
    }
}