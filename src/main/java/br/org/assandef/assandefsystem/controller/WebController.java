package br.org.assandef.assandefsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "acesso/login";
    }

    @GetMapping("/atendimento")
    public String atendimento() {
        return "atendimento/atendimento";
    }

    @GetMapping("/profissional")
    public String pacientes() {
        return "profissional/profissional";
    }

    @GetMapping("/doadores")
    public String doacoes() {
        return "doadores/donation";
    }

    @GetMapping("/funcionarios")
    public String gestaoFuncionarios() {
        return "funcionarios/gestaofuncionarios";
    }
}
