package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Integer> {
    List<Telefone> findByPacienteIdPaciente(Integer idPaciente);
    List<Telefone> findByPaciente_IdPaciente(Integer idPaciente);
}
