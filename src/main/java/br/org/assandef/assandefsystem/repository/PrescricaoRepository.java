package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Prescricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescricaoRepository extends JpaRepository<Prescricao, Integer> {
    List<Prescricao> findByEvolucaoIdEvolucao(Integer idEvolucao);
    List<Prescricao> findByTipo(String tipo);
}