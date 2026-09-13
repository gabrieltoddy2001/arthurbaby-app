package br.com.arthurbaby.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Categoria implements Serializable {
    private Long id;
    private String nome;
    private List<Categoria> subcategorias;

    public Categoria(Long id, String nome) {
        this.id = id;
        this.nome = nome;
        this.subcategorias = new ArrayList<>();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public List<Categoria> getSubcategorias() { return subcategorias; }

    public void addSubcategoria(Categoria c) { subcategorias.add(c); }

    public boolean temSubcategorias() { return !subcategorias.isEmpty(); }

    @Override
    public String toString() { return nome; }
}