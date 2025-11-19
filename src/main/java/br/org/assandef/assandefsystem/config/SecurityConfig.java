package br.org.assandef.assandefsystem.config;

import br.org.assandef.assandefsystem.security.AuthService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApplicationContext applicationContext;

    public SecurityConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== MODO TESTE: CSRF DESABILITADO PARA FACILITAR TESTES ==========
                // TODO: REVERTER APÓS TESTES -> remova a linha abaixo para reativar CSRF
                .csrf(csrf -> csrf.disable())
                // ========== FIM MODO TESTE CSRF ==========

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/login", "/doadores/newdonation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/doadores/salvar").permitAll()

                        // ========== MODO TESTE: TODOS OS ACESSOS LIBERADOS ==========
                        // TODO: REVERTER APÓS TESTES - Restaurar restrições de hierarquia

                        // TEMPORARIAMENTE LIBERADO - /funcionarios/** (era somente hierarquia 1)
                        .requestMatchers("/funcionarios/**").authenticated()

                        // TEMPORARIAMENTE LIBERADO - /almoxarifado/** (era hierarquia 1 ou 3)
                        .requestMatchers("/almoxarifado/**").authenticated()

                        // TEMPORARIAMENTE LIBERADO - /atendimento/** (era hierarquia 1 ou 2)
                        .requestMatchers("/atendimento/**").authenticated()

                        // TEMPORARIAMENTE LIBERADO - /pacientes/** (era hierarquia 1 ou 2)
                        .requestMatchers("/pacientes/**").authenticated()

                        // TEMPORARIAMENTE LIBERADO - /doadores/** (era hierarquia 1 ou 3)
                        .requestMatchers("/doadores/**").authenticated()

                        // ========== FIM DO MODO TESTE ==========

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(86400)
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}