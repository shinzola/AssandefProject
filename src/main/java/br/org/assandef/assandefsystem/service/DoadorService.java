package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.repository.DoadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public boolean existsByCpfCnpjOrEmailOrTelefone(String cpfCnpj, String email, String telefone) {
        return doadorRepository.existsByCpfCnpjOrEmailOrTelefone(cpfCnpj, email, telefone);
    }
    public List<Doador> findByDataCadastroBetween(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return doadorRepository.findAll();
        }
        if (dataInicio == null) {
            return doadorRepository.findByDataCadastroLessThanEqual(dataFim);
        }
        if (dataFim == null) {
            return doadorRepository.findByDataCadastroGreaterThanEqual(dataInicio);
        }
        // Between é inclusivo em ambos os lados
        return doadorRepository.findByDataCadastroBetween(dataInicio, dataFim);
    }
}