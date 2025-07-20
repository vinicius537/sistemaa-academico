package com.university.academic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ComentarioRepository {
    private static ComentarioRepository instance;
    private List<Comentario> comentarios;
    private final String DATA_FILE = "comentarios.json";
    private ObjectMapper objectMapper;

    // Construtor privado para o Singleton
    private ComentarioRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());

        this.comentarios = carregarComentariosDoArquivo();

        if (this.comentarios.isEmpty()) {
            System.out.println("DEBUG: Arquivo de comentários vazio ou não encontrado.");
        } else {
            System.out.println("DEBUG: Comentários carregados do arquivo.");
        }
    }

    // Método estático para obter a instância Singleton
    public static synchronized ComentarioRepository getInstance() {
        if (instance == null) {
            instance = new ComentarioRepository();
        }
        return instance;
    }

    // Carrega comentários do arquivo JSON
    private List<Comentario> carregarComentariosDoArquivo() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Comentario.class));
            } catch (IOException e) {
                System.err.println("Erro ao carregar comentários do arquivo JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    // Salva comentários no arquivo JSON
    private void salvarComentariosNoArquivo() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), comentarios);
            System.out.println("DEBUG: Comentários salvos no arquivo " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Erro ao salvar comentários no arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void salvar(Comentario comentario) {
        if (comentario.getId() == null || comentario.getId().isEmpty()) {
            comentario.setId(UUID.randomUUID().toString());
            comentarios.add(comentario);
        } else {
            boolean found = false;
            for (int i = 0; i < comentarios.size(); i++) {
                if (comentarios.get(i).getId().equals(comentario.getId())) {
                    comentarios.set(i, comentario);
                    found = true;
                    break;
                }
            }
            if (!found) {
                comentarios.add(comentario);
            }
        }
        salvarComentariosNoArquivo();
        System.out.println("DEBUG: Comentário salvo/atualizado: " + comentario.toString());
    }

    public Optional<Comentario> buscarPorId(String id) {
        return comentarios.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public List<Comentario> buscarTodos() {
        return new ArrayList<>(comentarios);
    }

    public List<Comentario> buscarPorIdPlanoDeEnsino(String idPlanoDeEnsino) {
        return comentarios.stream()
                .filter(c -> c.getIdPlanoDeEnsino().equals(idPlanoDeEnsino))
                .collect(Collectors.toList());
    }

    public void excluir(String id) {
        comentarios.removeIf(c -> c.getId().equals(id));
        comentarios.removeIf(c -> c.getIdComentarioPai() != null && c.getIdComentarioPai().equals(id));
        salvarComentariosNoArquivo();
        System.out.println("DEBUG: Comentário excluído: " + id);
    }

    public void excluirPorIdPlanoDeEnsino(String idPlanoDeEnsino) {
        comentarios.removeIf(c -> c.getIdPlanoDeEnsino().equals(idPlanoDeEnsino));
        salvarComentariosNoArquivo();
        System.out.println("DEBUG: Comentários do plano " + idPlanoDeEnsino + " excluídos.");
    }
}