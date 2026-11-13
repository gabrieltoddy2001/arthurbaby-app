package br.com.arthurbaby.controller;

import br.com.arthurbaby.entity.Modelo;
import br.com.arthurbaby.repository.ModeloRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/modelos")
@Tag(name = "Administração - Modelos", description = "Cadastro e listagem de modelos (ex: Tradicional, Ursinho) para as variações.")
public class ModeloController {

    private final ModeloRepository repository;

    public ModeloController(ModeloRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar modelos")
    public ResponseEntity<List<Modelo>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    @Operation(summary = "Cadastrar modelo")
    public ResponseEntity<Modelo> criar(@RequestBody Modelo modelo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(modelo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar modelo")
    public ResponseEntity<Modelo> atualizar(@PathVariable Long id, @RequestBody Modelo modeloAtualizado) {
        return repository.findById(id).map(modelo -> {
            modelo.setNome(modeloAtualizado.getNome());
            if (modeloAtualizado.getStatus() != null) modelo.setStatus(modeloAtualizado.getStatus());
            return ResponseEntity.ok(repository.save(modelo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir modelo")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Não é possível excluir: este modelo está sendo usado por um produto."));
        }
    }
}