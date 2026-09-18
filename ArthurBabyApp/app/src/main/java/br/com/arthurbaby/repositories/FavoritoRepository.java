package br.com.arthurbaby.repositories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.arthurbaby.models.Produto;

public class FavoritoRepository {

    private static FavoritoRepository instance;
    private final Set<Long> idsFavoritos = new HashSet<>();

    private FavoritoRepository() {}

    public static FavoritoRepository getInstance() {
        if (instance == null) instance = new FavoritoRepository();
        return instance;
    }

    public boolean isFavorito(Produto p) {
        return idsFavoritos.contains(p.getId());
    }

    public void toggle(Produto p) {
        if (idsFavoritos.contains(p.getId())) {
            idsFavoritos.remove(p.getId());
        } else {
            idsFavoritos.add(p.getId());
        }
    }

    public List<Produto> getFavoritos() {
        List<Produto> lista = new ArrayList<>();
        for (Produto p : ProdutoRepository.getInstance().getTodos()) {
            if (idsFavoritos.contains(p.getId())) lista.add(p);
        }
        return lista;
    }
}