package br.org.assandef.assandefsystem.repository;

import java.util.List;
import java.util.Optional;

import br.org.assandef.assandefsystem.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    Optional<Paciente> findByCpf(String cpf);
    Optional<Paciente> findBynSus(String nSus);
    boolean existsByCpf(String cpf);
    List<Paciente> findByNomeCompletoContainingIgnoreCaseOrCpfContaining(String nome, String cpf);
    List<Paciente> findByNomeCompletoContainingIgnoreCase(String nome);

    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN FETCH p.telefones")
    List<Paciente> findAllWithTelefones();

    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN FETCH p.telefones WHERE " +
           "(LOWER(p.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%')) OR p.cpf LIKE CONCAT('%', :busca, '%'))")
    List<Paciente> searchWithTelefones(@Param("busca") String busca);

    @Query("SELECT p FROM Paciente p LEFT JOIN FETCH p.telefones WHERE p.idPaciente = :id")
    Optional<Paciente> findByIdWithTelefones(@Param("id") Integer id);

    @Query("SELECT COUNT(a) FROM Atendimento a WHERE a.paciente.idPaciente = :idPaciente")
    int countAtendimentosByPacienteId(@Param("idPaciente") Integer idPaciente);
}