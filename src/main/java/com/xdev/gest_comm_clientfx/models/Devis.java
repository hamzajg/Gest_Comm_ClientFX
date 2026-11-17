package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Devis (quote) as returned by PyGestComm /api/devis. Field names match
 * the backend JSON 1:1. `client` is the backend idClient; `codeClient` /
 * `nomPrenomClient` / totals are read-only extras the API adds.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Devis {

    private Long idDevis;
    private String codeDevis;
    private String dateDevis;
    private Long client;
    private String statutDevis;
    private String observationDevis;
    private List<LigneDevis> lignes;
    private String codeClient;
    private String nomPrenomClient;
    private Double totalHT;
    private Double totalTVA;
    private Double totalTTC;

    public Long getIdDevis() { return idDevis; }
    public void setIdDevis(Long idDevis) { this.idDevis = idDevis; }
    public String getCodeDevis() { return codeDevis; }
    public void setCodeDevis(String codeDevis) { this.codeDevis = codeDevis; }
    public String getDateDevis() { return dateDevis; }
    public void setDateDevis(String dateDevis) { this.dateDevis = dateDevis; }
    public Long getClient() { return client; }
    public void setClient(Long client) { this.client = client; }
    public String getStatutDevis() { return statutDevis; }
    public void setStatutDevis(String statutDevis) { this.statutDevis = statutDevis; }
    public String getObservationDevis() { return observationDevis; }
    public void setObservationDevis(String observationDevis) { this.observationDevis = observationDevis; }
    public List<LigneDevis> getLignes() { return lignes; }
    public void setLignes(List<LigneDevis> lignes) { this.lignes = lignes; }
    public String getCodeClient() { return codeClient; }
    public void setCodeClient(String codeClient) { this.codeClient = codeClient; }
    public String getNomPrenomClient() { return nomPrenomClient; }
    public void setNomPrenomClient(String nomPrenomClient) { this.nomPrenomClient = nomPrenomClient; }
    public Double getTotalHT() { return totalHT; }
    public void setTotalHT(Double totalHT) { this.totalHT = totalHT; }
    public Double getTotalTVA() { return totalTVA; }
    public void setTotalTVA(Double totalTVA) { this.totalTVA = totalTVA; }
    public Double getTotalTTC() { return totalTTC; }
    public void setTotalTTC(Double totalTTC) { this.totalTTC = totalTTC; }

    @Override
    public String toString() {
        return codeDevis + " — " + nomPrenomClient
            + " — TTC " + totalTTC;
    }
}
