package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Prescricao;
import br.org.assandef.assandefsystem.repository.PrescricaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescricaoService {
    private final PrescricaoRepository prescricaoRepository;

    public List<Prescricao> findAll() {
        return prescricaoRepository.findAll();
    }

    public Prescricao findById(Integer id) {
        return prescricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescrição não encontrada"));
    }

    public Prescricao save(Prescricao prescricao) {
        return prescricaoRepository.save(prescricao);
    }

    public void deleteById(Integer id) {
        prescricaoRepository.deleteById(id);
    }

    public List<Prescricao> findByEvolucao(Integer idEvolucao) {
        return prescricaoRepository.findByEvolucaoIdEvolucao(idEvolucao);
    }
}