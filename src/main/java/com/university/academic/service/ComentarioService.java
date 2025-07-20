package com.university.academic.service;

import com.university.academic.model.Comentario;
import com.university.academic.model.ComentarioRepository;
import com.university.academic.model.PlanoDeEnsino;
import com.university.academic.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComentarioService extends Observable {
    private static ComentarioService instance;
    private ComentarioRepository repository;
    private UserService userService;
    private PlanoDeEnsinoService planoDeEnsinoService;

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

    public void criarComentario(String idPlanoDeEnsino, String conteudo, String idComentarioPai) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado para criar comentário.");
        }

        boolean canComment = "Estudante".equalsIgnoreCase(currentUser.getPapel()) || "Professor".equalsIgnoreCase(currentUser.getPapel());
        if (!canComment) {
            throw new SecurityException("Apenas Estudantes e Professores podem adicionar comentários.");
        }

        if (conteudo == null || conteudo.trim().isEmpty()) {
            throw new IllegalArgumentException("O conteúdo do comentário não pode ser vazio.");
        }

        Optional<PlanoDeEnsino> planoOpt = planoDeEnsinoService.buscarPlanoPorId(idPlanoDeEnsino);
        if (!planoOpt.isPresent()) {
            throw new IllegalArgumentException("Plano de Ensino não encontrado para este comentário.");
        }

        Comentario novoComentario = new Comentario(
                idPlanoDeEnsino,
                currentUser.getId(),
                currentUser.getNomeUsuario(),
                conteudo.trim(),
                idComentarioPai
        );

        repository.salvar(novoComentario);
        notifyObservers(buscarComentariosPorPlanoDeEnsino(idPlanoDeEnsino));
    }

    public List<Comentario> buscarComentariosPorPlanoDeEnsino(String idPlanoDeEnsino) {
        return repository.buscarPorIdPlanoDeEnsino(idPlanoDeEnsino);
    }

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

        boolean canDelete = false;
        if (comentario.getIdAutor().equals(currentUser.getId())) {
            canDelete = true;
        } else if ("Coordenador".equalsIgnoreCase(currentUser.getPapel()) || "Diretor".equalsIgnoreCase(currentUser.getPapel())) { // Coordenador/Diretor
            canDelete = true;
        } else if ("Professor".equalsIgnoreCase(currentUser.getPapel())) {
            Optional<PlanoDeEnsino> planoOpt = planoDeEnsinoService.buscarPlanoPorId(comentario.getIdPlanoDeEnsino());
            if (planoOpt.isPresent() && planoOpt.get().getProfessorResponsavel().equals(currentUser.getNomeUsuario())) {
                canDelete = true;
            }
        }

        if (!canDelete) {
            throw new SecurityException("Você não tem permissão para excluir este comentário.");
        }

        repository.excluir(idComentario);
        notifyObservers(buscarComentariosPorPlanoDeEnsino(comentario.getIdPlanoDeEnsino()));
    }

    public void excluirComentariosDoPlano(String idPlanoDeEnsino) {
        repository.excluirPorIdPlanoDeEnsino(idPlanoDeEnsino);
    }
}
