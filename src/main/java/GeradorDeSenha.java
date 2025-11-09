import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorDeSenha {
    public static void main(String[] args) {
        // Gera o hash para a senha "admin"
        String senhaHash = new BCryptPasswordEncoder().encode("admin");
        System.out.println(senhaHash);
    }
}