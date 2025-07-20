package com.university.academic.service;

import com.university.academic.model.AssociacaoDisciplinaUsuario;
import com.university.academic.model.AssociacaoDisciplinaUsuarioRepository;
import com.university.academic.model.Disciplina;
import com.university.academic.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssociacaoService extends Observable {
    private static AssociacaoService instance;
    private AssociacaoDisciplinaUsuarioRepository repository;
    private DisciplinaService disciplinaService;
    private UserService userService;

    private AssociacaoService() {
        this.repository = AssociacaoDisciplinaUsuarioRepository.getInstance();
        this.disciplinaService = DisciplinaService.getInstance();
        this.userService = UserService.getInstance();
    }

    public static synchronized AssociacaoService getInstance() {
        if (instance == null) {
            instance = new AssociacaoService();
        }
        return instance;
    }

    public void criarOuAtualizarAssociacao(String idAssociacao, String idDisciplina, String idUsuario, String papelUsuario) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
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

        if (!usuario.getPapel().equalsIgnoreCase(papelUsuario)) {
            throw new IllegalArgumentException("O papel selecionado (" + papelUsuario + ") não corresponde ao papel real do usuário (" + usuario.getPapel() + ").");
        }

        if (idAssociacao == null || idAssociacao.isEmpty()) {
            if (repository.associacaoExiste(idDisciplina, idUsuario, papelUsuario)) {
                throw new IllegalArgumentException("Esta associação já existe.");
            }
        }

        AssociacaoDisciplinaUsuario associacao;
        if (idAssociacao == null || idAssociacao.isEmpty()) {
            associacao = new AssociacaoDisciplinaUsuario(
                    disciplina.getId(), disciplina.getNome(),
                    usuario.getId(), usuario.getNomeUsuario(),
                    papelUsuario
            );
        } else {
            Optional<AssociacaoDisciplinaUsuario> existingAssocOpt = repository.buscarPorId(idAssociacao);
            if (!existingAssocOpt.isPresent()) {
                throw new IllegalArgumentException("Associação não encontrada para atualização.");
            }
            associacao = existingAssocOpt.get();
            associacao.setIdDisciplina(disciplina.getId());
            associacao.setNomeDisciplina(disciplina.getNome());
            associacao.setIdUsuario(usuario.getId());
            associacao.setNomeUsuario(usuario.getNomeUsuario());
            associacao.setPapelUsuario(papelUsuario);
        }

        repository.salvar(associacao);
        notifyObservers(repository.buscarTodos());
    }

    public void excluirAssociacao(String idAssociacao) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
        if (!"Coordenador".equalsIgnoreCase(currentUser.getPapel()) && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
            throw new SecurityException("Apenas Coordenadores e Diretores podem excluir associações de disciplinas.");
        }

        Optional<AssociacaoDisciplinaUsuario> associacaoOpt = repository.buscarPorId(idAssociacao);
        if (!associacaoOpt.isPresent()) {
            throw new IllegalArgumentException("Associação não encontrada.");
        }

        repository.excluir(idAssociacao);
        notifyObservers(repository.buscarTodos());
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

    public void associarPlanoDeEnsino(String idAssociacao, String idPlanoDeEnsino) {
        Usuario currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
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

        Optional<Usuario> professorAssociadoOpt = userService.buscarTodosUsuarios().stream()
                .filter(u -> u.getId().equals(associacao.getIdUsuario()))
                .findFirst();
        if (professorAssociadoOpt.isPresent() && !professorAssociadoOpt.get().getId().equals(currentUser.getId())) {
            throw new SecurityException("Você só pode associar planos a disciplinas que você leciona.");
        }

        associacao.setIdPlanoDeEnsino(idPlanoDeEnsino);
        repository.salvar(associacao);
        notifyObservers(repository.buscarTodos());
    }
}