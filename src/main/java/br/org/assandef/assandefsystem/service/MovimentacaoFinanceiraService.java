// =========================================================
// MovimentacaoFinanceiraService.java
// =========================================================
package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import br.org.assandef.assandefsystem.repository.MovimentacaoFinanceiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoFinanceiraService {

    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    public List<MovimentacaoFinanceira> findAll() {
        return movimentacaoFinanceiraRepository.findAll();
    }

    public MovimentacaoFinanceira findById(Integer id) {
        return movimentacaoFinanceiraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
    }

    public MovimentacaoFinanceira save(MovimentacaoFinanceira movimentacao) {
        return movimentacaoFinanceiraRepository.save(movimentacao);
    }

    public void deleteById(Integer id) {
        if (!movimentacaoFinanceiraRepository.existsById(id)) {
            throw new RuntimeException("Movimentação não encontrada com ID: " + id);
        }
        movimentacaoFinanceiraRepository.deleteById(id);
    }
}   