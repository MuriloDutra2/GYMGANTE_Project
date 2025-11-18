package br.com.gymgante.gymgante_api.exeception;



import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Esta anotação transforma a classe em um "conselheiro" global
// para todos os @RestControllers.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Método 1: Tratar erros de Login
    
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
    
    String mensagem = ex.getMessage(); // Pega a mensagem do erro

    // --- 1. Tratador de Login ---
    if ("Credenciais inválidas".equals(mensagem)) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(new ErrorResponseDto("Login ou senha inválidos."));
    }
    
    // --- 2. NOVO: Tratador de Erros da Anamnese ---
    if ("Usuário não encontrado.".equals(mensagem) ||
        "Este usuário já possui uma anamnese cadastrada.".equals(mensagem) ||
        "Nenhum plano de treino encontrado para esta combinação específica.".equals(mensagem)) {
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(new ErrorResponseDto(mensagem)); // Retorna a mensagem de erro exata
    }

    // --- 3. Bloco "Pega-Tudo" (Se não for nenhum dos acima) ---
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
            .body(new ErrorResponseDto("Ocorreu um erro inesperado no servidor."));
}
    // Método 2: Tratar erros de dados duplicados (Email/CPF)
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        
        String mensagemErro;
        
        // Verificamos qual restrição foi violada
        if (ex.getMessage().contains("UKspmnyb4dsul95fjmr5kmdmvub")) { // Restrição do E-mail
            mensagemErro = "O e-mail informado já está cadastrado.";
        } else if (ex.getMessage().contains("UK594wib8ansybtilla48x7vdld")) { // Restrição do CPF
            mensagemErro = "O CPF informado já está cadastrado.";
        } else {
            mensagemErro = "Violação de dados. Um campo único já existe.";
        }

        // Retornamos o DTO de erro com a mensagem
        // e o status HTTP 400 BAD REQUEST
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Status 400
                .body(new ErrorResponseDto(mensagemErro));
    }

}
