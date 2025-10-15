package br.org.assandef.assandefsystem.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.service.MaterialService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/materiais")
@CrossOrigin(origins = "*")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping
    public ResponseEntity<List<Material>> listarTodos() {
        List<Material> materiais = materialService.listarTodos();
        return ResponseEntity.ok(materiais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> buscarPorId(@PathVariable Integer id) {
        return materialService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Material>> buscarPorNome(@RequestParam String nome) {
        List<Material> materiais = materialService.buscarPorNome(nome);
        return ResponseEntity.ok(materiais);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Material>> buscarPorCategoria(@PathVariable Integer categoriaId) {
        List<Material> materiais = materialService.buscarPorCategoria(categoriaId);
        return ResponseEntity.ok(materiais);
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<Material>> buscarEstoqueBaixo(@RequestParam(defaultValue = "10") Integer quantidade) {
        List<Material> materiais = materialService.buscarEstoqueBaixo(quantidade);
        return ResponseEntity.ok(materiais);
    }

    @PostMapping
    public ResponseEntity<Material> criar(@Valid @RequestBody Material material) {
        Material novoMaterial = materialService.salvar(material);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoMaterial);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> atualizar(@PathVariable Integer id, 
                                               @Valid @RequestBody Material material) {
        try {
            Material materialAtualizado = materialService.atualizar(id, material);
            return ResponseEntity.ok(materialAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<Material> atualizarEstoque(@PathVariable Integer id, 
                                                      @RequestBody Map<String, Integer> body) {
        try {
            Integer quantidade = body.get("quantidade");
            Material materialAtualizado = materialService.atualizarEstoque(id, quantidade);
            return ResponseEntity.ok(materialAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (materialService.existe(id)) {
            materialService.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
