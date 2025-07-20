package com.university.academic.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {
    private String id;
    private String nomeUsuario;
    private String senha;
    private String email;
    private String papel;
    private boolean isVerified;
    private String matricula;

    private String codigoRecuperacao;
    private LocalDateTime validadeCodigoRecuperacao;


    public Usuario() {
    }

    public Usuario(String id, String nomeUsuario, String senha, String email, String papel, boolean isVerified, String matricula, String codigoRecuperacao, LocalDateTime validadeCodigoRecuperacao) {
        this.id = id;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.email = email;
        this.papel = papel;
        this.isVerified = isVerified;
        this.matricula = matricula;
        this.codigoRecuperacao = codigoRecuperacao;
        this.validadeCodigoRecuperacao = validadeCodigoRecuperacao;
    }

    public Usuario(String id, String nomeUsuario, String senha, String email, String papel, boolean isVerified, String matricula) {
        this(id, nomeUsuario, senha, email, papel, isVerified, matricula, null, null);
    }

    public Usuario(String nomeUsuario, String senha, String email, String papel) {
        this(null, nomeUsuario, senha, email, papel, false, null);
    }

    public Usuario(String nomeUsuario, String senha, String email, String papel, String matricula) {
        this(null, nomeUsuario, senha, email, papel, false, matricula);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCodigoRecuperacao() { return codigoRecuperacao; }
    public void setCodigoRecuperacao(String codigoRecuperacao) { this.codigoRecuperacao = codigoRecuperacao; }
    public LocalDateTime getValidadeCodigoRecuperacao() { return validadeCodigoRecuperacao; }
    public void setValidadeCodigoRecuperacao(LocalDateTime validadeCodigoRecuperacao) { this.validadeCodigoRecuperacao = validadeCodigoRecuperacao; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        if ("Estudante".equalsIgnoreCase(papel) && matricula != null && !matricula.isEmpty()) {
            return nomeUsuario + " (" + matricula + ")";
        }
        return nomeUsuario + " (" + papel + ")";
    }
}