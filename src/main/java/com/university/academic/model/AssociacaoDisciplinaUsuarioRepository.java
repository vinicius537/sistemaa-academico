package com.university.academic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AssociacaoDisciplinaUsuarioRepository {
    private static AssociacaoDisciplinaUsuarioRepository instance;
    private List<AssociacaoDisciplinaUsuario> associacoes;
    private final String DATA_FILE = "associacoes.json";
    private ObjectMapper objectMapper;

    private AssociacaoDisciplinaUsuarioRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.associacoes = carregarAssociacoesDoArquivo();

        if (this.associacoes.isEmpty()) {
            System.out.println("DEBUG: Arquivo de associações vazio ou não encontrado.");
        } else {
            System.out.println("DEBUG: Associações carregadas do arquivo.");
        }
    }

    public static synchronized AssociacaoDisciplinaUsuarioRepository getInstance() {
        if (instance == null) {
            instance = new AssociacaoDisciplinaUsuarioRepository();
        }
        return instance;
    }

    private List<AssociacaoDisciplinaUsuario> carregarAssociacoesDoArquivo() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, AssociacaoDisciplinaUsuario.class));
            } catch (IOException e) {
                System.err.println("Erro ao carregar associações do arquivo JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void salvarAssociacoesNoArquivo() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), associacoes);
            System.out.println("DEBUG: Associações salvas no arquivo " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Erro ao salvar associações no arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void salvar(AssociacaoDisciplinaUsuario associacao) {
        if (associacao.getId() == null || associacao.getId().isEmpty()) {
            associacao.setId(UUID.randomUUID().toString());
            associacoes.add(associacao);
        } else {
            boolean found = false;
            for (int i = 0; i < associacoes.size(); i++) {
                if (associacoes.get(i).getId().equals(associacao.getId())) {
                    associacoes.set(i, associacao);
                    found = true;
                    break;
                }
            }
            if (!found) {
                associacoes.add(associacao);
            }
        }
        salvarAssociacoesNoArquivo();
        System.out.println("DEBUG: Associação salva/atualizada: " + associacao.toString());
    }

    public Optional<AssociacaoDisciplinaUsuario> buscarPorId(String id) {
        return associacoes.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public List<AssociacaoDisciplinaUsuario> buscarTodos() {
        return new ArrayList<>(associacoes);
    }

    public List<AssociacaoDisciplinaUsuario> buscarPorIdUsuario(String idUsuario) {
        return associacoes.stream()
                .filter(a -> a.getIdUsuario().equals(idUsuario))
                .collect(Collectors.toList());
    }

    public List<AssociacaoDisciplinaUsuario> buscarPorIdDisciplina(String idDisciplina) {
        return associacoes.stream()
                .filter(a -> a.getIdDisciplina().equals(idDisciplina))
                .collect(Collectors.toList());
    }

    public void excluir(String id) {
        associacoes.removeIf(a -> a.getId().equals(id));
        salvarAssociacoesNoArquivo();
        System.out.println("DEBUG: Associação excluída: " + id);
    }

    public boolean associacaoExiste(String idDisciplina, String idUsuario, String papelUsuario) {
        return associacoes.stream()
                .anyMatch(a -> a.getIdDisciplina().equals(idDisciplina) &&
                        a.getIdUsuario().equals(idUsuario) &&
                        a.getPapelUsuario().equalsIgnoreCase(papelUsuario));
    }
}