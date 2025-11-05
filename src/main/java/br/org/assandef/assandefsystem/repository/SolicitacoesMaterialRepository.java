package br.org.assandef.assandefsystem.repository;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.model.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SolicitacoesMaterialRepository extends JpaRepository<SolicitacoesMaterial, Integer> {
    List<SolicitacoesMaterial> findByFuncionarioSolicitanteIdFuncionario(Integer idFuncionario);
    List<SolicitacoesMaterial> findByMaterial_IdMaterial(Integer idMaterial);
    List<SolicitacoesMaterial> findByStatus(StatusSolicitacao status);
    // Busca por dataSolicitacao entre dataInicio e dataFim
    List<SolicitacoesMaterial> findByDataSolicitacaoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    // Caso queira buscar a partir de uma data (dataFim pode ser null)
    List<SolicitacoesMaterial> findByDataSolicitacaoAfter(LocalDateTime dataInicio);

    // Caso queira buscar até uma data (dataInicio pode ser null)
    List<SolicitacoesMaterial> findByDataSolicitacaoBefore(LocalDateTime dataFim);
}