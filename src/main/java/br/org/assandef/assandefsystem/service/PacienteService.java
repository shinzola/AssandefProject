package br.org.assandef.assandefsystem.service;

import java.util.List;

import br.org.assandef.assandefsystem.model.Paciente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.assandef.assandefsystem.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PacienteService {
    private final PacienteRepository pacienteRepository;

    public List<Paciente> findAll() {
        List<Paciente> pacientes = pacienteRepository.findAllWithTelefones();
        System.out.println("=== CARREGANDO PACIENTES ===");
        pacientes.forEach(p -> {
            int qtdTelefones = p.getTelefones() != null ? p.getTelefones().size() : 0;
            System.out.println("Paciente ID " + p.getIdPaciente() +
                             " (" + p.getNomeCompleto() + ") tem " + qtdTelefones + " telefone(s)");

            // Preenche telefone principal para exibição na listagem
            if (p.getTelefones() != null && !p.getTelefones().isEmpty()) {
                p.setTelefonePrincipal(p.getTelefones().get(0).getNumero());
                System.out.println("  -> Telefone principal: " + p.getTelefonePrincipal());
            }
        });
        return pacientes;
    }

    public Paciente findById(Integer id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
    }

    @Transactional
    public Paciente save(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void deleteById(Integer id) {
        pacienteRepository.deleteById(id);
    }

    public Paciente findByCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
    }

    public List<Paciente> buscarPorNomeOuCpf(String busca) {
        List<Paciente> pacientes = pacienteRepository.findByNomeCompletoContainingIgnoreCaseOrCpfContaining(busca, busca);
        // Força inicialização dos telefones e preenche telefone principal
        pacientes.forEach(p -> {
            if (p.getTelefones() != null) {
                p.getTelefones().size();
                if (!p.getTelefones().isEmpty()) {
                    p.setTelefonePrincipal(p.getTelefones().get(0).getNumero());
                }
            }
        });
        return pacientes;
    }

    public boolean existsByCpf(String cpf) {
        return pacienteRepository.existsByCpf(cpf);
    }

    public boolean temAtendimentosVinculados(Integer idPaciente) {
        return pacienteRepository.countAtendimentosByPacienteId(idPaciente) > 0;
    }

    public int contarAtendimentos(Integer idPaciente) {
        return pacienteRepository.countAtendimentosByPacienteId(idPaciente);
    }
}