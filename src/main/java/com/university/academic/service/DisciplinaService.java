package com.university.academic.service;

import com.university.academic.model.Disciplina;
import com.university.academic.model.DisciplinaRepository;

import java.util.List;
import java.util.Optional;

public class DisciplinaService {
    private static DisciplinaService instance; // Instância Singleton
    private DisciplinaRepository repository;

    // Construtor privado para o Singleton
    private DisciplinaService() {
        this.repository = DisciplinaRepository.getInstance(); // Obtém a instância Singleton do repositório
    }

    // Método estático para obter a instância Singleton
    public static synchronized DisciplinaService getInstance() {
        if (instance == null) {
            instance = new DisciplinaService();
        }
        return instance;
    }

    public void criarOuAtualizarDisciplina(Disciplina disciplina) {
        // Exemplo de validação de negócio para Disciplina
        if (disciplina.getCodigo() == null || disciplina.getCodigo().isEmpty()) {
            throw new IllegalArgumentException("Código da disciplina é obrigatório.");
        }
        if (disciplina.getNome() == null || disciplina.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome da disciplina é obrigatório.");
        }
        if (disciplina.getCargaHoraria() <= 0) {
            throw new IllegalArgumentException("Carga horária deve ser maior que zero.");
        }

        // Verifica se o código da disciplina já existe para outra disciplina (em caso de criação/edição)
        Optional<Disciplina> existingDisciplina = repository.buscarPorCodigo(disciplina.getCodigo());
        if (existingDisciplina.isPresent() && (disciplina.getId() == null || !existingDisciplina.get().getId().equals(disciplina.getId()))) {
            throw new IllegalArgumentException("Já existe uma disciplina com este código.");
        }

        repository.salvar(disciplina);
        // Não notifica observadores aqui, pois as disciplinas são mais estáticas e as associações as observarão
    }

    public Optional<Disciplina> buscarDisciplinaPorId(String id) {
        return repository.buscarPorId(id);
    }

    public Optional<Disciplina> buscarDisciplinaPorCodigo(String codigo) {
        return repository.buscarPorCodigo(codigo);
    }

    public List<Disciplina> buscarTodasDisciplinas() {
        return repository.buscarTodos();
    }

    public void excluirDisciplina(String id) {
        // Em um sistema real, aqui haveria validação para não excluir disciplina com associações ativas
        repository.excluir(id);
    }
}