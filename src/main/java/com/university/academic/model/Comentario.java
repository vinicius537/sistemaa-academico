package com.university.academic.model;

import java.time.LocalDateTime; // Para registrar a data e hora do comentário
import java.util.Objects;
import java.util.UUID; // Para gerar IDs únicos

public class Comentario {
    private String id;
    private String idPlanoDeEnsino; // ID do Plano de Ensino ao qual o comentário pertence
    private String idAutor;          // ID do Usuário que fez o comentário
    private String nomeAutor;        // Nome do Usuário que fez o comentário (para exibição)
    private String conteudo;         // Conteúdo textual do comentário
    private LocalDateTime dataHora;  // Data e hora em que o comentário foi feito
    private String idComentarioPai;  // ID do comentário pai, se for uma resposta (null se for um comentário principal)

    // Construtor padrão para desserialização do Jackson
    public Comentario() {
    }

    // Construtor completo
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

    // Construtor para novos comentários/respostas (ID será gerado, dataHora será definida ao criar)
    public Comentario(String idPlanoDeEnsino, String idAutor, String nomeAutor, String conteudo, String idComentarioPai) {
        this(null, idPlanoDeEnsino, idAutor, nomeAutor, conteudo, LocalDateTime.now(), idComentarioPai);
    }

    // Getters e Setters
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