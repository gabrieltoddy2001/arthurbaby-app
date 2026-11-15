package br.com.arthurbaby.controller;

import br.com.arthurbaby.entity.Cor;
import br.com.arthurbaby.repository.CorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cores")
@Tag(name = "Administração - Cores", description = "Cadastro e listagem de cores com código HEX para as variações de produto.")
public class CorController {

    private final CorRepository repository;

    public CorController(CorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar cores")
    public ResponseEntity<List<Cor>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    @Operation(summary = "Cadastrar cor")
    public ResponseEntity<Cor> criar(@RequestBody Cor cor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(cor));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cor")
    public ResponseEntity<Cor> atualizar(@PathVariable Long id, @RequestBody Cor corAtualizada) {
        return repository.findById(id).map(cor -> {
            cor.setNome(corAtualizada.getNome());
            cor.setCodigoHex(corAtualizada.getCodigoHex());
            cor.setOrdemExibicao(corAtualizada.getOrdemExibicao());
            if (corAtualizada.getStatus() != null) cor.setStatus(corAtualizada.getStatus());
            return ResponseEntity.ok(repository.save(cor));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cor")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Não é possível excluir: esta cor está sendo usada por um produto."));
        }
    }
}