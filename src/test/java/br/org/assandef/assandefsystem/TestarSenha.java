package br.org.assandef.assandefsystem;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestarSenha {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Hash que está no banco
        String hashNoBanco = "$2a$10$Xl4SbFQplatjAAP.nU8eNeLv.IvFXEbv2SLEGr1KNGVLD7cD/VtGO";
        
        // Testar senhas
        System.out.println("Testando hash: " + hashNoBanco);
        System.out.println();
        System.out.println("admin123: " + encoder.matches("admin123", hashNoBanco));
        System.out.println("senha123: " + encoder.matches("senha123", hashNoBanco));
        System.out.println("admin: " + encoder.matches("admin", hashNoBanco));
        System.out.println("123: " + encoder.matches("123", hashNoBanco));
        
        // Gerar novo hash
        System.out.println();
        System.out.println("Novo hash para 'admin123': " + encoder.encode("admin123"));
    }
}

