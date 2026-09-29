package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Endereco;

public record EnderecoResponse(
        Long id,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String referencia,
        boolean principal
) {
    public static EnderecoResponse from(Endereco e) {
        return new EnderecoResponse(e.getId(), e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf(), e.getReferencia(), e.isPrincipal());
    }
}
