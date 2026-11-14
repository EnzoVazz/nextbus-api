package br.com.fiap.nextbus.exception;

import br.com.fiap.nextbus.dto.ErroResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        List<String> mensagens = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ErroResponse(400, "validacao", mensagens));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> credenciais(BadCredentialsException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "nao_autenticado", "Email ou senha inválidos.");
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> emailDuplicado(EmailJaCadastradoException ex) {
        return resposta(HttpStatus.CONFLICT, "email_cadastrado", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "conflito", "Os dados conflitam com uma regra já gravada no banco.");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> cabecalho(MissingRequestHeaderException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "validacao", "Informe o cabeçalho " + ex.getHeaderName() + ".");
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> requisicao(RequisicaoInvalidaException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "validacao", ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametro(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "validacao", "Informe o parâmetro " + ex.getParameterName() + ".");
    }

    @ExceptionHandler({RotinaNaoEncontradaException.class, NaoEncontradoException.class})
    public ResponseEntity<ErroResponse> naoEncontrado(RuntimeException ex) {
        return resposta(HttpStatus.NOT_FOUND, "nao_encontrada", ex.getMessage());
    }

    @ExceptionHandler({RotinaDuplicadaException.class, RotinaComAvisoException.class, IdempotenciaDivergenteException.class})
    public ResponseEntity<ErroResponse> rotinaConflito(RuntimeException ex) {
        return resposta(HttpStatus.CONFLICT, "conflito", ex.getMessage());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), erro, List.of(mensagem)));
    }
}
