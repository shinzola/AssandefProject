// =========================================================
// CategoriaFinanceiraService.java
// =========================================================
package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.CategoriaFinanceira;
import br.org.assandef.assandefsystem.repository.CategoriaFinanceiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaFinanceiraService {

    private final CategoriaFinanceiraRepository categoriaFinanceiraRepository;

    public List<CategoriaFinanceira> findAll() {
        return categoriaFinanceiraRepository.findAll();
    }

    public CategoriaFinanceira findById(Integer id) {
        return categoriaFinanceiraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria financeira não encontrada com ID: " + id));
    }

    public CategoriaFinanceira save(CategoriaFinanceira categoria) {
        return categoriaFinanceiraRepository.save(categoria);
    }

    public void deleteById(Integer id) {
        if (!categoriaFinanceiraRepository.existsById(id)) {
            throw new RuntimeException("Categoria financeira não encontrada com ID: " + id);
        }
        categoriaFinanceiraRepository.deleteById(id);
    }
}