// =========================================================
// ContaBancariaService.java
// =========================================================
package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.ContaBancaria;
import br.org.assandef.assandefsystem.repository.ContaBancariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaBancariaService {

    private final ContaBancariaRepository contaBancariaRepository;

    public List<ContaBancaria> findAll() {
        return contaBancariaRepository.findAll();
    }

    public ContaBancaria findById(Integer id) {
        return contaBancariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta bancária não encontrada com ID: " + id));
    }

    public ContaBancaria save(ContaBancaria conta) {
        return contaBancariaRepository.save(conta);
    }

    public void deleteById(Integer id) {
        if (!contaBancariaRepository.existsById(id)) {
            throw new RuntimeException("Conta bancária não encontrada com ID: " + id);
        }
        contaBancariaRepository.deleteById(id);
    }
}