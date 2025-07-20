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
    private ComentarioService comentarioService; // NOVO: Atributo para o ComentarioService

    private final String PDF_UPLOAD_DIR = "planos_pdf";

    // Construtor privado para o Singleton
    private PlanoDeEnsinoService() {
        this.repository = PlanoDeEnsinoRepository.getInstance();
        this.userService = UserService.getInstance();

        criarPastaPdfs();
    }

    // Método estático para obter a instância Singleton
    public static synchronized PlanoDeEnsinoService getInstance() {
        if (instance == null) {
            instance = new PlanoDeEnsinoService();
        }
        return instance;
    }

    // Getter público para a pasta de upload de PDFs
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

        // Validação de negócio
        if (plano.getCodigoDisciplina() == null || plano.getCodigoDisciplina().isEmpty()) {
            throw new IllegalArgumentException("Código da disciplina é obrigatório para o plano.");
        }
        if (plano.getNomeDisciplina() == null || plano.getNomeDisciplina().isEmpty()) {
            throw new IllegalArgumentException("Nome da disciplina é obrigatório.");
        }

        // Lógica para lidar com o PDF
        if (pdfFile != null) { // Se um novo PDF foi selecionado
            if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("O arquivo selecionado não é um PDF válido.");
            }
            try {
                // Se já existe um PDF associado e estamos atualizando, podemos excluir o antigo
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
            // Para novos planos, o PDF é obrigatório
            throw new IllegalArgumentException("Um arquivo PDF é obrigatório para um novo plano de ensino.");
        } else {
            // Se for uma atualização e nenhum novo PDF foi selecionado, mantém o caminho do PDF existente
            Optional<PlanoDeEnsino> existingPlanoOpt = repository.buscarPorId(plano.getId());
            existingPlanoOpt.ifPresent(existingPlano -> plano.setCaminhoPdf(existingPlano.getCaminhoPdf()));
        }


        // Controle de acesso para criar/atualizar
        if (plano.getId() == null || plano.getId().isEmpty()) { // Novo plano
            if (!"Professor".equalsIgnoreCase(currentUser.getPapel()) && !"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
                throw new SecurityException("Apenas Professores, Coordenadores e Diretores podem criar planos de ensino.");
            }
            plano.setProfessorResponsavel(currentUser.getNomeUsuario()); // Associa o plano ao professor logado
        } else { // Atualizando plano existente
            Optional<PlanoDeEnsino> existingPlanoOpt = repository.buscarPorId(plano.getId());
            if (existingPlanoOpt.isPresent()) {
                PlanoDeEnsino existingPlano = existingPlanoOpt.get();
                // Professor só pode atualizar seus próprios planos
                if ("Professor".equalsIgnoreCase(currentUser.getPapel()) && !existingPlano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                    throw new SecurityException("Você só pode editar planos de ensino que você é responsável.");
                }
                // Coordenador e Diretor podem atualizar qualquer plano
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

    // Modificado: excluirPlano agora verifica permissão e exclui PDF físico e comentários
    public void excluirPlano(String id) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }

        Optional<PlanoDeEnsino> planoOpt = repository.buscarPorId(id);
        if (planoOpt.isPresent()) {
            PlanoDeEnsino plano = planoOpt.get();
            // Controle de acesso para exclusão
            if ("Professor".equalsIgnoreCase(currentUser.getPapel()) && !plano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                throw new SecurityException("Você só pode excluir planos de ensino que você é responsável.");
            }
            if (!"Professor".equalsIgnoreCase(currentUser.getPapel()) && !"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
                throw new SecurityException("Você não tem permissão para excluir planos de ensino.");
            }

            // NOVO: Exclui todos os comentários associados a este plano ANTES de excluir o plano em si
            getComentarioService().excluirComentariosDoPlano(id);

            repository.excluir(id); // Exclui o plano do repositório
            excluirPdfFisico(plano.getCaminhoPdf()); // Exclui o arquivo PDF físico
            notifyObservers(repository.buscarTodos()); // Notifica os observadores
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
            // Professor só pode submeter seus próprios planos
            if ("Professor".equalsIgnoreCase(currentUser.getPapel()) && !plano.getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                throw new SecurityException("Você só pode submeter planos de ensino que você é responsável.");
            }
            if (!"Professor".equalsIgnoreCase(currentUser.getPapel())) { // Apenas professor pode submeter
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

        // Apenas Coordenador e Diretor podem aprovar
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