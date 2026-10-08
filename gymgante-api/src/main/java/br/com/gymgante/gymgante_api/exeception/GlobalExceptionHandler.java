package br.com.gymgante.gymgante_api.exeception;



import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
    if ("Usuário não encontrado".equals(mensagem) ||
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
    @ExceptionHandler(DadoDuplicadoException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicado(DadoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto(ex.getMessage()));
    }

    // Método 2: Tratar erros de dados duplicados (Email/CPF) - rede de segurança para corridas
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        
        String detalhe = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        String mensagemErro;

        if (detalhe.contains("(email)") || detalhe.contains("email_key") || detalhe.contains("uk_email")) {
            mensagemErro = "O e-mail informado já está cadastrado.";
        } else if (detalhe.contains("(cpf)") || detalhe.contains("cpf_key") || detalhe.contains("uk_cpf")) {
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

    // Método 3: Tratar erros de validação (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Erro de validação: " + mensagem));
    }

}
