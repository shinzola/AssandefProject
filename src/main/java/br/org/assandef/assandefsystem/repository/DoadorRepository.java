package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Doador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoadorRepository extends JpaRepository<Doador, Integer> {
    Optional<Doador> findByCpfCnpj(String cpfCnpj);
    Optional<Doador> findByEmail(String email);
    boolean existsByCpfCnpjOrEmailOrTelefone(String cpfCnpj, String email, String telefone);
    List<Doador> findByDataCadastroBetween(LocalDate start, LocalDate end);

    List<Doador> findByDataCadastroGreaterThanEqual(LocalDate start);

    List<Doador> findByDataCadastroLessThanEqual(LocalDate end);
}