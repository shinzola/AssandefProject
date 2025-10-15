package br.org.assandef.assandefsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.assandef.assandefsystem.model.Paciente;
import br.org.assandef.assandefsystem.repository.PacienteRepository;

@Service
@Transactional
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> buscarPorId(Integer id) {
        return pacienteRepository.findById(id);
    }

    public List<Paciente> buscarPorNome(String nome) {
        return pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome);
    }

    public Paciente buscarPorCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf);
    }

    public Paciente salvar(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public Paciente atualizar(Integer id, Paciente pacienteAtualizado) {
        return pacienteRepository.findById(id)
            .map(paciente -> {
                paciente.setNomeCompleto(pacienteAtualizado.getNomeCompleto());
                paciente.setCpf(pacienteAtualizado.getCpf());
                paciente.setRg(pacienteAtualizado.getRg());
                paciente.setnSus(pacienteAtualizado.getnSus());
                paciente.setDataNascimento(pacienteAtualizado.getDataNascimento());
                paciente.setSexo(pacienteAtualizado.getSexo());
                paciente.setEndereco(pacienteAtualizado.getEndereco());
                paciente.setNomeResponsavel(pacienteAtualizado.getNomeResponsavel());
                paciente.setContatoResponsavel(pacienteAtualizado.getContatoResponsavel());
                return pacienteRepository.save(paciente);
            })
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado com id: " + id));
    }

    public void deletar(Integer id) {
        pacienteRepository.deleteById(id);
    }

    public boolean existe(Integer id) {
        return pacienteRepository.existsById(id);
    }
}
