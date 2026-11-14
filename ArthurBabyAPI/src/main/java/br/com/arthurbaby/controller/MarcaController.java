package br.com.arthurbaby.controller;

import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Enums.CategoriaStatus;
import br.com.arthurbaby.repository.MarcaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/marcas")
@Tag(name = "Administração - Marcas", description = "Gerenciamento de marcas e cadastro ágil (obter-ou-criar) para uso em produtos.")
public class MarcaController {

    private final MarcaRepository repository;

    public MarcaController(MarcaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar marcas")
    public ResponseEntity<List<Marca>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    @Operation(summary = "Cadastrar marca")
    public ResponseEntity<Marca> criar(@RequestBody Marca marca) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(marca));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar marca")
    public ResponseEntity<Marca> atualizar(@PathVariable Long id, @RequestBody Marca marcaAtualizada) {
        return repository.findById(id).map(marca -> {
            marca.setNome(marcaAtualizada.getNome());
            if (marcaAtualizada.getStatus() != null) marca.setStatus(marcaAtualizada.getStatus());
            return ResponseEntity.ok(repository.save(marca));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir marca")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Não é possível excluir: esta marca está sendo usada por um produto."));
        }
    }

    @PostMapping("/obter-ou-criar")
    @Operation(summary = "Obter ou criar marca (Cadastro Ágil)", description = "Busca a marca pelo nome. Se não existir, cria uma nova e retorna o ID para o cadastro do produto.")
    public ResponseEntity<?> obterOuCriar(@RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O nome da marca é obrigatório."));
        }
        nome = nome.trim();

        Optional<Marca> existente = repository.findByNomeIgnoreCase(nome);

        if (existente.isPresent()) {
            Marca m = existente.get();
            return ResponseEntity.ok(Map.of(
                    "id", m.getId(),
                    "nome", m.getNome(),
                    "status", m.getStatus().name(),
                    "criada", false
            ));
        }

        Marca nova = new Marca();
        nova.setNome(nome);
        nova.setStatus(CategoriaStatus.ATIVA);
        nova = repository.save(nova);

        return ResponseEntity.ok(Map.of(
                "id", nova.getId(),
                "nome", nova.getNome(),
                "status", nova.getStatus().name(),
                "criada", true
        ));
    }
}