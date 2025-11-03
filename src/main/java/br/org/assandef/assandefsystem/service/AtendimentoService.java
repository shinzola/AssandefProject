package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.model.Evolucao;
import br.org.assandef.assandefsystem.repository.AtendimentoRepository;
import br.org.assandef.assandefsystem.repository.EvolucaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtendimentoService {
    private final AtendimentoRepository atendimentoRepository;
    private final EvolucaoRepository evolucaoRepository;

    public List<Atendimento> findAll() {
        return atendimentoRepository.findAllByOrderByDataHoraInicioDesc();
    }

    public Atendimento findById(Integer id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado: " + id));
    }

    @Transactional
    public Atendimento save(Atendimento atendimento) {
        // Se é novo atendimento, define data de início e status
        if (atendimento.getIdAtendimento() == null) {
            atendimento.setDataHoraInicio(LocalDateTime.now());
            if (atendimento.getStatus() == null || atendimento.getStatus().isEmpty()) {
                atendimento.setStatus("Aguardando");
            }
        }
        return atendimentoRepository.save(atendimento);
    }

    @Transactional
    public void iniciarAtendimento(Integer id) {
        Atendimento atendimento = findById(id);
        atendimento.setStatus("Em Andamento");
        atendimentoRepository.save(atendimento);
    }

    @Transactional
    public void finalizarAtendimento(Integer id, String descricaoEvolucao) {
        Atendimento atendimento = findById(id);
        atendimento.setStatus("Finalizado");
        atendimento.setDataHoraFim(LocalDateTime.now());
        atendimentoRepository.save(atendimento);

        // Criar evolução automaticamente
        if (descricaoEvolucao != null && !descricaoEvolucao.trim().isEmpty()) {
            Evolucao evolucao = new Evolucao();
            evolucao.setAtendimento(atendimento);
            evolucao.setDescricao(descricaoEvolucao);
            evolucao.setDataHoraRegistro(LocalDateTime.now());
            evolucaoRepository.save(evolucao);
        }
    }

    @Transactional
    public void cancelarAtendimento(Integer id, String motivo) {
        Atendimento atendimento = findById(id);
        atendimento.setStatus("Cancelado");
        atendimento.setObservacoes(motivo);
        atendimento.setDataHoraFim(LocalDateTime.now());
        atendimentoRepository.save(atendimento);
    }

    public List<Atendimento> findByStatus(String status) {
        return atendimentoRepository.findByStatusOrderByDataHoraInicioAsc(status);
    }

    public List<Atendimento> findFilaEspera() {
        return atendimentoRepository.findByStatusOrderByDataHoraInicioAsc("Aguardando");
    }

    public List<Atendimento> findByPaciente(Integer idPaciente) {
        return atendimentoRepository.findByPaciente_IdPaciente(idPaciente);
    }

    public List<Atendimento> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return atendimentoRepository.findByDataHoraInicioBetween(inicio, fim);
    }

    public Long contarPorStatus(String status) {
        return atendimentoRepository.countByStatus(status);
    }

    public Long calcularTempoEsperaMinutos(Atendimento atendimento) {
        if (atendimento.getDataHoraInicio() != null && "Aguardando".equals(atendimento.getStatus())) {
            return Duration.between(atendimento.getDataHoraInicio(), LocalDateTime.now()).toMinutes();
        }
        return 0L;
    }

    public void deleteById(Integer id) {
        atendimentoRepository.deleteById(id);
    }
}