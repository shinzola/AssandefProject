package br.org.assandef.assandefsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.org.assandef.assandefsystem.model.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
    List<Material> findByNomeContainingIgnoreCase(String nome);
    List<Material> findByCategoriaId(Integer categoriaId);
    List<Material> findByQuantidadeAtualLessThan(Integer quantidade);
}
