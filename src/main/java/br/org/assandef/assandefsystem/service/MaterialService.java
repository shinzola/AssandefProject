package br.org.assandef.assandefsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.repository.MaterialRepository;

@Service
@Transactional
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    public List<Material> listarTodos() {
        return materialRepository.findAll();
    }

    public Optional<Material> buscarPorId(Integer id) {
        return materialRepository.findById(id);
    }

    public List<Material> buscarPorNome(String nome) {
        return materialRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Material> buscarPorCategoria(Integer categoriaId) {
        return materialRepository.findByCategoriaId(categoriaId);
    }

    public List<Material> buscarEstoqueBaixo(Integer quantidade) {
        return materialRepository.findByQuantidadeAtualLessThan(quantidade);
    }

    public Material salvar(Material material) {
        return materialRepository.save(material);
    }

    public Material atualizar(Integer id, Material materialAtualizado) {
        return materialRepository.findById(id)
            .map(material -> {
                material.setNome(materialAtualizado.getNome());
                material.setCategoria(materialAtualizado.getCategoria());
                material.setQuantidadeAtual(materialAtualizado.getQuantidadeAtual());
                material.setFornecedor(materialAtualizado.getFornecedor());
                material.setDataValidade(materialAtualizado.getDataValidade());
                return materialRepository.save(material);
            })
            .orElseThrow(() -> new RuntimeException("Material não encontrado com id: " + id));
    }

    public Material atualizarEstoque(Integer id, Integer quantidade) {
        return materialRepository.findById(id)
            .map(material -> {
                material.setQuantidadeAtual(material.getQuantidadeAtual() + quantidade);
                return materialRepository.save(material);
            })
            .orElseThrow(() -> new RuntimeException("Material não encontrado com id: " + id));
    }

    public void deletar(Integer id) {
        materialRepository.deleteById(id);
    }

    public boolean existe(Integer id) {
        return materialRepository.existsById(id);
    }
}
