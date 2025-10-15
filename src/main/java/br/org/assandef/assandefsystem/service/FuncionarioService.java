package br.org.assandef.assandefsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;

@Service
@Transactional
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    public Optional<Funcionario> buscarPorId(Integer id) {
        return funcionarioRepository.findById(id);
    }

    public Optional<Funcionario> buscarPorLogin(String login) {
        return funcionarioRepository.findByLogin(login);
    }

    public List<Funcionario> buscarPorNome(String nome) {
        return funcionarioRepository.findByNomeCompletoContainingIgnoreCase(nome);
    }

    public List<Funcionario> buscarPorHierarquia(Integer hierarquia) {
        return funcionarioRepository.findByHierarquia(hierarquia);
    }

    public Funcionario salvar(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    public Funcionario atualizar(Integer id, Funcionario funcionarioAtualizado) {
        return funcionarioRepository.findById(id)
            .map(funcionario -> {
                funcionario.setNomeCompleto(funcionarioAtualizado.getNomeCompleto());
                funcionario.setLogin(funcionarioAtualizado.getLogin());
                if (funcionarioAtualizado.getSenhaHash() != null && !funcionarioAtualizado.getSenhaHash().isEmpty()) {
                    funcionario.setSenhaHash(funcionarioAtualizado.getSenhaHash());
                }
                funcionario.setHierarquia(funcionarioAtualizado.getHierarquia());
                return funcionarioRepository.save(funcionario);
            })
            .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com id: " + id));
    }

    public void deletar(Integer id) {
        funcionarioRepository.deleteById(id);
    }

    public boolean existe(Integer id) {
        return funcionarioRepository.existsById(id);
    }
}
