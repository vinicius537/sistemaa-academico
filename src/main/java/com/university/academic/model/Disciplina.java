package com.university.academic.model;

import java.util.Objects;
import java.util.UUID;

public class Disciplina {
    private String id;
    private String codigo;
    private String nome;
    private int cargaHoraria;
    private String descricao;

    public Disciplina() {
    }

    public Disciplina(String id, String codigo, String nome, int cargaHoraria, String descricao) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.descricao = descricao;
    }

    public Disciplina(String codigo, String nome, int cargaHoraria, String descricao) {
        this(null, codigo, nome, cargaHoraria, descricao);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disciplina that = (Disciplina) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return codigo + " - " + nome; // Representação amigável para ComboBoxes, por exemplo
    }
}