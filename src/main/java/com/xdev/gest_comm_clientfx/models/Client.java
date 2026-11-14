package com.xdev.gest_comm_clientfx.models;

/**
 * Client entity mirroring the Gest_Comm backend model
 * (PyGestComm GestCommApp Client): same field names so JSON payloads map
 * one-to-one. Field types follow the backend — phone numbers are ints.
 */
public class Client {

    private Long idClient;
    private String codeClient;
    private String nomPrenomClient;
    private String matFiscalClient;
    private String addresseClient1;
    private String addresseClient2;
    private Integer numTelClient1;
    private Integer numTelClient2;

    public Long getIdClient() {
        return idClient;
    }

    public void setIdClient(Long idClient) {
        this.idClient = idClient;
    }

    public String getCodeClient() {
        return codeClient;
    }

    public void setCodeClient(String codeClient) {
        this.codeClient = codeClient;
    }

    public String getNomPrenomClient() {
        return nomPrenomClient;
    }

    public void setNomPrenomClient(String nomPrenomClient) {
        this.nomPrenomClient = nomPrenomClient;
    }

    public String getMatFiscalClient() {
        return matFiscalClient;
    }

    public void setMatFiscalClient(String matFiscalClient) {
        this.matFiscalClient = matFiscalClient;
    }

    public String getAddresseClient1() {
        return addresseClient1;
    }

    public void setAddresseClient1(String addresseClient1) {
        this.addresseClient1 = addresseClient1;
    }

    public String getAddresseClient2() {
        return addresseClient2;
    }

    public void setAddresseClient2(String addresseClient2) {
        this.addresseClient2 = addresseClient2;
    }

    public Integer getNumTelClient1() {
        return numTelClient1;
    }

    public void setNumTelClient1(Integer numTelClient1) {
        this.numTelClient1 = numTelClient1;
    }

    public Integer getNumTelClient2() {
        return numTelClient2;
    }

    public void setNumTelClient2(Integer numTelClient2) {
        this.numTelClient2 = numTelClient2;
    }
}
