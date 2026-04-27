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

    @GetMapping("/profissional")
    public String pacientes() {
        return "profissional/profissional";
    }

    @GetMapping("/sobre")
    public String sobre() {
        return "home/sobre"; // era "sobre", agora aponta para a subpasta
    }
}
