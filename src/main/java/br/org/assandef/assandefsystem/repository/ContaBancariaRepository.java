// =========================================================
// ContaBancariaRepository.java
// =========================================================
package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Integer> {
}