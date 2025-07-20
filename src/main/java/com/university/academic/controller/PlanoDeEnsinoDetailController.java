package com.university.academic.controller;

import com.university.academic.model.Comentario;
import com.university.academic.model.PlanoDeEnsino;
import com.university.academic.model.Usuario;
import com.university.academic.service.ComentarioService;
import com.university.academic.service.Observer;
import com.university.academic.service.PlanoDeEnsinoService;
import com.university.academic.service.SceneManager;
import com.university.academic.service.UserService;

import javafx.application.Platform; // Para rodar atualizações na UI thread
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser; // Import para FileChooser
import javafx.stage.Stage; // Import para Stage (para FileChooser e fechar a própria janela)

import java.io.File; // Import para File
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.geometry.Insets;

public class PlanoDeEnsinoDetailController implements Observer {
    @FXML private Label lblTituloPlano;
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
    @FXML private Button btnVerPdf;

    @FXML private TextArea txtNovoComentario;
    @FXML private Button btnEnviarComentario;
    @FXML private Button btnLimparComentario;
    @FXML private Label lblComentarioMessage;
    @FXML private VBox vboxComentarios;
    @FXML private Label lblNoComments;

    private PlanoDeEnsinoService planoDeEnsinoService;
    private ComentarioService comentarioService;
    private UserService userService;
    private SceneManager sceneManager;

    private PlanoDeEnsino planoAtual;
    private Stage detailStage;

    public PlanoDeEnsinoDetailController() {
        this.planoDeEnsinoService = PlanoDeEnsinoService.getInstance();
        this.comentarioService = ComentarioService.getInstance();
        this.userService = UserService.getInstance();
        this.sceneManager = SceneManager.getInstance();

        this.comentarioService.addObserver(this);
    }

    public void setDetailStage(Stage stage) {
        this.detailStage = stage;
    }

    public void setPlanoDeEnsino(PlanoDeEnsino plano) {
        this.planoAtual = plano;
        preencherDetalhesPlano();
        carregarComentarios();
        configurarAcessoComentarios();
    }

    @FXML
    public void initialize() {
    }

    private void preencherDetalhesPlano() {
        if (planoAtual != null) {
            lblTituloPlano.setText("Detalhes do Plano: " + planoAtual.getNomeDisciplina());
            txtId.setText(planoAtual.getId());
            txtCodigoDisciplina.setText(planoAtual.getCodigoDisciplina());
            txtNomeDisciplina.setText(planoAtual.getNomeDisciplina());
            txtSemestreLetivo.setText(planoAtual.getSemestreLetivo());
            txtProfessorResponsavel.setText(planoAtual.getProfessorResponsavel());
            txtEmenta.setText(planoAtual.getEmenta());
            txtObjetivos.setText(planoAtual.getObjetivos());
            txtConteudoProgramatico.setText(planoAtual.getConteudoProgramatico());
            txtMetodologia.setText(planoAtual.getMetodologia());
            txtAvaliacao.setText(planoAtual.getAvaliacao());
            txtBibliografiaBasica.setText(planoAtual.getBibliografiaBasica());
            txtBibliografiaComplementar.setText(planoAtual.getBibliografiaComplementar());
            txtCaminhoPdf.setText(planoAtual.getCaminhoPdf() != null ? planoAtual.getCaminhoPdf() : "Nenhum arquivo associado");

            // Habilita/desabilita o botão Ver PDF
            btnVerPdf.setDisable(planoAtual.getCaminhoPdf() == null || planoAtual.getCaminhoPdf().isEmpty() || !"Aprovado".equals(planoAtual.getStatus()));
        }
    }

    private void configurarAcessoComentarios() {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            btnEnviarComentario.setDisable(true);
            btnLimparComentario.setDisable(true);
            txtNovoComentario.setDisable(true);
            lblComentarioMessage.setText("Faça login para adicionar comentários.");
            lblComentarioMessage.setTextFill(Color.GRAY);
            return;
        }

        boolean canComment = "Estudante".equalsIgnoreCase(currentUser.getPapel()) || "Professor".equalsIgnoreCase(currentUser.getPapel());
        btnEnviarComentario.setDisable(!canComment);
        btnLimparComentario.setDisable(!canComment);
        txtNovoComentario.setDisable(!canComment);

        if (!canComment) {
            lblComentarioMessage.setText("Você não tem permissão para adicionar comentários.");
            lblComentarioMessage.setTextFill(Color.RED);
        } else {
            lblComentarioMessage.setText("");
        }
    }

    // Carrega e exibe os comentários na VBox
    private void carregarComentarios() {
        if (planoAtual == null) return;

        List<Comentario> todosComentariosDoPlano = comentarioService.buscarComentariosPorPlanoDeEnsino(planoAtual.getId());

        List<Comentario> comentariosPrincipais = todosComentariosDoPlano.stream()
                .filter(c -> c.getIdComentarioPai() == null || c.getIdComentarioPai().isEmpty())
                .sorted(Comparator.comparing(Comentario::getDataHora)) // Ordena por data
                .collect(Collectors.toList());

        vboxComentarios.getChildren().clear();
        if (comentariosPrincipais.isEmpty()) {
            lblNoComments.setVisible(true);
            lblNoComments.setManaged(true);
        } else {
            lblNoComments.setVisible(false);
            lblNoComments.setManaged(false);
            for (Comentario comentario : comentariosPrincipais) {
                adicionarComentarioNaUI(comentario, 0);

                // Adiciona respostas
                List<Comentario> respostas = todosComentariosDoPlano.stream()
                        .filter(c -> comentario.getId().equals(c.getIdComentarioPai()))
                        .sorted(Comparator.comparing(Comentario::getDataHora))
                        .collect(Collectors.toList());
                for (Comentario resposta : respostas) {
                    adicionarComentarioNaUI(resposta, 1); // Nível 1 para respostas
                }
            }
        }
    }

    // Adiciona um comentário (ou resposta) à UI
    private void adicionarComentarioNaUI(Comentario comentario, int nivelIndentacao) {
        VBox comentarioBox = new VBox(5);
        comentarioBox.setPadding(new Insets(5));
        comentarioBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-radius: 5;");
        comentarioBox.setPrefWidth(Double.MAX_VALUE); // Para que o VBox se expanda horizontalmente

        if (nivelIndentacao > 0) {
            comentarioBox.setPadding(new Insets(5, 5, 5, 20 * nivelIndentacao)); // Adiciona padding à esquerda
            comentarioBox.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-background-color: #f0f0f0; -fx-background-radius: 5; -fx-border-radius: 5;");
        }

        HBox headerBox = new HBox(10);
        Label autorLabel = new Label(comentario.getNomeAutor());
        autorLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        autorLabel.setTextFill(Color.web("#336699"));

        Label dataHoraLabel = new Label(comentario.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dataHoraLabel.setFont(Font.font("System", 10));
        dataHoraLabel.setTextFill(Color.GRAY);

        headerBox.getChildren().addAll(autorLabel, dataHoraLabel);

        TextFlow contentTextFlow = new TextFlow(new Text(comentario.getConteudo()));
        contentTextFlow.setPrefWidth(Double.MAX_VALUE); // Permite que o TextFlow se expanda

        HBox actionBox = new HBox(5);
        actionBox.setAlignment(Pos.CENTER_RIGHT); // Alinha botões à direita

        Usuario currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            boolean canComment = "Estudante".equalsIgnoreCase(currentUser.getPapel()) || "Professor".equalsIgnoreCase(currentUser.getPapel());
            if (canComment) {
                Button btnResponder = new Button("Responder");
                btnResponder.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 2 5; -fx-font-size: 9px; -fx-background-radius: 3;");
                btnResponder.setOnAction(e -> {
                    txtNovoComentario.setText("@" + comentario.getNomeAutor() + " ");
                    txtNovoComentario.requestFocus();

                });
                actionBox.getChildren().add(btnResponder);
            }

            boolean canDelete = false;
            if (comentario.getIdAutor().equals(currentUser.getId())) { // É o próprio autor
                canDelete = true;
            } else if ("Coordenador".equalsIgnoreCase(currentUser.getPapel()) || "Diretor".equalsIgnoreCase(currentUser.getPapel())) { // Coordenador/Diretor
                canDelete = true;
            } else if ("Professor".equalsIgnoreCase(currentUser.getPapel())) { // Professor
                Optional<PlanoDeEnsino> planoOpt = planoDeEnsinoService.buscarPlanoPorId(comentario.getIdPlanoDeEnsino());
                if (planoOpt.isPresent() && planoOpt.get().getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                    canDelete = true;
                }
            }

            if (canDelete) {
                Button btnExcluir = new Button("Excluir");
                btnExcluir.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 2 5; -fx-font-size: 9px; -fx-background-radius: 3;");
                btnExcluir.setOnAction(e -> excluirComentario(comentario.getId()));
                actionBox.getChildren().add(btnExcluir);
            }
        }

        comentarioBox.getChildren().addAll(headerBox, contentTextFlow, actionBox);
        vboxComentarios.getChildren().add(comentarioBox);
    }

    @FXML
    private void handleVerPdf(ActionEvent event) {
        if (planoAtual == null || planoAtual.getCaminhoPdf() == null || planoAtual.getCaminhoPdf().isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Não Encontrado", "Nenhum arquivo PDF associado a este plano.");
            return;
        }
        File pdfFile = new File(planoDeEnsinoService.getPdfUploadDir(), planoAtual.getCaminhoPdf());
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
            mostrarAlerta(Alert.AlertType.WARNING, "Arquivo PDF Ausente", "O arquivo PDF '" + planoAtual.getCaminhoPdf() + "' não foi encontrado na pasta de uploads.");
        }
    }

    @FXML
    private void handleEnviarComentario(ActionEvent event) {
        String conteudo = txtNovoComentario.getText();
        if (planoAtual == null) {
            lblComentarioMessage.setText("Erro: Nenhum plano de ensino selecionado.");
            lblComentarioMessage.setTextFill(Color.RED);
            return;
        }

        String idComentarioPai = null;
        if (conteudo.startsWith("@")) {
            int spaceIndex = conteudo.indexOf(" ");
            if (spaceIndex > 0) {
                String mentionedUsername = conteudo.substring(1, spaceIndex);
                Optional<Comentario> parentCommentOpt = comentarioService.buscarComentariosPorPlanoDeEnsino(planoAtual.getId()).stream()
                        .filter(c -> c.getNomeAutor().equalsIgnoreCase(mentionedUsername) && (c.getIdComentarioPai() == null || c.getIdComentarioPai().isEmpty()))
                        .findFirst();
                if (parentCommentOpt.isPresent()) {
                    idComentarioPai = parentCommentOpt.get().getId();
                }
            }
        }

        try {
            comentarioService.criarComentario(planoAtual.getId(), conteudo, idComentarioPai);
            lblComentarioMessage.setText("Comentário enviado com sucesso!");
            lblComentarioMessage.setTextFill(Color.GREEN);
            txtNovoComentario.clear();
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            lblComentarioMessage.setText(e.getMessage());
            lblComentarioMessage.setTextFill(Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
            lblComentarioMessage.setText("Erro ao enviar comentário.");
            lblComentarioMessage.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleLimparComentario(ActionEvent event) {
        txtNovoComentario.clear();
        lblComentarioMessage.setText("");
    }

    private void excluirComentario(String idComentario) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir este comentário e suas respostas?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.YES) {
            try {
                comentarioService.excluirComentario(idComentario);
                lblComentarioMessage.setText("Comentário excluído com sucesso!");
                lblComentarioMessage.setTextFill(Color.GREEN);
            } catch (SecurityException | IllegalArgumentException | IllegalStateException e) {
                lblComentarioMessage.setText("Erro ao excluir: " + e.getMessage());
                lblComentarioMessage.setTextFill(Color.RED);
            } catch (Exception e) {
                e.printStackTrace();
                lblComentarioMessage.setText("Erro inesperado ao excluir comentário.");
                lblComentarioMessage.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (detailStage != null) {
            detailStage.close();
        }
    }

    // Método do padrão Observer: Atualiza a lista de comentários quando o ComentarioService notifica
    @Override
    @SuppressWarnings("unchecked")
    public void update(Object data) {
        Platform.runLater(() -> {
            if (data instanceof List && planoAtual != null) {
                List<Comentario> comentariosAtualizados = ((List<Comentario>) data).stream()
                        .filter(c -> c.getIdPlanoDeEnsino().equals(planoAtual.getId()))
                        .collect(Collectors.toList());
                carregarComentarios();
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
