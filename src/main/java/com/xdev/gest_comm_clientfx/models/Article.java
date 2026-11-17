package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Article entity. Java field names stay snake_case (legacy ADF naming);
 * the PyGestComm API speaks camelCase — the @JsonProperty annotations
 * bridge the two so JSON maps one-to-one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {

    @JsonProperty("idArticle")
    private Long id_article;
    @JsonProperty("refArticle")
    private String ref_article;
    @JsonProperty("descArticle")
    private String desc_article;
    @JsonProperty("prixAchatUnitaire")
    private BigDecimal prix_achat_unitaire;
    @JsonProperty("remiseAchat")
    private Double remise_achat;
    @JsonProperty("tvaAchat")
    private Double tva_achat;
    @JsonProperty("prixVenteUnitaire")
    private BigDecimal prix_vente_unitaire;
    @JsonProperty("remiseVente")
    private Double remise_vente;
    @JsonProperty("tvaVente")
    private Double tva_vente;
    @JsonProperty("dernierPrixVenteUnitaire")
    private BigDecimal dernier_prix_vente_unitaire;

    public Long getId_article() {
        return id_article;
    }

    public void setId_article(Long id_article) {
        this.id_article = id_article;
    }

    public String getRef_article() {
        return ref_article;
    }

    public void setRef_article(String ref_article) {
        this.ref_article = ref_article;
    }

    public String getDesc_article() {
        return desc_article;
    }

    public void setDesc_article(String desc_article) {
        this.desc_article = desc_article;
    }

    public BigDecimal getPrix_achat_unitaire() {
        return prix_achat_unitaire;
    }

    public void setPrix_achat_unitaire(BigDecimal prix_achat_unitaire) {
        this.prix_achat_unitaire = prix_achat_unitaire;
    }

    public Double getRemise_achat() {
        return remise_achat;
    }

    public void setRemise_achat(Double remise_achat) {
        this.remise_achat = remise_achat;
    }

    public Double getTva_achat() {
        return tva_achat;
    }

    public void setTva_achat(Double tva_achat) {
        this.tva_achat = tva_achat;
    }

    public BigDecimal getPrix_vente_unitaire() {
        return prix_vente_unitaire;
    }

    public void setPrix_vente_unitaire(BigDecimal prix_vente_unitaire) {
        this.prix_vente_unitaire = prix_vente_unitaire;
    }

    public Double getRemise_vente() {
        return remise_vente;
    }

    public void setRemise_vente(Double remise_vente) {
        this.remise_vente = remise_vente;
    }

    public Double getTva_vente() {
        return tva_vente;
    }

    public void setTva_vente(Double tva_vente) {
        this.tva_vente = tva_vente;
    }

    public BigDecimal getDernier_prix_vente_unitaire() {
        return dernier_prix_vente_unitaire;
    }

    public void setDernier_prix_vente_unitaire(BigDecimal dernier_prix_vente_unitaire) {
        this.dernier_prix_vente_unitaire = dernier_prix_vente_unitaire;
    }
}
