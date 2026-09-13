package br.com.arthurbaby.repositories;

import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.models.Endereco;

public class EnderecoRepository {

    private static EnderecoRepository instance;
    private final List<Endereco> enderecos = new ArrayList<>();

    private EnderecoRepository() {
        // Mock
        enderecos.add(new Endereco("40020-455", "Avenida Sete de Setembro", "548",
                "Ed. Fatima Loja", "Centro", "Salvador", "BA", true));
    }

    public static EnderecoRepository getInstance() {
        if (instance == null) instance = new EnderecoRepository();
        return instance;
    }

    public List<Endereco> getEnderecos() { return enderecos; }

    public void adicionar(Endereco e) { enderecos.add(e); }

    public void remover(Endereco e) { enderecos.remove(e); }
}