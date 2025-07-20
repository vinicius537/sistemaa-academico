package com.university.academic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DisciplinaRepository {
    private static DisciplinaRepository instance;
    private List<Disciplina> disciplinas;
    private final String DATA_FILE = "disciplinas.json";
    private ObjectMapper objectMapper;

    private DisciplinaRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.disciplinas = carregarDisciplinasDoArquivo();

        if (this.disciplinas.isEmpty()) {
            System.out.println("DEBUG: Arquivo de disciplinas vazio ou não encontrado. Inicializando com disciplinas padrão.");
            disciplinas.add(new Disciplina(UUID.randomUUID().toString(), "COMP101", "Introdução à Programação", 60, "Disciplina introdutória de programação."));
            disciplinas.add(new Disciplina(UUID.randomUUID().toString(), "COMP202", "Estruturas de Dados", 60, "Estudo de estruturas de dados e algoritmos."));
            disciplinas.add(new Disciplina(UUID.randomUUID().toString(), "COMP303", "Bancos de Dados", 60, "Conceitos e práticas de bancos de dados."));
            salvarDisciplinasNoArquivo();
        } else {
            System.out.println("DEBUG: Disciplinas carregadas do arquivo.");
        }
    }

    public static synchronized DisciplinaRepository getInstance() {
        if (instance == null) {
            instance = new DisciplinaRepository();
        }
        return instance;
    }

    private List<Disciplina> carregarDisciplinasDoArquivo() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Disciplina.class));
            } catch (IOException e) {
                System.err.println("Erro ao carregar disciplinas do arquivo JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void salvarDisciplinasNoArquivo() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), disciplinas);
            System.out.println("DEBUG: Disciplinas salvas no arquivo " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Erro ao salvar disciplinas no arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void salvar(Disciplina disciplina) {
        if (disciplina.getId() == null || disciplina.getId().isEmpty()) {
            disciplina.setId(UUID.randomUUID().toString());
            disciplinas.add(disciplina);
        } else {
            boolean found = false;
            for (int i = 0; i < disciplinas.size(); i++) {
                if (disciplinas.get(i).getId().equals(disciplina.getId())) {
                    disciplinas.set(i, disciplina);
                    found = true;
                    break;
                }
            }
            if (!found) {
                disciplinas.add(disciplina);
            }
        }
        salvarDisciplinasNoArquivo();
        System.out.println("DEBUG: Disciplina salva/atualizada: " + disciplina.getNome());
    }

    public Optional<Disciplina> buscarPorId(String id) {
        return disciplinas.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    public Optional<Disciplina> buscarPorCodigo(String codigo) {
        return disciplinas.stream().filter(d -> d.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }

    public List<Disciplina> buscarTodos() {
        return new ArrayList<>(disciplinas);
    }

    public void excluir(String id) {
        disciplinas.removeIf(d -> d.getId().equals(id));
        salvarDisciplinasNoArquivo();
        System.out.println("DEBUG: Disciplina excluída: " + id);
    }
}