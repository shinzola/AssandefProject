package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Funcionario> findAll() {
        return funcionarioRepository.findAll();
    }

    public Funcionario findById(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    public Funcionario findByLogin(String login) {
        return funcionarioRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    public void deleteById(Integer id) {
        funcionarioRepository.deleteById(id);
    }

    /**
     * Salva funcionário. Se senhaPlana != null e não vazia, aplica hash e atualiza senha.
     * Se edição sem senhaPlana, preserva senhaHash existente.
     */
    public Funcionario save(Funcionario funcionario, String senhaPlana) {
        if (funcionario.getIdFuncionario() != null) {
            // edição: buscar existente para preservar campos não enviados (principalmente senha)
            Funcionario existente = funcionarioRepository.findById(funcionario.getIdFuncionario())
                    .orElse(null);
            if (existente != null) {
                // preservar senha atual caso nenhuma nova senha tenha sido informada
                if (senhaPlana == null || senhaPlana.trim().isEmpty()) {
                    funcionario.setSenhaHash(existente.getSenhaHash());
                } else {
                    funcionario.setSenhaHash(passwordEncoder.encode(senhaPlana));
                }
            } else {
                // se por algum motivo não encontrado, tratar como novo
                if (senhaPlana != null && !senhaPlana.trim().isEmpty()) {
                    funcionario.setSenhaHash(passwordEncoder.encode(senhaPlana));
                }
            }
        } else {
            // criação
            if (senhaPlana != null && !senhaPlana.trim().isEmpty()) {
                funcionario.setSenhaHash(passwordEncoder.encode(senhaPlana));
            }
        }
        return funcionarioRepository.save(funcionario);
    }
}