package br.org.assandef.assandefsystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.service.MaterialService;

@RestController
@RequestMapping("/api/almoxarifado")
@CrossOrigin(origins = "*")
public class AlmoxarifadoController {

    @Autowired
    private MaterialService materialService;

    /**
     * 🔹 Lista todos os materiais disponíveis no almoxarifado
     */
    @GetMapping
    public ResponseEntity<List<Material>> listarMateriais() {
        List<Material> materiais = materialService.listarTodos();
        return ResponseEntity.ok(materiais);
    }

    /**
     * 🔹 Busca materiais por nome (filtro da tela de almoxarifado)
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Material>> buscarPorNome(@RequestParam String nome) {
        List<Material> materiais = materialService.buscarPorNome(nome);
        return ResponseEntity.ok(materiais);
    }

    /**
     * 🔹 Lista apenas materiais com estoque baixo (para alertas)
     */
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<Material>> buscarEstoqueBaixo(
            @RequestParam(defaultValue = "10") Integer quantidade) {
        List<Material> materiais = materialService.buscarEstoqueBaixo(quantidade);
        return ResponseEntity.ok(materiais);
    }

    /**
     * 🔹 (Opcional) Listar materiais filtrados por categoria, caso usado no frontend
     */
    @GetMapping("/categoria")
    public ResponseEntity<List<Material>> buscarPorCategoria(@RequestParam Integer categoriaId) {
        List<Material> materiais = materialService.buscarPorCategoria(categoriaId);
        return ResponseEntity.ok(materiais);
    }
}
