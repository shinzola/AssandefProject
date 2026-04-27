// =========================================================
// CategoriaFinanceiraRepository.java
// =========================================================
package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.CategoriaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Integer> {
}