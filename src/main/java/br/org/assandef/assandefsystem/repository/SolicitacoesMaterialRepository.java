package br.org.assandef.assandefsystem.repository;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.model.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacoesMaterialRepository extends JpaRepository<SolicitacoesMaterial, Integer> {
    List<SolicitacoesMaterial> findByFuncionarioSolicitanteIdFuncionario(Integer idFuncionario);
    List<SolicitacoesMaterial> findByMaterial_IdMaterial(Integer idMaterial);
    List<SolicitacoesMaterial> findByStatus(StatusSolicitacao status);
}