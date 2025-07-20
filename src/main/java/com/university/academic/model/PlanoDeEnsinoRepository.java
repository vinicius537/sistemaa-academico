package com.university.academic.model;

import com.fasterxml.jackson.databind.ObjectMapper; // Import para Jackson
import com.fasterxml.jackson.databind.SerializationFeature; // Import para Jackson

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlanoDeEnsinoRepository {
    private static PlanoDeEnsinoRepository instance;
    private List<PlanoDeEnsino> planos;
    private final String DATA_FILE = "planos_ensino.json";
    private ObjectMapper objectMapper;

    private PlanoDeEnsinoRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.planos = carregarPlanosDoArquivo();

        if (this.planos.isEmpty()) {
            System.out.println("DEBUG: Arquivo de planos vazio ou não encontrado. Inicializando com planos padrão.");
            planos.add(new PlanoDeEnsino(UUID.randomUUID().toString(), "COMP123", "Programação Orientada a Objetos",
                    "2025.1", "Ementa POO...", "Objetivos POO...", "Conteúdo POO...",
                    "Metodologia POO...", "Avaliação POO...", "Bib POO Basica", "Bib POO Comp", "professor", "plano_poo.pdf")); // NOVO: Adicionado caminhoPdf
            planos.add(new PlanoDeEnsino(UUID.randomUUID().toString(), "REDES456", "Redes de Computadores",
                    "2025.1", "Ementa Redes...", "Objetivos Redes...", "Conteúdo Redes...",
                    "Metodologia Redes...", "Avaliação Redes...", "Bib Redes Basica", "Bib Redes Comp", "professor", "plano_redes.pdf")); // NOVO: Adicionado caminhoPdf
            salvarPlanosNoArquivo(); // Salva os planos iniciais no arquivo
        } else {
            System.out.println("DEBUG: Planos de ensino carregados do arquivo.");
        }
    }

    public static synchronized PlanoDeEnsinoRepository getInstance() {
        if (instance == null) {
            instance = new PlanoDeEnsinoRepository();
        }
        return instance;
    }

    private List<PlanoDeEnsino> carregarPlanosDoArquivo() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, PlanoDeEnsino.class));
            } catch (IOException e) {
                System.err.println("Erro ao carregar planos do arquivo JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void salvarPlanosNoArquivo() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), planos);
            System.out.println("DEBUG: Planos salvos no arquivo " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Erro ao salvar planos no arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void salvar(PlanoDeEnsino plano) {
        if (plano.getId() == null || plano.getId().isEmpty()) {
            plano.setId(UUID.randomUUID().toString());
            planos.add(plano);
        } else {
            boolean found = false;
            for (int i = 0; i < planos.size(); i++) {
                if (planos.get(i).getId().equals(plano.getId())) {
                    planos.set(i, plano);
                    found = true;
                    break;
                }
            }
            if (!found) {
                planos.add(plano);
            }
        }
        salvarPlanosNoArquivo();
        System.out.println("DEBUG: Plano salvo/atualizado: " + plano.getNomeDisciplina());
    }

    public Optional<PlanoDeEnsino> buscarPorId(String id) {
        return planos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<PlanoDeEnsino> buscarTodos() {
        return new ArrayList<>(planos);
    }

    public void excluir(String id) {
        planos.removeIf(p -> p.getId().equals(id));
        salvarPlanosNoArquivo();
        System.out.println("DEBUG: Plano excluído: " + id);
    }
}