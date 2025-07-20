package com.university.academic.model;

import java.util.Objects;
import java.util.UUID;

public class AssociacaoDisciplinaUsuario {
    private String id;
    private String idDisciplina; // ID da Disciplina
    private String nomeDisciplina; // Nome da Disciplina (para fácil exibição)
    private String idUsuario;    // ID do Usuário (Professor ou Estudante)
    private String nomeUsuario;  // Nome do Usuário (para fácil exibição)
    private String papelUsuario; // Papel do usuário na associação (ex: "Professor", "Estudante")
    private String idPlanoDeEnsino; // ID do Plano de Ensino associado (opcional, para Professor)

    // Construtor padrão para desserialização do Jackson
    public AssociacaoDisciplinaUsuario() {
    }

    // Construtor completo
    public AssociacaoDisciplinaUsuario(String id, String idDisciplina, String nomeDisciplina, String idUsuario, String nomeUsuario, String papelUsuario, String idPlanoDeEnsino) {
        this.id = id;
        this.idDisciplina = idDisciplina;
        this.nomeDisciplina = nomeDisciplina;
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.papelUsuario = papelUsuario;
        this.idPlanoDeEnsino = idPlanoDeEnsino;
    }

    // Construtor para novas associações (ID será gerado, plano de ensino opcional)
    public AssociacaoDisciplinaUsuario(String idDisciplina, String nomeDisciplina, String idUsuario, String nomeUsuario, String papelUsuario) {
        this(null, idDisciplina, nomeDisciplina, idUsuario, nomeUsuario, papelUsuario, null);
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(String idDisciplina) { this.idDisciplina = idDisciplina; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public String getPapelUsuario() { return papelUsuario; }
    public void setPapelUsuario(String papelUsuario) { this.papelUsuario = papelUsuario; }
    public String getIdPlanoDeEnsino() { return idPlanoDeEnsino; }
    public void setIdPlanoDeEnsino(String idPlanoDeEnsino) { this.idPlanoDeEnsino = idPlanoDeEnsino; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssociacaoDisciplinaUsuario that = (AssociacaoDisciplinaUsuario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nomeDisciplina + " - " + nomeUsuario + " (" + papelUsuario + ")";
    }
}