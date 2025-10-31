package br.org.assandef.assandefsystem.repository;

import java.util.Optional;

import br.org.assandef.assandefsystem.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    Optional<Paciente> findByCpf(String cpf);
    Optional<Paciente> findBynSus(String nSus);
}