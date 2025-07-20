package com.university.academic.service;

import com.university.academic.model.Comentario;
import com.university.academic.model.ComentarioRepository;
import com.university.academic.model.PlanoDeEnsino; // Para verificar a existência do plano
import com.university.academic.model.Usuario; // Para verificar o usuário logado

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComentarioService extends Observable { // Estende Observable para notificar mudanças
    private static ComentarioService instance; // Instância Singleton
    private ComentarioRepository repository;
    private UserService userService; // Para obter o usuário logado
    private PlanoDeEnsinoService planoDeEnsinoService; // Para verificar a existência do plano

    // Construtor privado para o Singleton
    private ComentarioService() {
        this.repository = ComentarioRepository.getInstance();
        this.userService = UserService.getInstance();
        this.planoDeEnsinoService = PlanoDeEnsinoService.getInstance();
    }

    // Método estático para obter a instância Singleton
    public static synchronized ComentarioService getInstance() {
        if (instance == null) {
            instance = new ComentarioService();
        }
        return instance;
    }

    /**
     * Cria um novo comentário ou uma resposta a um comentário existente.
     * @param idPlanoDeEnsino ID do plano de ensino ao qual o comentário pertence.
     * @param conteudo Conteúdo textual do comentário.
     * @param idComentarioPai ID do comentário pai, se for uma resposta (null para comentário principal).
     * @throws IllegalStateException Se não houver usuário logado.
     * @throws IllegalArgumentException Se o plano de ensino não existir ou o conteúdo for vazio.
     * @throws SecurityException Se o usuário logado não tiver permissão para comentar.
     */
    public void criarComentario(String idPlanoDeEnsino, String conteudo, String idComentarioPai) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado para criar comentário.");
        }

        // NOVO: Controle de acesso baseado no papel para criar comentários
        boolean canComment = "Estudante".equalsIgnoreCase(currentUser.getPapel()) || "Professor".equalsIgnoreCase(currentUser.getPapel());
        if (!canComment) {
            throw new SecurityException("Apenas Estudantes e Professores podem adicionar comentários.");
        }

        if (conteudo == null || conteudo.trim().isEmpty()) {
            throw new IllegalArgumentException("O conteúdo do comentário não pode ser vazio.");
        }

        // Verifica se o plano de ensino existe
        Optional<PlanoDeEnsino> planoOpt = planoDeEnsinoService.buscarPlanoPorId(idPlanoDeEnsino);
        if (!planoOpt.isPresent()) {
            throw new IllegalArgumentException("Plano de Ensino não encontrado para este comentário.");
        }

        Comentario novoComentario = new Comentario(
                idPlanoDeEnsino,
                currentUser.getId(),
                currentUser.getNomeUsuario(), // Usar nome de usuário ou nome completo
                conteudo.trim(),
                idComentarioPai
        );

        repository.salvar(novoComentario);
        // Notifica os observadores com a lista de comentários para o plano específico
        notifyObservers(buscarComentariosPorPlanoDeEnsino(idPlanoDeEnsino));
    }

    /**
     * Busca todos os comentários para um plano de ensino específico.
     * @param idPlanoDeEnsino ID do plano de ensino.
     * @return Uma lista de comentários para o plano, incluindo respostas.
     */
    public List<Comentario> buscarComentariosPorPlanoDeEnsino(String idPlanoDeEnsino) {
        return repository.buscarPorIdPlanoDeEnsino(idPlanoDeEnsino);
    }

    /**
     * Exclui um comentário e todas as suas respostas.
     * @param idComentario ID do comentário a ser excluído.
     * @throws IllegalStateException Se não houver usuário logado.
     * @throws SecurityException Se o usuário logado não tiver permissão para excluir o comentário.
     */
    public void excluirComentario(String idComentario) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado para excluir comentário.");
        }

        Optional<Comentario> comentarioOpt = repository.buscarPorId(idComentario);
        if (!comentarioOpt.isPresent()) {
            throw new IllegalArgumentException("Comentário não encontrado.");
        }
        Comentario comentario = comentarioOpt.get();

        // Regra de permissão para exclusão:
        // - O próprio autor pode excluir seu comentário.
        // - Professores da disciplina podem excluir comentários no plano deles.
        // - Coordenadores e Diretores podem excluir qualquer comentário.
        boolean canDelete = false;
        if (comentario.getIdAutor().equals(currentUser.getId())) { // É o próprio autor
            canDelete = true;
        } else if ("Coordenador".equalsIgnoreCase(currentUser.getPapel()) || "Diretor".equalsIgnoreCase(currentUser.getPapel())) { // Coordenador/Diretor
            canDelete = true;
        } else if ("Professor".equalsIgnoreCase(currentUser.getPapel())) { // Professor
            // Verifica se o professor é responsável pelo plano
            Optional<PlanoDeEnsino> planoOpt = planoDeEnsinoService.buscarPlanoPorId(comentario.getIdPlanoDeEnsino());
            if (planoOpt.isPresent() && planoOpt.get().getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                canDelete = true;
            }
        }

        if (!canDelete) {
            throw new SecurityException("Você não tem permissão para excluir este comentário.");
        }

        repository.excluir(idComentario); // Exclui o comentário e suas respostas (lógica no repo)
        notifyObservers(buscarComentariosPorPlanoDeEnsino(comentario.getIdPlanoDeEnsino())); // Notifica
    }

    // Método para ser chamado ao excluir um PlanoDeEnsino, para limpar seus comentários
    public void excluirComentariosDoPlano(String idPlanoDeEnsino) {
        repository.excluirPorIdPlanoDeEnsino(idPlanoDeEnsino);
        // Não notifica aqui, pois a exclusão do plano já dispara a atualização da lista de planos
    }
}
