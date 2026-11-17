package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Facture (invoice) as returned by PyGestComm /api/facture. Field names
 * match the backend JSON 1:1. `client`/`devis` are backend ids;
 * `codeClient` / `nomPrenomClient` / `codeDevis` / totals are read-only
 * extras the API adds.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Facture {

    private Long idFacture;
    private String codeFacture;
    private String dateFacture;
    private Long client;
    private Long devis;
    private String statutFacture;
    private String observationFacture;
    private List<LigneFacture> lignes;
    private String codeClient;
    private String nomPrenomClient;
    private String codeDevis;
    private Double totalHT;
    private Double totalTVA;
    private Double totalTTC;

    public Long getIdFacture() { return idFacture; }
    public void setIdFacture(Long idFacture) { this.idFacture = idFacture; }
    public String getCodeFacture() { return codeFacture; }
    public void setCodeFacture(String codeFacture) { this.codeFacture = codeFacture; }
    public String getDateFacture() { return dateFacture; }
    public void setDateFacture(String dateFacture) { this.dateFacture = dateFacture; }
    public Long getClient() { return client; }
    public void setClient(Long client) { this.client = client; }
    public Long getDevis() { return devis; }
    public void setDevis(Long devis) { this.devis = devis; }
    public String getStatutFacture() { return statutFacture; }
    public void setStatutFacture(String statutFacture) { this.statutFacture = statutFacture; }
    public String getObservationFacture() { return observationFacture; }
    public void setObservationFacture(String observationFacture) { this.observationFacture = observationFacture; }
    public List<LigneFacture> getLignes() { return lignes; }
    public void setLignes(List<LigneFacture> lignes) { this.lignes = lignes; }
    public String getCodeClient() { return codeClient; }
    public void setCodeClient(String codeClient) { this.codeClient = codeClient; }
    public String getNomPrenomClient() { return nomPrenomClient; }
    public void setNomPrenomClient(String nomPrenomClient) { this.nomPrenomClient = nomPrenomClient; }
    public String getCodeDevis() { return codeDevis; }
    public void setCodeDevis(String codeDevis) { this.codeDevis = codeDevis; }
    public Double getTotalHT() { return totalHT; }
    public void setTotalHT(Double totalHT) { this.totalHT = totalHT; }
    public Double getTotalTVA() { return totalTVA; }
    public void setTotalTVA(Double totalTVA) { this.totalTVA = totalTVA; }
    public Double getTotalTTC() { return totalTTC; }
    public void setTotalTTC(Double totalTTC) { this.totalTTC = totalTTC; }
}
