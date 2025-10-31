package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.model.StatusSolicitacao;
import br.org.assandef.assandefsystem.repository.SolicitacoesMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacoesMaterialService {
    private final SolicitacoesMaterialRepository solicitacoesRepository;

    public List<SolicitacoesMaterial> findAll() {
        return solicitacoesRepository.findAll();
    }

    public SolicitacoesMaterial findById(Integer id) {
        return solicitacoesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
    }

    public SolicitacoesMaterial save(SolicitacoesMaterial solicitacao) {
        return solicitacoesRepository.save(solicitacao);
    }

    public void deleteById(Integer id) {
        solicitacoesRepository.deleteById(id);
    }

    public List<SolicitacoesMaterial> findByStatus(StatusSolicitacao status) {
        return solicitacoesRepository.findByStatus(status);
    }
}
