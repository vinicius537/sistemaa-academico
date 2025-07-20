package com.university.academic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Módulo para serializar datas (LocalDateTime)

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {
    private static UserRepository instance;
    private List<Usuario> usuarios;
    private final String DATA_FILE = "users.json";
    private final ObjectMapper objectMapper;

    private UserRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());

        this.usuarios = carregarUsuariosDoArquivo();

        if (this.usuarios.isEmpty()) {
            System.out.println("DEBUG: Arquivo de usuários vazio ou não encontrado. Inicializando com Diretor padrão.");
            Usuario diretor = new Usuario(
                    UUID.randomUUID().toString(),
                    "diretor",
                    "diretor123!",
                    "diretor@universidade.com",
                    "Diretor",
                    true,
                    null
            );
            this.usuarios.add(diretor);
            salvarUsuariosNoArquivo();
        } else {
            System.out.println("DEBUG: Usuários carregados do arquivo.");
        }
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    private List<Usuario> carregarUsuariosDoArquivo() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                return objectMapper.readValue(file, objectMapper
                        .getTypeFactory()
                        .constructCollectionType(List.class, Usuario.class));
            } catch (IOException e) {
                System.err.println("Erro ao carregar usuários do arquivo JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void salvarUsuariosNoArquivo() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), usuarios);
            System.out.println("DEBUG: Usuários salvos no arquivo " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Erro ao salvar usuários no arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void salvar(Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            usuario.setId(UUID.randomUUID().toString());
            usuarios.add(usuario);
        } else {
            boolean found = false;
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).getId().equals(usuario.getId())) {
                    usuarios.set(i, usuario);
                    found = true;
                    break;
                }
            }
            if (!found) {
                usuarios.add(usuario);
            }
        }
        salvarUsuariosNoArquivo();
    }

    public Optional<Usuario> buscarPorNomeUsuario(String nomeUsuario) {
        return usuarios.stream()
                .filter(u -> u.getNomeUsuario() != null && u.getNomeUsuario().equalsIgnoreCase(nomeUsuario))
                .findFirst();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarios.stream()
                .filter(u -> u.getId() != null && u.getId().equals(id))
                .findFirst();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Usuario> buscarPorCodigoRecuperacao(String codigo) {
        return usuarios.stream()
                .filter(u -> u.getCodigoRecuperacao() != null && u.getCodigoRecuperacao().equals(codigo))
                .findFirst();
    }

    public List<Usuario> buscarTodos() {
        return new ArrayList<>(usuarios);
    }

    public void excluir(String id) {
        if (usuarios.removeIf(u -> u.getId() != null && u.getId().equals(id))) {
            salvarUsuariosNoArquivo();
        }
    }

    public boolean existeDiretor() {
        return usuarios.stream().anyMatch(u -> "Diretor".equalsIgnoreCase(u.getPapel()));
    }

    public boolean matriculaExiste(String matricula) {
        return usuarios.stream()
                .anyMatch(u -> matricula != null && matricula.equalsIgnoreCase(u.getMatricula()));
    }
}