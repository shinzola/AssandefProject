package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.repository.DoadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoadorService {
    private final DoadorRepository doadorRepository;

    public List<Doador> findAll() {
        return doadorRepository.findAll();
    }

    public Doador findById(Integer id) {
        return doadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado"));
    }

    public Doador save(Doador doador) {
        return doadorRepository.save(doador);
    }

    public void deleteById(Integer id) {
        doadorRepository.deleteById(id);
    }

    public Doador findByCpfCnpj(String cpfCnpj) {
        return doadorRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado"));
    }
}