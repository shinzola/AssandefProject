package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listar(Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("erro") String erro) {
        List<Funcionario> funcionarios = funcionarioService.findAll();
        model.addAttribute("funcionarios", funcionarios);
        model.addAttribute("funcionario", new Funcionario());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "funcionarios/gestaofuncionarios";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Funcionario funcionario,
                         @RequestParam(required = false) String senhaPlain,
                         RedirectAttributes ra) {
        try {
            boolean isNew = (funcionario.getIdFuncionario() == null);

            // Verifica se o login já existe (exceto se for edição do mesmo funcionário)
            try {
                Funcionario existente = funcionarioService.findByLogin(funcionario.getLogin());
                if (existente != null && !existente.getIdFuncionario().equals(funcionario.getIdFuncionario())) {
                    ra.addFlashAttribute("erro", "Já existe um funcionário com este login.");
                    return "redirect:/funcionarios";
                }
            } catch (RuntimeException e) {
                // Login não encontrado, pode continuar
            }

            // Validar hierarquia (1 a 5)
            if (funcionario.getHierarquia() == null || funcionario.getHierarquia() < 1 || funcionario.getHierarquia() > 5) {
                ra.addFlashAttribute("erro", "Hierarquia deve estar entre 1 e 5.");
                return "redirect:/funcionarios";
            }

            // Hash da senha
            if (isNew) {
                // Novo funcionário: senha obrigatória
                if (senhaPlain == null || senhaPlain.isBlank()) {
                    ra.addFlashAttribute("erro", "Senha é obrigatória para novo funcionário.");
                    return "redirect:/funcionarios";
                }
                if (senhaPlain.length() < 6) {
                    ra.addFlashAttribute("erro", "Senha deve ter no mínimo 6 caracteres.");
                    return "redirect:/funcionarios";
                }
                funcionario.setSenhaHash(passwordEncoder.encode(senhaPlain));
            } else {
                // Edição: só atualiza senha se foi informada
                if (senhaPlain != null && !senhaPlain.isBlank()) {
                    if (senhaPlain.length() < 6) {
                        ra.addFlashAttribute("erro", "Senha deve ter no mínimo 6 caracteres.");
                        return "redirect:/funcionarios";
                    }
                    funcionario.setSenhaHash(passwordEncoder.encode(senhaPlain));
                } else {
                    // Mantém a senha atual
                    Funcionario atual = funcionarioService.findById(funcionario.getIdFuncionario());
                    funcionario.setSenhaHash(atual.getSenhaHash());
                }
            }

            funcionarioService.save(funcionario);
            ra.addFlashAttribute("msg", isNew
                    ? "Funcionário cadastrado com sucesso!"
                    : "Funcionário atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar funcionário: " + e.getMessage());
        }
        return "redirect:/funcionarios";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            funcionarioService.deleteById(id);
            ra.addFlashAttribute("msg", "Funcionário excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir funcionário. Pode haver atendimentos ou solicitações vinculados.");
        }
        return "redirect:/funcionarios";
    }

    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Funcionario> obter(@PathVariable("id") Integer id) {
        try {
            Funcionario funcionario = funcionarioService.findById(id);
            // Não enviar hash da senha para o front-end
            funcionario.setSenhaHash(null);
            return ResponseEntity.ok(funcionario);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}