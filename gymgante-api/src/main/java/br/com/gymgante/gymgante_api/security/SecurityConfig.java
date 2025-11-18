package br.com.gymgante.gymgante_api.security;
import java.util.Arrays;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.security.config.Customizer.withDefaults;

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
        .cors(withDefaults()) // <-- 1. APLICA a configuração CORS global que criamos
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // 2. LIBERA os endpoints de anamnese, além dos de usuário
            .requestMatchers("/api/usuarios/**", "/api/anamnese/**").permitAll() 
            .anyRequest().authenticated()
        );

    return http.build();
}

    // Este Bean define a configuração CORS global
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Permite requisições de qualquer origem
    configuration.setAllowedOrigins(Arrays.asList("*"));
    // Permite todos os métodos HTTP (GET, POST, PUT, OPTIONS, etc)
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    // Permite todos os cabeçalhos (headers)
    configuration.setAllowedHeaders(Arrays.asList("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // Aplica esta configuração a TODOS os caminhos da nossa API
    source.registerCorsConfiguration("/**", configuration); 
    return source;
}
}