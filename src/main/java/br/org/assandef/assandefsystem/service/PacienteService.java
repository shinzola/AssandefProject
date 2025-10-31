package br.org.assandef.assandefsystem.service;

import java.util.List;

import br.org.assandef.assandefsystem.model.Paciente;
import org.springframework.stereotype.Service;

import br.org.assandef.assandefsystem.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteService {
    private final PacienteRepository pacienteRepository;

    public List<Paciente> findAll() {
        return pacienteRepository.findAll();
    }

    public Paciente findById(Integer id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
    }

    public Paciente save(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public void deleteById(Integer id) {
        pacienteRepository.deleteById(id);
    }

    public Paciente findByCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
    }
}