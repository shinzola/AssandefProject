package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.repository.MaterialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {
    private final MaterialRepository materialRepository;

    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    public Material findById(Integer id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));
    }

    public Material save(Material material) {
        return materialRepository.save(material);
    }

    public void deleteById(Integer id) {
        materialRepository.deleteById(id);
    }

    public List<Material> findByCategoria(Integer idCategoria) {
        return materialRepository.findByCategoria_IdCategoria(idCategoria);
    }

    public List<Material> findEstoqueBaixo(Integer quantidade) {
        return materialRepository.findByQuantidadeAtualLessThan(quantidade);
    }

    public List<Material> findVencidos() {
        return materialRepository.findByDataValidadeBefore(LocalDate.now());
    }

    @Transactional
    public void baixarEstoque(Integer idMaterial, Integer quantidade) {
        Material material = materialRepository.findById(idMaterial)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        Integer estoqueAtual = material.getQuantidadeAtual() != null ? material.getQuantidadeAtual() : 0;

        if (estoqueAtual < quantidade) {
            throw new RuntimeException("Estoque insuficiente para realizar a baixa");
        }

        material.setQuantidadeAtual(estoqueAtual - quantidade);
        materialRepository.save(material);
    }
}