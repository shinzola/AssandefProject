package br.org.assandef.assandefsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.org.assandef.assandefsystem.model.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    List<Paciente> findByNomeCompletoContainingIgnoreCase(String nome);
    Paciente findByCpf(String cpf);
}
