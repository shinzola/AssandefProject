package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvolucaoRepository extends JpaRepository<Evolucao, Integer> {
    List<Evolucao> findByAtendimentoIdAtendimento(Integer idAtendimento);
}