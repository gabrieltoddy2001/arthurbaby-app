package br.com.arthurbaby.models;

import java.io.Serializable;
import java.util.Date;

public class PedidoStatus implements Serializable {
    private String status;
    private Date data;

    public PedidoStatus(String status, Date data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() { return status; }
    public Date getData() { return data; }
}