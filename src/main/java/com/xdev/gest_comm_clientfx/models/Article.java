package com.xdev.gest_comm_clientfx.models;

import java.math.BigDecimal;

public class Article {

    private Long id_article;
    private String ref_article;
    private String desc_article;
    private BigDecimal prix_achat_unitaire;
    private Double remise_achat;
    private Double tva_achat;
    private BigDecimal prix_vente_unitaire;
    private Double remise_vente;
    private Double tva_vente;
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
