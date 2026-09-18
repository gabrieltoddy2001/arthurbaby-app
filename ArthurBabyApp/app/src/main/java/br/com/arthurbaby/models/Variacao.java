package br.com.arthurbaby.models;

import java.io.Serializable;

public class Variacao implements Serializable {
    private String tamanho;
    private String cor;
    private String modelo;

    public Variacao(String tamanho, String cor, String modelo) {
        this.tamanho = tamanho;
        this.cor = cor;
        this.modelo = modelo;
    }

    public String getTamanho() { return tamanho; }
    public String getCor() { return cor; }
    public String getModelo() { return modelo; }

    public void setTamanho(String tamanho) { this.tamanho = tamanho; }
    public void setCor(String cor) { this.cor = cor; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String resumo() {
        return "Tam: " + tamanho + " | Cor: " + cor + " | Mod: " + modelo;
    }
}