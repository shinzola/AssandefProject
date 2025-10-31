package br.org.assandef.assandefsystem.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;

    public List<Funcionario> findAll() {
        return funcionarioRepository.findAll();
    }

    public Funcionario findById(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    public Funcionario save(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    public void deleteById(Integer id) {
        funcionarioRepository.deleteById(id);
    }

    public Funcionario findByLogin(String login) {
        return funcionarioRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }
}