package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final FuncionarioRepository funcionarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/funcionarios")
    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    @GetMapping("/testar-senha")
    public Map<String, Object> testarSenha(@RequestParam String login, @RequestParam String senha) {
        Map<String, Object> result = new HashMap<>();

        Funcionario func = funcionarioRepository.findByLogin(login).orElse(null);

        if (func == null) {
            result.put("erro", "Funcionário não encontrado");
            return result;
        }

        result.put("funcionario", func.getNomeCompleto());
        result.put("login", func.getLogin());
        result.put("hashNoBanco", func.getSenhaHash());
        result.put("senhaTestada", senha);
        result.put("senhaCorreta", passwordEncoder.matches(senha, func.getSenhaHash()));
        result.put("ativo", func.getAtivo());

        return result;
    }

    @GetMapping("/gerar-hash")
    public Map<String, String> gerarHash(@RequestParam String senha) {
        Map<String, String> result = new HashMap<>();
        String hash = passwordEncoder.encode(senha);
        result.put("senha", senha);
        result.put("hash", hash);
        result.put("verificacao", String.valueOf(passwordEncoder.matches(senha, hash)));
        return result;
    }
}

