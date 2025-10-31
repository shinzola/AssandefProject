package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Telefone;
import br.org.assandef.assandefsystem.repository.TelefoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelefoneService {
    private final TelefoneRepository telefoneRepository;

    public List<Telefone> findAll() {
        return telefoneRepository.findAll();
    }

    public Telefone findById(Integer id) {
        return telefoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Telefone não encontrado"));
    }

    public Telefone save(Telefone telefone) {
        return telefoneRepository.save(telefone);
    }

    public void deleteById(Integer id) {
        telefoneRepository.deleteById(id);
    }

    public List<Telefone> findByPaciente(Integer idPaciente) {
        return telefoneRepository.findByPacienteIdPaciente(idPaciente);
    }
}