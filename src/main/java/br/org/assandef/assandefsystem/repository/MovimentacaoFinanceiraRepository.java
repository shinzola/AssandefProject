// =========================================================
// MovimentacaoFinanceiraRepository.java
// =========================================================
package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Integer> {
}