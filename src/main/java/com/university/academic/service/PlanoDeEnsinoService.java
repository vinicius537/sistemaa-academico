package com.university.academic.service;

import com.university.academic.model.PlanoDeEnsino;
import com.university.academic.model.PlanoDeEnsinoRepository;
import com.university.academic.model.Usuario;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Optional;

public class PlanoDeEnsinoService extends Observable {
    private static PlanoDeEnsinoService instance;
    private PlanoDeEnsinoRepository repository;
    private UserService userService;
    private ComentarioService comentarioService;

    private final String PDF_UPLOAD_DIR = "planos_pdf";

    private PlanoDeEnsinoService() {
        this.repository = PlanoDeEnsinoRepository.getInstance();
        this.userService = UserService.getInstance();

        criarPastaPdfs();
    }

    public static synchronized PlanoDeEnsinoService getInstance() {
        if (instance == null) {
            instance = new PlanoDeEnsinoService();
        }
        return instance;
    }

    public String getPdfUploadDir() {
        return PDF_UPLOAD_DIR;
    }

    private void criarPastaPdfs() {
        File dir = new File(PDF_UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("DEBUG: Pasta de upload de PDFs criada: " + PDF_UPLOAD_DIR);
        }
    }

    public String copiarPdfParaPasta(File sourceFile) throws IOException {
        if (sourceFile == null) {
            return null;
        }
        String fileName = sourceFile.getName();
        Path destinationPath = Paths.get(PDF_UPLOAD_DIR, fileName);
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("DEBUG: PDF copiado para: " + destinationPath);
        return fileName;
    }

    private void excluirPdfFisico(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            File fileToDelete = new File(PDF_UPLOAD_DIR, fileName);
            if (fileToDelete.exists()) {
                fileToDelete.delete();
                System.out.println("DEBUG: PDF físico excluído: " + fileName);
            }
        }
    }

    public void criarOuAtualizarPlano(PlanoDeEnsino plano, File pdfFile) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }

        if (plano.getCodigoDisciplina() == null || plano.getCodigoDisciplina().isEmpty()) {
            throw new IllegalArgumentException("Código da disciplina é obrigatório para o plano.");
        }
        if (plano.getNomeDisciplina() == null || plano.getNomeDisciplina().isEmpty()) {
            throw new IllegalArgumentException("Nome da disciplina é obrigatório.");
        }

        if (pdfFile != null) {
            if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("O arquivo selecionado não é um PDF válido.");
            }
            try {
                if (plano.getId() != null && !plano.getId().isEmpty()) {
                    Optional<PlanoDeEnsino> existingPlanoOpt = repository.buscarPorId(plano.getId());
                    existingPlanoOpt.ifPresent(existingPlano -> {
                        if (existingPlano.getCaminhoPdf() != null && !existingPlano.getCaminhoPdf().isEmpty()) {
                            excluirPdfFisico(existingPlano.getCaminhoPdf());
                        }
                    });
                }
                plano.setCaminhoPdf(copiarPdfParaPasta(pdfFile));
            } catch (IOException e) {
                throw new RuntimeException("Erro ao copiar arquivo PDF: " + e.getMessage(), e);
            }
        } else if (plano.getId() == null || plano.getId().isEmpty()) {
            throw new IllegalArgumentException("Um arquivo PDF é obrigatório para um novo plano de ensino.");
        } else {
            Optional<PlanoDeEnsino> existingPlanoOpt = repository.buscarPorId(plano.getId());
            existingPlanoOpt.ifPresent(existingPlano -> plano.setCaminhoPdf(existingPlano.getCaminhoPdf()));
        }

        if (plano.getId() == null || plano.getId().isEmpty()) {
            if (!"Professor".equalsIgnoreCase(currentUser.getPapel()) && !"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
                throw new SecurityException("Apenas Professores, Coordenadores e Diretores podem criar planos de ensino.");
            }
            plano.setProfessorResponsavel(currentUser.getNomeUsuario());
        } else {
            Optional<PlanoDeEnsino> existingPlanoOpt = repository.buscarPorId(plano.getId());
            if (existingPlanoOpt.isPresent()) {
                PlanoDeEnsino existingPlano = existingPlanoOpt.get();
                if ("Professor".equalsIgnoreCase(currentUser.getPapel()) && !existingPlano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                    throw new SecurityException("Você só pode editar planos de ensino que você é responsável.");
                }
                if (!"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel()) && !"Professor".equalsIgnoreCase(currentUser.getPapel())) {
                    throw new SecurityException("Você não tem permissão para editar planos de ensino.");
                }
            } else {
                throw new IllegalArgumentException("Plano de ensino não encontrado para atualização.");
            }
        }

        repository.salvar(plano);
        notifyObservers(repository.buscarTodos());
    }

    public Optional<PlanoDeEnsino> buscarPlanoPorId(String id) {
        return repository.buscarPorId(id);
    }

    public List<PlanoDeEnsino> buscarTodosPlanos() {
        return repository.buscarTodos();
    }

    public void excluirPlano(String id) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }

        Optional<PlanoDeEnsino> planoOpt = repository.buscarPorId(id);
        if (planoOpt.isPresent()) {
            PlanoDeEnsino plano = planoOpt.get();
            if ("Professor".equalsIgnoreCase(currentUser.getPapel()) && !plano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                throw new SecurityException("Você só pode excluir planos de ensino que você é responsável.");
            }
            if (!"Professor".equalsIgnoreCase(currentUser.getPapel()) && !"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
                throw new SecurityException("Você não tem permissão para excluir planos de ensino.");
            }

            getComentarioService().excluirComentariosDoPlano(id);

            repository.excluir(id);
            excluirPdfFisico(plano.getCaminhoPdf());
            notifyObservers(repository.buscarTodos());
        } else {
            throw new IllegalArgumentException("Plano de ensino não encontrado para exclusão.");
        }
    }

    public void submeterParaAprovacao(String id) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }

        Optional<PlanoDeEnsino> planoOpt = repository.buscarPorId(id);
        planoOpt.ifPresent(plano -> {
            if ("Professor".equalsIgnoreCase(currentUser.getPapel()) && !plano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                throw new SecurityException("Você só pode submeter planos de ensino que você é responsável.");
            }
            if (!"Professor".equalsIgnoreCase(currentUser.getPapel())) {
                throw new SecurityException("Apenas Professores podem submeter planos de ensino.");
            }

            if ("Rascunho".equals(plano.getStatus())) {
                plano.setStatus("Submetido");
                repository.salvar(plano);
                notifyObservers(repository.buscarTodos());
            } else {
                throw new IllegalArgumentException("Plano não pode ser submetido neste status: " + plano.getStatus());
            }
        });
    }

    public void aprovarPlano(String id) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }

        if (!"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
            throw new SecurityException("Apenas Coordenadores e Diretores podem aprovar planos de ensino.");
        }

        Optional<PlanoDeEnsino> planoOpt = repository.buscarPorId(id);
        planoOpt.ifPresent(plano -> {
            if ("Submetido".equals(plano.getStatus())) {
                plano.setStatus("Aprovado");
                repository.salvar(plano);
                notifyObservers(repository.buscarTodos());
            } else {
                throw new IllegalArgumentException("Plano não pode ser aprovado neste status: " + plano.getStatus());
            }
        });
    }
    private ComentarioService getComentarioService() {
        if (comentarioService == null) {
            comentarioService = ComentarioService.getInstance();
        }
        return comentarioService;
    }

}