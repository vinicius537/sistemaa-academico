package com.university.academic.service;

import com.university.academic.model.AssociacaoDisciplinaUsuario;
import com.university.academic.model.AssociacaoDisciplinaUsuarioRepository;
import com.university.academic.model.Disciplina;
import com.university.academic.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssociacaoService extends Observable { // Estende Observable para notificar mudanças
    private static AssociacaoService instance; // Instância Singleton
    private AssociacaoDisciplinaUsuarioRepository repository;
    private DisciplinaService disciplinaService; // Para buscar dados de disciplina
    private UserService userService; // Para buscar dados de usuário

    // Construtor privado para o Singleton
    private AssociacaoService() {
        this.repository = AssociacaoDisciplinaUsuarioRepository.getInstance();
        this.disciplinaService = DisciplinaService.getInstance();
        this.userService = UserService.getInstance();
    }

    // Método estático para obter a instância Singleton
    public static synchronized AssociacaoService getInstance() {
        if (instance == null) {
            instance = new AssociacaoService();
        }
        return instance;
    }

    /**
     * Cria ou atualiza uma associação entre disciplina e usuário.
     * @param idDisciplina ID da disciplina.
     * @param idUsuario ID do usuário (Professor ou Estudante).
     * @param papelUsuario Papel do usuário na associação (Professor ou Estudante).
     * @throws IllegalArgumentException Se a disciplina ou usuário não existirem, ou a associação já existir.
     * @throws SecurityException Se o usuário logado não tiver permissão (Coordenador ou Diretor).
     */
    public void criarOuAtualizarAssociacao(String idAssociacao, String idDisciplina, String idUsuario, String papelUsuario) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
        // Apenas Coordenador e Diretor podem gerenciar associações
        if (!"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
            throw new SecurityException("Apenas Coordenadores e Diretores podem gerenciar associações de disciplinas.");
        }

        Optional<Disciplina> disciplinaOpt = disciplinaService.buscarDisciplinaPorId(idDisciplina);
        if (!disciplinaOpt.isPresent()) {
            throw new IllegalArgumentException("Disciplina não encontrada.");
        }
        Disciplina disciplina = disciplinaOpt.get();

        Optional<Usuario> usuarioOpt = userService.buscarTodosUsuarios().stream()
                .filter(u -> u.getId().equals(idUsuario))
                .findFirst();
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        Usuario usuario = usuarioOpt.get();

        // Valida se o papel do usuário é compatível com a associação
        if (!usuario.getPapel().equalsIgnoreCase(papelUsuario)) {
            throw new IllegalArgumentException("O papel selecionado (" + papelUsuario + ") não corresponde ao papel real do usuário (" + usuario.getPapel() + ").");
        }

        // Verifica se a associação já existe (para evitar duplicatas)
        if (idAssociacao == null || idAssociacao.isEmpty()) { // Se for uma nova associação
            if (repository.associacaoExiste(idDisciplina, idUsuario, papelUsuario)) {
                throw new IllegalArgumentException("Esta associação já existe.");
            }
        }

        AssociacaoDisciplinaUsuario associacao;
        if (idAssociacao == null || idAssociacao.isEmpty()) {
            // Nova associação
            associacao = new AssociacaoDisciplinaUsuario(
                    disciplina.getId(), disciplina.getNome(),
                    usuario.getId(), usuario.getNomeUsuario(),
                    papelUsuario
            );
        } else {
            // Atualizando associação existente (apenas o ID do plano pode ser atualizado, ou o ID da associação)
            Optional<AssociacaoDisciplinaUsuario> existingAssocOpt = repository.buscarPorId(idAssociacao);
            if (!existingAssocOpt.isPresent()) {
                throw new IllegalArgumentException("Associação não encontrada para atualização.");
            }
            associacao = existingAssocOpt.get();
            // Atualiza os campos que podem ter sido alterados via UI (embora nesta tela não haja edição direta exceto exclusão)
            associacao.setIdDisciplina(disciplina.getId());
            associacao.setNomeDisciplina(disciplina.getNome());
            associacao.setIdUsuario(usuario.getId());
            associacao.setNomeUsuario(usuario.getNomeUsuario());
            associacao.setPapelUsuario(papelUsuario);
        }

        repository.salvar(associacao);
        notifyObservers(repository.buscarTodos()); // Notifica os observadores
    }

    /**
     * Exclui uma associação existente.
     * @param idAssociacao ID da associação a ser excluída.
     * @throws SecurityException Se o usuário logado não tiver permissão.
     */
    public void excluirAssociacao(String idAssociacao) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
        // Apenas Coordenador e Diretor podem excluir associações
        if (!"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
            throw new SecurityException("Apenas Coordenadores e Diretores podem excluir associações de disciplinas.");
        }

        Optional<AssociacaoDisciplinaUsuario> associacaoOpt = repository.buscarPorId(idAssociacao);
        if (!associacaoOpt.isPresent()) {
            throw new IllegalArgumentException("Associação não encontrada.");
        }
        // Em um sistema real, aqui haveria validação se o plano de ensino associado existe
        // e se ele deve ser desvinculado ou excluído junto.

        repository.excluir(idAssociacao);
        notifyObservers(repository.buscarTodos()); // Notifica os observadores
    }

    public List<AssociacaoDisciplinaUsuario> buscarTodasAssociacoes() {
        return repository.buscarTodos();
    }

    public List<AssociacaoDisciplinaUsuario> buscarAssociacoesPorIdUsuario(String idUsuario) {
        return repository.buscarPorIdUsuario(idUsuario);
    }

    public List<AssociacaoDisciplinaUsuario> buscarAssociacoesPorIdDisciplina(String idDisciplina) {
        return repository.buscarPorIdDisciplina(idDisciplina);
    }

    /**
     * Associa um Plano de Ensino a uma Associação Disciplina-Professor.
     * @param idAssociacao ID da associação disciplina-professor.
     * @param idPlanoDeEnsino ID do plano de ensino.
     * @throws IllegalArgumentException Se a associação não for encontrada ou não for de um professor.
     * @throws SecurityException Se o usuário logado não tiver permissão (Professor, Coordenador, Diretor).
     */
    public void associarPlanoDeEnsino(String idAssociacao, String idPlanoDeEnsino) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
        // Professor, Coordenador e Diretor podem associar planos
        if (!"Professor".equalsIgnoreCase(currentUser.getPapel()) && !"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
            throw new SecurityException("Você não tem permissão para associar planos de ensino.");
        }

        Optional<AssociacaoDisciplinaUsuario> associacaoOpt = repository.buscarPorId(idAssociacao);
        if (!associacaoOpt.isPresent()) {
            throw new IllegalArgumentException("Associação não encontrada.");
        }
        AssociacaoDisciplinaUsuario associacao = associacaoOpt.get();

        if (!"Professor".equalsIgnoreCase(associacao.getPapelUsuario())) {
            throw new IllegalArgumentException("A associação deve ser de um Professor para ter um plano de ensino.");
        }

        // Professor só pode associar seus próprios planos
        Optional<Usuario> professorAssociadoOpt = userService.buscarTodosUsuarios().stream()
                .filter(u -> u.getId().equals(associacao.getIdUsuario()))
                .findFirst();
        if (professorAssociadoOpt.isPresent() && !professorAssociadoOpt.get().getId().equals(currentUser.getId())) {
            throw new SecurityException("Você só pode associar planos a disciplinas que você leciona.");
        }

        associacao.setIdPlanoDeEnsino(idPlanoDeEnsino);
        repository.salvar(associacao);
        notifyObservers(repository.buscarTodos()); // Notifica os observadores
    }
}