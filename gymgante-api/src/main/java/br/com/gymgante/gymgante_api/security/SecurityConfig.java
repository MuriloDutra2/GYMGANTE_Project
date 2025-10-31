package br.com.gymgante.gymgante_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Diz ao Spring que esta é uma classe de configuração
@EnableWebSecurity // Habilita a segurança web do Spring
public class SecurityConfig {

    // Este é o "Bean" que vamos injetar depois para criptografar
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Estamos dizendo ao Spring para usar o BCrypt
        return new BCryptPasswordEncoder(); 
    }

   
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF (necessário para APIs stateless)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/usuarios/**").permitAll() // Permite tudo em /api/usuarios
                .anyRequest().authenticated() // Exige autenticação para o resto
            );
        
        return http.build();
    }
}