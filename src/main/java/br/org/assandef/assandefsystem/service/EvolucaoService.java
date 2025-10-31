package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Evolucao;
import br.org.assandef.assandefsystem.repository.EvolucaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvolucaoService {
    private final EvolucaoRepository evolucaoRepository;

    public List<Evolucao> findAll() {
        return evolucaoRepository.findAll();
    }

    public Evolucao findById(Integer id) {
        return evolucaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evolução não encontrada"));
    }

    public Evolucao save(Evolucao evolucao) {
        return evolucaoRepository.save(evolucao);
    }

    public void deleteById(Integer id) {
        evolucaoRepository.deleteById(id);
    }

    public List<Evolucao> findByAtendimento(Integer idAtendimento) {
        return evolucaoRepository.findByAtendimentoIdAtendimento(idAtendimento);
    }
}