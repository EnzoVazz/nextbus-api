package br.com.fiap.nextbus.repository;

import br.com.fiap.nextbus.model.Idempotencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotenciaRepository extends JpaRepository<Idempotencia, Long> {

    Optional<Idempotencia> findByIdUsuarioAndChave(Long idUsuario, String chave);

    void deleteByIdRotina(Long idRotina);
}
