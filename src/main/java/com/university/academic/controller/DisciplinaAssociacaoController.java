package com.university.academic.controller;

import com.university.academic.model.AssociacaoDisciplinaUsuario;
import com.university.academic.model.Disciplina;
import com.university.academic.model.Usuario;
import com.university.academic.service.AssociacaoService;
import com.university.academic.service.DisciplinaService;
import com.university.academic.service.Observer;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DisciplinaAssociacaoController implements Observer {
    @FXML private TextField txtDisciplinaId;
    @FXML private TextField txtDisciplinaCodigo;
    @FXML private TextField txtDisciplinaNome;
    @FXML private TextField txtDisciplinaCargaHoraria;
    @FXML private TextField txtDisciplinaDescricao;
    @FXML private Label lblDisciplinaMessage;
    @FXML private TableView<Disciplina> tabelaDisciplinas;
    @FXML private TableColumn<Disciplina, String> colDisciplinaId;
    @FXML private TableColumn<Disciplina, String> colDisciplinaCodigo;
    @FXML private TableColumn<Disciplina, String> colDisciplinaNome;
    @FXML private TableColumn<Disciplina, Integer> colDisciplinaCargaHoraria;
    @FXML private TableColumn<Disciplina, String> colDisciplinaDescricao;
    @FXML private TableColumn<Disciplina, Void> colDisciplinaAcoes;

    @FXML private ComboBox<Disciplina> cmbAssociacaoDisciplina;
    @FXML private ComboBox<Usuario> cmbAssociacaoUsuario;
    @FXML private ComboBox<String> cmbAssociacaoPapel;
    @FXML private Label lblAssociacaoMessage;
    @FXML private TableView<AssociacaoDisciplinaUsuario> tabelaAssociacoes;
    @FXML private TableColumn<AssociacaoDisciplinaUsuario, String> colAssociacaoId;
    @FXML private TableColumn<AssociacaoDisciplinaUsuario, String> colAssociacaoDisciplina;
    @FXML private TableColumn<AssociacaoDisciplinaUsuario, String> colAssociacaoUsuario;
    @FXML private TableColumn<AssociacaoDisciplinaUsuario, String> colAssociacaoPapel;
    @FXML private TableColumn<AssociacaoDisciplinaUsuario, String> colAssociacaoPlano;
    @FXML private TableColumn<AssociacaoDisciplinaUsuario, Void> colAssociacaoAcoes;

    private DisciplinaService disciplinaService;
    private AssociacaoService associacaoService;
    private UserService userService;
    private SceneManager sceneManager;

    private ObservableList<Disciplina> listaObservavelDisciplinas;
    private ObservableList<AssociacaoDisciplinaUsuario> listaObservavelAssociacoes;

    public DisciplinaAssociacaoController() {
        this.disciplinaService = DisciplinaService.getInstance();
        this.associacaoService = AssociacaoService.getInstance();
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();

        this.associacaoService.addObserver(this);
    }

    @FXML
    public void initialize() {
        configurarTabelaDisciplinas();
        carregarDisciplinasNaTabela();

        configurarTabelaAssociacoes();
        carregarAssociacoesNaTabela();
        popularComboBoxesAssociacao();

        tabelaDisciplinas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                preencherCamposDisciplina(newSelection);
            } else {
                limparCamposDisciplina();
            }
        });

        cmbAssociacaoPapel.setItems(FXCollections.observableArrayList("Professor", "Estudante"));
        cmbAssociacaoPapel.getSelectionModel().selectedItemProperty().addListener((obs, oldPapel, newPapel) -> {
            popularComboBoxUsuariosPorPapel(newPapel);
        });
        cmbAssociacaoPapel.getSelectionModel().selectFirst();
    }

    private void configurarTabelaDisciplinas() {
        colDisciplinaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDisciplinaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDisciplinaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDisciplinaCargaHoraria.setCellValueFactory(new PropertyValueFactory<>("cargaHoraria"));
        colDisciplinaDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        listaObservavelDisciplinas = FXCollections.observableArrayList();
        tabelaDisciplinas.setItems(listaObservavelDisciplinas);

        colDisciplinaAcoes.setCellFactory(param -> new TableCell<Disciplina, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");

            {
                btnEditar.setOnAction(event -> {
                    Disciplina disciplina = getTableView().getItems().get(getIndex());
                    preencherCamposDisciplina(disciplina);
                });
                btnExcluir.setOnAction(event -> {
                    Disciplina disciplina = getTableView().getItems().get(getIndex());
                    excluirDisciplina(disciplina.getId());
                });

                btnEditar.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
                btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(btnEditar, btnExcluir);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void carregarDisciplinasNaTabela() {
        listaObservavelDisciplinas.clear();
        listaObservavelDisciplinas.addAll(disciplinaService.buscarTodasDisciplinas());
    }

    private void preencherCamposDisciplina(Disciplina disciplina) {
        txtDisciplinaId.setText(disciplina.getId());
        txtDisciplinaCodigo.setText(disciplina.getCodigo());
        txtDisciplinaNome.setText(disciplina.getNome());
        txtDisciplinaCargaHoraria.setText(String.valueOf(disciplina.getCargaHoraria()));
        txtDisciplinaDescricao.setText(disciplina.getDescricao());
        lblDisciplinaMessage.setText("");
    }

    @FXML
    private void handleSalvarDisciplina(ActionEvent event) {
        try {
            String id = txtDisciplinaId.getText();
            String codigo = txtDisciplinaCodigo.getText();
            String nome = txtDisciplinaNome.getText();
            int cargaHoraria = Integer.parseInt(txtDisciplinaCargaHoraria.getText());
            String descricao = txtDisciplinaDescricao.getText();

            Disciplina disciplina = new Disciplina(id.isEmpty() ? null : id, codigo, nome, cargaHoraria, descricao);
            disciplinaService.criarOuAtualizarDisciplina(disciplina);

            lblDisciplinaMessage.setText("Disciplina salva com sucesso!");
            lblDisciplinaMessage.setTextFill(javafx.scene.paint.Color.GREEN);
            carregarDisciplinasNaTabela();
            limparCamposDisciplina();
            popularComboBoxesAssociacao();
        } catch (NumberFormatException e) {
            lblDisciplinaMessage.setText("Carga Horária deve ser um número válido.");
            lblDisciplinaMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (IllegalArgumentException e) {
            lblDisciplinaMessage.setText(e.getMessage());
            lblDisciplinaMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
            lblDisciplinaMessage.setText("Erro ao salvar disciplina.");
            lblDisciplinaMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleLimparDisciplina(ActionEvent event) {
        limparCamposDisciplina();
    }

    private void limparCamposDisciplina() {
        txtDisciplinaId.clear();
        txtDisciplinaCodigo.clear();
        txtDisciplinaNome.clear();
        txtDisciplinaCargaHoraria.clear();
        txtDisciplinaDescricao.clear();
        lblDisciplinaMessage.setText("");
        tabelaDisciplinas.getSelectionModel().clearSelection();
    }

    private void excluirDisciplina(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir esta disciplina? Isso pode afetar associações.", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                disciplinaService.excluirDisciplina(id);
                lblDisciplinaMessage.setText("Disciplina excluída com sucesso!");
                lblDisciplinaMessage.setTextFill(javafx.scene.paint.Color.GREEN);
                carregarDisciplinasNaTabela();
                limparCamposDisciplina();
                popularComboBoxesAssociacao();
            } catch (Exception e) {
                e.printStackTrace();
                lblDisciplinaMessage.setText("Erro ao excluir disciplina: " + e.getMessage());
                lblDisciplinaMessage.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
    }

    // --- Métodos da Seção de Associações ---
    private void configurarTabelaAssociacoes() {
        colAssociacaoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAssociacaoDisciplina.setCellValueFactory(new PropertyValueFactory<>("nomeDisciplina"));
        colAssociacaoUsuario.setCellValueFactory(new PropertyValueFactory<>("nomeUsuario"));
        colAssociacaoPapel.setCellValueFactory(new PropertyValueFactory<>("papelUsuario"));
        colAssociacaoPlano.setCellValueFactory(new PropertyValueFactory<>("idPlanoDeEnsino"));

        listaObservavelAssociacoes = FXCollections.observableArrayList();
        tabelaAssociacoes.setItems(listaObservavelAssociacoes);

        colAssociacaoAcoes.setCellFactory(param -> new TableCell<AssociacaoDisciplinaUsuario, Void>() {
            private final Button btnExcluir = new Button("Excluir");

            {
                btnExcluir.setOnAction(event -> {
                    AssociacaoDisciplinaUsuario associacao = getTableView().getItems().get(getIndex());
                    excluirAssociacao(associacao.getId());
                });
                btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 3;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnExcluir);
                }
            }
        });
    }

    private void carregarAssociacoesNaTabela() {
        listaObservavelAssociacoes.clear();
        listaObservavelAssociacoes.addAll(associacaoService.buscarTodasAssociacoes());
    }

    private void popularComboBoxesAssociacao() {
        cmbAssociacaoDisciplina.setItems(FXCollections.observableArrayList(disciplinaService.buscarTodasDisciplinas()));
    }

    private void popularComboBoxUsuariosPorPapel(String papel) {
        List<Usuario> usuariosFiltrados = userService.buscarTodosUsuarios().stream()
                .filter(u -> u.getPapel().equalsIgnoreCase(papel))
                .collect(Collectors.toList());
        cmbAssociacaoUsuario.setItems(FXCollections.observableArrayList(usuariosFiltrados));
        cmbAssociacaoUsuario.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAssociar(ActionEvent event) {
        Disciplina selectedDisciplina = cmbAssociacaoDisciplina.getSelectionModel().getSelectedItem();
        Usuario selectedUsuario = cmbAssociacaoUsuario.getSelectionModel().getSelectedItem();
        String selectedPapel = cmbAssociacaoPapel.getSelectionModel().getSelectedItem();

        if (selectedDisciplina == null || selectedUsuario == null || selectedPapel == null) {
            lblAssociacaoMessage.setText("Selecione a disciplina, o usuário e o papel na associação.");
            lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            associacaoService.criarOuAtualizarAssociacao(null, selectedDisciplina.getId(), selectedUsuario.getId(), selectedPapel);
            lblAssociacaoMessage.setText("Associação criada com sucesso!");
            lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.GREEN);
            limparCamposAssociacao();
        } catch (SecurityException e) {
            lblAssociacaoMessage.setText("Erro de Permissão: " + e.getMessage());
            lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (IllegalArgumentException e) {
            lblAssociacaoMessage.setText(e.getMessage());
            lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
            lblAssociacaoMessage.setText("Erro ao criar associação.");
            lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleLimparAssociacao(ActionEvent event) {
        limparCamposAssociacao();
    }

    private void limparCamposAssociacao() {
        cmbAssociacaoDisciplina.getSelectionModel().clearSelection();
        cmbAssociacaoUsuario.getSelectionModel().clearSelection();
        cmbAssociacaoPapel.getSelectionModel().selectFirst();
        lblAssociacaoMessage.setText("");
        tabelaAssociacoes.getSelectionModel().clearSelection();
    }

    private void excluirAssociacao(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir esta associação?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                associacaoService.excluirAssociacao(id);
                lblAssociacaoMessage.setText("Associação excluída com sucesso!");
                lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.GREEN);
            } catch (SecurityException e) {
                lblAssociacaoMessage.setText("Erro de Permissão: " + e.getMessage());
                lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
            } catch (IllegalArgumentException e) {
                lblAssociacaoMessage.setText(e.getMessage());
                lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
            } catch (Exception e) {
                e.printStackTrace();
                lblAssociacaoMessage.setText("Erro ao excluir associação.");
                lblAssociacaoMessage.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        sceneManager.loadScene("/com/university/academic/view/PlanoDeEnsinoListView.fxml", "Sistema de Gestão de Planos de Ensino", true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Object data) {
        if (data instanceof List) {
            List<AssociacaoDisciplinaUsuario> associacoesAtualizadas = (List<AssociacaoDisciplinaUsuario>) data;
            if (listaObservavelAssociacoes != null) {
                listaObservavelAssociacoes.setAll(associacoesAtualizadas);
            }
        }
    }
}