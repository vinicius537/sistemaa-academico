package com.university.academic.model;

import java.util.Objects;

public class PlanoDeEnsino {
    private String id;
    private String codigoDisciplina;
    private String nomeDisciplina;
    private String semestreLetivo;
    private String ementa;
    private String objetivos;
    private String conteudoProgramatico;
    private String metodologia;
    private String avaliacao;
    private String bibliografiaBasica;
    private String bibliografiaComplementar;
    private String status;
    private String professorResponsavel; // ID ou nome do professor
    private String caminhoPdf; // NOVO: Caminho do arquivo PDF do plano

    // Construtor padrão (sem argumentos) - Necessário para a desserialização do Jackson
    public PlanoDeEnsino() {
    }

    // Construtor completo (AGORA COM 13 ARGUMENTOS)
    public PlanoDeEnsino(String id, String codigoDisciplina, String nomeDisciplina, String semestreLetivo,
                         String ementa, String objetivos, String conteudoProgramatico, String metodologia,
                         String avaliacao, String bibliografiaBasica, String bibliografiaComplementar,
                         String professorResponsavel, String caminhoPdf) { // NOVO: Adicionado caminhoPdf
        this.id = id;
        this.codigoDisciplina = codigoDisciplina;
        this.nomeDisciplina = nomeDisciplina;
        this.semestreLetivo = semestreLetivo;
        this.ementa = ementa;
        this.objetivos = objetivos;
        this.conteudoProgramatico = conteudoProgramatico;
        this.metodologia = metodologia;
        this.avaliacao = avaliacao;
        this.bibliografiaBasica = bibliografiaBasica;
        this.bibliografiaComplementar = bibliografiaComplementar;
        this.professorResponsavel = professorResponsavel;
        this.caminhoPdf = caminhoPdf; // Inicializa o novo campo
        this.status = "Rascunho";
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }
    public String getSemestreLetivo() { return semestreLetivo; }
    public void setSemestreLetivo(String semestreLetivo) { this.semestreLetivo = semestreLetivo; }
    public String getEmenta() { return ementa; }
    public void setEmenta(String ementa) { this.ementa = ementa; }
    public String getObjetivos() { return objetivos; }
    public void setObjetivos(String objetivos) { this.objetivos = objetivos; }
    public String getConteudoProgramatico() { return conteudoProgramatico; }
    public void setConteudoProgramatico(String conteudoProgramatico) { this.conteudoProgramatico = conteudoProgramatico; }
    public String getMetodologia() { return metodologia; }
    public void setMetodologia(String metodologia) { this.metodologia = metodologia; }
    public String getAvaliacao() { return avaliacao; }
    public void setAvaliacao(String avaliacao) { this.avaliacao = avaliacao; }
    public String getBibliografiaBasica() { return bibliografiaBasica; }
    public void setBibliografiaBasica(String bibliografiaBasica) { this.bibliografiaBasica = bibliografiaBasica; }
    public String getBibliografiaComplementar() { return bibliografiaComplementar; }
    public void setBibliografiaComplementar(String bibliografiaComplementar) { this.bibliografiaComplementar = bibliografiaComplementar; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public void setProfessorResponsavel(String professorResponsavel) { this.professorResponsavel = professorResponsavel; }
    public String getCaminhoPdf() { return caminhoPdf; } // NOVO Getter
    public void setCaminhoPdf(String caminhoPdf) { this.caminhoPdf = caminhoPdf; } // NOVO Setter

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanoDeEnsino that = (PlanoDeEnsino) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlanoDeEnsino{" +
                "id='" + id + '\'' +
                ", nomeDisciplina='" + nomeDisciplina + '\'' +
                ", semestreLetivo='" + semestreLetivo + '\'' +
                ", status='" + status + '\'' +
                ", professor='" + professorResponsavel + '\'' +
                '}';
    }
}