package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendimentoService {
    private final AtendimentoRepository atendimentoRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository) {
        this.atendimentoRepository = atendimentoRepository;
    }

    public List<Atendimento> findAll() {
        return atendimentoRepository.findAll();
    }

    public Atendimento findById(Integer id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado: " + id));
    }

    public Atendimento save(Atendimento a) {
        return atendimentoRepository.save(a);
    }

    public void deleteById(Integer id) {
        atendimentoRepository.deleteById(id);
    }

    public List<Atendimento> findByDataHoraInicioBetween(java.time.LocalDateTime dataInicio, java.time.LocalDateTime dataFim) {
        return atendimentoRepository.findByDataHoraInicioBetween(dataInicio, dataFim);
    }
}