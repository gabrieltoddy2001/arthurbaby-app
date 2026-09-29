package br.com.arthurbaby.controller;

import br.com.arthurbaby.service.AdminCrudService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCrudController {
    private final AdminCrudService adminCrudService;

    public AdminCrudController(AdminCrudService adminCrudService) {
        this.adminCrudService = adminCrudService;
    }

    @GetMapping("/{tipo}")
    public List<?> listar(@PathVariable String tipo) {
        return adminCrudService.listar(tipo);
    }

    @GetMapping("/{tipo}/{id}")
    public Object buscar(@PathVariable String tipo, @PathVariable Long id) {
        return adminCrudService.buscar(tipo, id);
    }

    @PostMapping("/{tipo}")
    public Object criar(@PathVariable String tipo, @RequestBody Object body) {
        return adminCrudService.criar(tipo, body);
    }

    @PutMapping("/{tipo}/{id}")
    public Object atualizar(@PathVariable String tipo, @PathVariable Long id, @RequestBody Object body) {
        return adminCrudService.atualizar(tipo, id, body);
    }

    @DeleteMapping("/{tipo}/{id}")
    public void excluir(@PathVariable String tipo, @PathVariable Long id) {
        adminCrudService.excluir(tipo, id);
    }
}
