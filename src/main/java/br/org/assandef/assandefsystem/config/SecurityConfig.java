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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/login", "/doadores/newdonation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/doadores/salvar").permitAll()

                        // /funcionarios/** -> somente hierarquia 1
                        .requestMatchers("/funcionarios/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasHierarquia(authentication, 1);
                            return new AuthorizationDecision(allowed);
                        })

                        // /almoxarifado/** -> hierarquia 1 ou 3
                        .requestMatchers("/almoxarifado/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1,2,3);
                            return new AuthorizationDecision(allowed);
                        })

                        // /atendimento/** -> hierarquia 1 ou 2
                        .requestMatchers("/atendimento/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 2);
                            return new AuthorizationDecision(allowed);
                        })

                        // /pacientes/** -> hierarquia 1 ou 2
                        .requestMatchers("/pacientes/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 2);
                            return new AuthorizationDecision(allowed);
                        })

                        .requestMatchers(HttpMethod.GET, "/doadores/editar/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })
                        .requestMatchers(HttpMethod.POST, "/doadores/editar/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })
                        .requestMatchers(HttpMethod.GET, "/doadores/deletar/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })

                        // /doadores/** -> hierarquia 1 ou 3
                        .requestMatchers("/doadores/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })

                        // /doadores/** -> hierarquia 1 ou 3
                        .requestMatchers("/funcionarios/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1);
                            return new AuthorizationDecision(allowed);
                        })



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