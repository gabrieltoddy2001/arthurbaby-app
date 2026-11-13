package br.com.arthurbaby.controller;

import br.com.arthurbaby.entity.Tamanho;
import br.com.arthurbaby.repository.TamanhoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tamanhos")
@Tag(name = "Administração - Tamanhos", description = "Cadastro e listagem de tamanhos (RN, P, M, G...) para as variações de produto.")
public class TamanhoController {

    private final TamanhoRepository repository;

    public TamanhoController(TamanhoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar tamanhos")
    public ResponseEntity<List<Tamanho>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    @Operation(summary = "Cadastrar tamanho")
    public ResponseEntity<Tamanho> criar(@RequestBody Tamanho tamanho) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(tamanho));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tamanho")
    public ResponseEntity<Tamanho> atualizar(@PathVariable Long id, @RequestBody Tamanho tamanhoAtualizado) {
        return repository.findById(id).map(tamanho -> {
            tamanho.setNome(tamanhoAtualizado.getNome());
            tamanho.setOrdemExibicao(tamanhoAtualizado.getOrdemExibicao());
            if (tamanhoAtualizado.getStatus() != null) tamanho.setStatus(tamanhoAtualizado.getStatus());
            return ResponseEntity.ok(repository.save(tamanho));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tamanho")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Não é possível excluir: este tamanho está sendo usado por um produto."));
        }
    }
}