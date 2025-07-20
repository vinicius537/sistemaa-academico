package com.university.academic.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Comentario {
    private String id;
    private String idPlanoDeEnsino;
    private String idAutor;
    private String nomeAutor;
    private String conteudo;
    private LocalDateTime dataHora;
    private String idComentarioPai;

    public Comentario() {
    }

    public Comentario(String id, String idPlanoDeEnsino, String idAutor, String nomeAutor,
                      String conteudo, LocalDateTime dataHora, String idComentarioPai) {
        this.id = id;
        this.idPlanoDeEnsino = idPlanoDeEnsino;
        this.idAutor = idAutor;
        this.nomeAutor = nomeAutor;
        this.conteudo = conteudo;
        this.dataHora = dataHora;
        this.idComentarioPai = idComentarioPai;
    }

    public Comentario(String idPlanoDeEnsino, String idAutor, String nomeAutor, String conteudo, String idComentarioPai) {
        this(null, idPlanoDeEnsino, idAutor, nomeAutor, conteudo, LocalDateTime.now(), idComentarioPai);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdPlanoDeEnsino() { return idPlanoDeEnsino; }
    public void setIdPlanoDeEnsino(String idPlanoDeEnsino) { this.idPlanoDeEnsino = idPlanoDeEnsino; }
    public String getIdAutor() { return idAutor; }
    public void setIdAutor(String idAutor) { this.idAutor = idAutor; }
    public String getNomeAutor() { return nomeAutor; }
    public void setNomeAutor(String nomeAutor) { this.nomeAutor = nomeAutor; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getIdComentarioPai() { return idComentarioPai; }
    public void setIdComentarioPai(String idComentarioPai) { this.idComentarioPai = idComentarioPai; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comentario that = (Comentario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comentario{" +
                "autor='" + nomeAutor + '\'' +
                ", plano='" + idPlanoDeEnsino + '\'' +
                ", data=" + dataHora.toLocalDate() +
                ", conteudo='" + conteudo.substring(0, Math.min(conteudo.length(), 30)) + "...'"+
                '}';
    }
}