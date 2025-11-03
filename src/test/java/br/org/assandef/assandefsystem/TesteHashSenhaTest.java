package br.org.assandef.assandefsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class TesteHashSenhaTest {

    @Test
    void gerarHashesESenhas() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("\n=== GERANDO HASHES DE SENHA ===\n");

        String senhaAdmin = "admin123";
        String hashAdmin = encoder.encode(senhaAdmin);
        System.out.println("Senha: " + senhaAdmin);
        System.out.println("Hash:  " + hashAdmin);
        System.out.println();

        String senhaComum = "senha123";
        String hashComum = encoder.encode(senhaComum);
        System.out.println("Senha: " + senhaComum);
        System.out.println("Hash:  " + hashComum);
        System.out.println();

        // Verificar hash atual do admin
        String hashAtualAdmin = "$2a$10$N8qN7JxS4xBqBvW6rXWGN.zP1bvY1PvC8qoG8zGx0YM7F6lQlQxQm";
        System.out.println("=== VERIFICANDO HASH EXISTENTE (ANTIGO) ===");
        System.out.println("Hash antigo no banco: " + hashAtualAdmin);
        System.out.println("Corresponde a 'admin123'? " + encoder.matches(senhaAdmin, hashAtualAdmin));
        System.out.println();

        // Verificar novo hash
        String hashNovoAdmin = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi";
        System.out.println("=== VERIFICANDO HASH NOVO ===");
        System.out.println("Hash novo no banco: " + hashNovoAdmin);
        System.out.println("Corresponde a 'admin123'? " + encoder.matches(senhaAdmin, hashNovoAdmin));
        System.out.println();

        String hashNovoSenha123 = "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6";
        System.out.println("Hash para 'senha123': " + hashNovoSenha123);
        System.out.println("Corresponde a 'senha123'? " + encoder.matches(senhaComum, hashNovoSenha123));
        System.out.println();
    }
}

