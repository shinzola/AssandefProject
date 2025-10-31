package br.org.assandef.assandefsystem.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.org.assandef.assandefsystem.model.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
    List<Material> findByCategoria_IdCategoria(Integer idCategoria);
    List<Material> findByQuantidadeAtualLessThan(Integer quantidade);
    List<Material> findByDataValidadeBefore(LocalDate data);
}