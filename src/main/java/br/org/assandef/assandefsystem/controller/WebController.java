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

    @GetMapping("/cadastro")
    public String cadastro() {
        return "acesso/cadastro";
    }

    @GetMapping("/almoxarifado")
    public String almoxarifado() {
        return "almoxarifado/almoxarifado";
    }

    @GetMapping("/atendimento")
    public String atendimento() {
        return "atendimento/atendimento";
    }

    @GetMapping("/pacientes")
    public String pacientes() {
        return "pacientes";
    }

    @GetMapping("/materiais")
    public String materiais() {
        return "materiais";
    }
}
