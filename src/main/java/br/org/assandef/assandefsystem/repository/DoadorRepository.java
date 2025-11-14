package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Doador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoadorRepository extends JpaRepository<Doador, Integer> {
    boolean existsByCpfCnpj(String cpfCnpj);
    boolean existsByCpfCnpjAndIdDoadorNot(String cpfCnpj, Integer idDoador);

    // Email (case insensitive)
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdDoadorNot(String email, Integer idDoador);

    // Telefone
    boolean existsByTelefone(String telefone);
    boolean existsByTelefoneAndIdDoadorNot(String telefone, Integer idDoador);
    Optional<Doador> findByCpfCnpj(String cpfCnpj);
    Optional<Doador> findByEmail(String email);

    // Método para verificar duplicidade em novo cadastro
    boolean existsByCpfCnpjOrEmailOrTelefone(String cpfCnpj, String email, String telefone);

    // Método para verificar duplicidade durante edição (ignora o próprio id)
    boolean existsByCpfCnpjOrEmailOrTelefoneAndIdDoadorNot(String cpfCnpj, String email, String telefone, Integer idDoador);

    List<Doador> findByDataCadastroBetween(LocalDate start, LocalDate end);
    List<Doador> findByDataCadastroGreaterThanEqual(LocalDate start);
    List<Doador> findByDataCadastroLessThanEqual(LocalDate end);

}