package br.org.assandef.assandefsystem.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.repository.AtendimentoRepository;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import br.org.assandef.assandefsystem.repository.SolicitacoesMaterialRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AtendimentoRepository atendimentoRepository;
    private final SolicitacoesMaterialRepository solicitacoesMaterialRepository;

    public List<Funcionario> findAll() {
        return funcionarioRepository.findAll();
    }

    public List<Funcionario> findAllAtivos() {
        return funcionarioRepository.findByAtivoTrueOrderByNomeCompletoAsc();
    }

    public Funcionario findById(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    @Transactional
    public Funcionario save(Funcionario funcionario) {
        // Se é novo funcionário, criptografa a senha
        if (funcionario.getIdFuncionario() == null) {
            funcionario.setSenhaHash(passwordEncoder.encode(funcionario.getSenhaHash()));
            funcionario.setAtivo(true);
        } else {
            // Se está editando, mantém a senha atual
            Funcionario existente = findById(funcionario.getIdFuncionario());
            funcionario.setSenhaHash(existente.getSenhaHash());
        }
        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public void desativar(Integer id, Integer idFuncionarioSubstituto) {
        Funcionario funcionario = findById(id);
        funcionario.setAtivo(false);

        // Transferir atendimentos
        if (idFuncionarioSubstituto != null) {
            Funcionario substituto = findById(idFuncionarioSubstituto);
            List<Atendimento> atendimentos = atendimentoRepository.findByFuncionario_IdFuncionario(id);
            atendimentos.forEach(a -> a.setFuncionario(substituto));
            atendimentoRepository.saveAll(atendimentos);

            // Transferir solicitações
            List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialRepository.findByFuncionarioSolicitanteIdFuncionario(id);
            solicitacoes.forEach(s -> s.setFuncionarioSolicitante(substituto));
            solicitacoesMaterialRepository.saveAll(solicitacoes);
        }

        funcionarioRepository.save(funcionario);
    }

    @Transactional
    public void redefinirSenha(Integer id, String novaSenha) {
        Funcionario funcionario = findById(id);
        funcionario.setSenhaHash(passwordEncoder.encode(novaSenha));
        funcionarioRepository.save(funcionario);
    }

    public Funcionario findByLogin(String login) {
        return funcionarioRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    public List<Funcionario> buscarPorNome(String nome) {
        return funcionarioRepository.findByNomeCompletoContainingIgnoreCase(nome);
    }
}