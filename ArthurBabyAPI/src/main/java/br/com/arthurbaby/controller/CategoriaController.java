package br.com.arthurbaby.controller;

import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categorias")
@Tag(name = "Administração - Categorias", description = "Cadastro, edição e listagem de categorias e subcategorias do catálogo.")
public class CategoriaController {

    private final CategoriaRepository repository;
    private final br.com.arthurbaby.service.AuditoriaService auditoria;

    public CategoriaController(CategoriaRepository repository, br.com.arthurbaby.service.AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @GetMapping
    @Operation(summary = "Listar categorias")
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    @Operation(summary = "Cadastrar categoria")
    public ResponseEntity<Categoria> criar(@RequestBody Categoria categoria) {
        categoria.setId(null); // POST sempre cria: nunca sobrescreve um registro existente
        Categoria salvo = repository.save(categoria);
        auditoria.registrar("CRIAR", "Categoria", salvo.getId(), "Criou Categoria #" + salvo.getId() + " (" + salvo.getNome() + ")");
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id, @RequestBody Categoria categoriaAtualizada) {
        return repository.findById(id).map(categoria -> {
            categoria.setNome(categoriaAtualizada.getNome());
            categoria.setDescricao(categoriaAtualizada.getDescricao());
            categoria.setCategoriaPai(categoriaAtualizada.getCategoriaPai());
            if (categoriaAtualizada.getStatus() != null) categoria.setStatus(categoriaAtualizada.getStatus());
            Categoria salvo = repository.save(categoria);
            auditoria.registrar("ATUALIZAR", "Categoria", id, "Atualizou Categoria #" + id + " (" + salvo.getNome() + ")");
            return ResponseEntity.ok(salvo);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        try {
            repository.deleteById(id);
            auditoria.registrar("DELETAR", "Categoria", id, "Excluiu Categoria #" + id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Não é possível excluir: esta categoria está sendo usada por um produto ou subcategoria."));
        }
    }
}