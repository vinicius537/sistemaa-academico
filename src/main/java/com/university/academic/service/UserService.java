package com.university.academic.service;

import com.university.academic.model.Usuario;
import com.university.academic.model.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class UserService {
    private static UserService instance;
    private UserRepository userRepository;
    private Usuario currentUser;

    private UserService() {
        this.userRepository = UserRepository.getInstance();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public Optional<Usuario> autenticar(String nomeUsuario, String senha) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorNomeUsuario(nomeUsuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (usuario.getSenha().equals(senha)) {
                if (!usuario.isVerified()) {
                    System.out.println("DEBUG: Autenticação falhou: Usuário não verificado.");
                    throw new IllegalStateException("Sua conta ainda não foi verificada. Por favor, verifique seu email.");
                }
                this.currentUser = usuario;
                System.out.println("DEBUG: Autenticação bem-sucedida para: " + nomeUsuario);
                return Optional.of(usuario);
            }
        }
        System.out.println("DEBUG: Falha na autenticação para: " + nomeUsuario);
        return Optional.empty();
    }

    public Optional<String> iniciarResetDeSenha(String email) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorEmail(email);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Nenhum usuário encontrado com o e-mail fornecido.");
        }

        Usuario usuario = usuarioOpt.get();
        String codigo = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime validade = LocalDateTime.now().plusMinutes(15);

        usuario.setCodigoRecuperacao(codigo);
        usuario.setValidadeCodigoRecuperacao(validade);
        userRepository.salvar(usuario);

        System.out.println("DEBUG: Código de recuperação " + codigo + " gerado para " + usuario.getNomeUsuario());

        return Optional.of(codigo);
    }


    public void finalizarResetDeSenha(String codigoRecuperacao, String novaSenha) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorCodigoRecuperacao(codigoRecuperacao);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Código de recuperação inválido.");
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getValidadeCodigoRecuperacao().isBefore(LocalDateTime.now())) {
            usuario.setCodigoRecuperacao(null);
            usuario.setValidadeCodigoRecuperacao(null);
            userRepository.salvar(usuario);
            throw new IllegalStateException("Seu código de recuperação expirou. Por favor, solicite um novo.");
        }

        validatePasswordPolicy(novaSenha);
        usuario.setSenha(novaSenha);

        usuario.setCodigoRecuperacao(null);
        usuario.setValidadeCodigoRecuperacao(null);
        userRepository.salvar(usuario);

        System.out.println("DEBUG: Senha redefinida com sucesso para o usuário " + usuario.getNomeUsuario());
    }

    public Usuario getUsuarioLogado() {
        return currentUser;
    }

    public void setUsuarioLogado(Usuario usuario) {
        this.currentUser = usuario;
    }

    public boolean podeVisualizarTodosUsuarios(Usuario usuario) {
        return usuario != null && "Diretor".equalsIgnoreCase(usuario.getPapel());
    }

    public boolean podeCadastrarPlano(Usuario usuario) {
        if (usuario == null) return false;
        String papel = usuario.getPapel().toLowerCase();
        return papel.equals("diretor") || papel.equals("coordenador") || papel.equals("professor");
    }

    public boolean podeCadastrarDisciplina(Usuario usuario) {
        if (usuario == null) return false;
        String papel = usuario.getPapel().toLowerCase();
        return papel.equals("diretor") || papel.equals("coordenador");
    }

    public boolean podeComentar(Usuario usuario) {
        if (usuario == null) return false;
        String papel = usuario.getPapel().toLowerCase();
        return papel.equals("diretor") || papel.equals("coordenador") || papel.equals("professor") || papel.equals("estudante");
    }

    public boolean podeVisualizarDisciplina(Usuario usuario, Object disciplina) {
        if (usuario == null) return false;
        String papel = usuario.getPapel();
        if ("Diretor".equalsIgnoreCase(papel) || "Coordenador".equalsIgnoreCase(papel) || "Professor".equalsIgnoreCase(papel)) {
            return true;
        }
        if ("Estudante".equalsIgnoreCase(papel)) {
            return false;
        }
        return false;
    }

    public List<Usuario> buscarUsuariosVisiveis(Usuario usuarioLogado) {
        if (podeVisualizarTodosUsuarios(usuarioLogado)) {
            return userRepository.buscarTodos();
        }
        return List.of(usuarioLogado);
    }

    public void registrarNovoUsuario(String nomeUsuario, String senha, String email, String papel, String matricula) {
        if (userRepository.buscarPorNomeUsuario(nomeUsuario).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já existe.");
        }
        if ("Diretor".equalsIgnoreCase(papel) && userRepository.existeDiretor()) {
            throw new IllegalArgumentException("Já existe um Diretor cadastrado no sistema.");
        }

        if ("Estudante".equalsIgnoreCase(papel)) {
            if (matricula == null || matricula.trim().isEmpty()) {
                throw new IllegalArgumentException("Matrícula é obrigatória para Estudantes.");
            }
            if (userRepository.matriculaExiste(matricula)) {
                throw new IllegalArgumentException("Matrícula já cadastrada.");
            }
        } else {
            matricula = null;
        }
        validatePasswordPolicy(senha);

        Usuario novoUsuario = new Usuario(nomeUsuario, senha, email, papel, matricula);
        userRepository.salvar(novoUsuario);
        System.out.println("DEBUG: Novo usuário registrado: " + novoUsuario.getNomeUsuario() + " (Não verificado)");
    }

    public Usuario getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
        System.out.println("DEBUG: Usuário deslogado.");
    }

    public void atualizarUsuario(Usuario usuarioAtualizado) {
        if (usuarioAtualizado == null || usuarioAtualizado.getId() == null) {
            throw new IllegalArgumentException("Dados do usuário inválidos para atualização.");
        }
        if (currentUser != null && !currentUser.getId().equals(usuarioAtualizado.getId())
                && !"Diretor".equalsIgnoreCase(currentUser.getPapel())) {
            throw new IllegalArgumentException("Você não tem permissão para atualizar este usuário.");
        }
        if ("Diretor".equalsIgnoreCase(usuarioAtualizado.getPapel())) {
            Optional<Usuario> existingDirector = userRepository.buscarTodos().stream()
                    .filter(u -> "Diretor".equalsIgnoreCase(u.getPapel()) && !u.getId().equals(usuarioAtualizado.getId()))
                    .findFirst();
            if (existingDirector.isPresent()) {
                throw new IllegalArgumentException("Já existe um Diretor no sistema. Não é possível ter mais de um.");
            }
        }
        userRepository.salvar(usuarioAtualizado);
        if (this.currentUser != null && this.currentUser.getId().equals(usuarioAtualizado.getId())) {
            this.currentUser = usuarioAtualizado;
        }
        System.out.println("DEBUG: Usuário atualizado: " + usuarioAtualizado.getNomeUsuario());
    }

    public void alterarSenha(String userId, String senhaAtual, String novaSenha) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorId(userId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (!usuario.getSenha().equals(senhaAtual)) {
                throw new IllegalArgumentException("Senha atual incorreta.");
            }
            validatePasswordPolicy(novaSenha);

            usuario.setSenha(novaSenha);
            userRepository.salvar(usuario);
            if (this.currentUser != null && this.currentUser.getId().equals(userId)) {
                this.currentUser = usuario;
            }
            System.out.println("DEBUG: Senha do usuário " + usuario.getNomeUsuario() + " alterada.");
        } else {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
    }

    public void excluirConta(String userId, String senha) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorId(userId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (!usuario.getSenha().equals(senha)) {
                throw new IllegalArgumentException("Senha incorreta. Não é possível excluir a conta.");
            }
            if ("Diretor".equalsIgnoreCase(usuario.getPapel()) && userRepository.buscarTodos().stream().filter(u -> "Diretor".equalsIgnoreCase(u.getPapel())).count() == 1) {
                throw new IllegalArgumentException("Não é possível excluir o único Diretor do sistema.");
            }

            userRepository.excluir(userId);
            if (this.currentUser != null && this.currentUser.getId().equals(userId)) {
                this.logout();
            }
            System.out.println("DEBUG: Usuário " + usuario.getNomeUsuario() + " excluído.");
        } else {
            throw new IllegalArgumentException("Usuário não encontrado para exclusão.");
        }
    }

    public void excluirContaPorAdmin(String userId) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorId(userId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if ("Diretor".equalsIgnoreCase(usuario.getPapel()) && userRepository.buscarTodos().stream().filter(u -> "Diretor".equalsIgnoreCase(u.getPapel())).count() == 1) {
                throw new IllegalArgumentException("Não é possível excluir o único Diretor do sistema.");
            }

            userRepository.excluir(userId);
            if (this.currentUser != null && this.currentUser.getId().equals(userId)) {
                this.logout();
            }
            System.out.println("DEBUG: Usuário " + usuario.getNomeUsuario() + " excluído por Admin.");
        } else {
            throw new IllegalArgumentException("Usuário não encontrado para exclusão por Admin.");
        }
    }

    public void verificarUsuario(String userId) {
        Optional<Usuario> usuarioOpt = userRepository.buscarPorId(userId);
        usuarioOpt.ifPresent(usuario -> {
            usuario.setVerified(true);
            userRepository.salvar(usuario);
            System.out.println("DEBUG: Usuário " + usuario.getNomeUsuario() + " verificado.");
        });
    }

    public List<Usuario> buscarTodosUsuarios() {
        return userRepository.buscarTodos();
    }

    public Optional<Usuario> buscarUsuarioPorNomeUsuario(String nomeUsuario) {
        return userRepository.buscarPorNomeUsuario(nomeUsuario);
    }

    private void validatePasswordPolicy(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres.");
        }
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9]");
        if (!specialCharPattern.matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 caractere especial.");
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 letra maiúscula.");
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 número.");
        }
    }
}