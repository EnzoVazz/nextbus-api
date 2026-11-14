package br.com.fiap.nextbus.repository;

import br.com.fiap.nextbus.model.Rotina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RotinaRepository extends JpaRepository<Rotina, Long> {

    Page<Rotina> findByUsuarioId(Long usuarioId, Pageable pageable);

    @Query("select r from Rotina r where r.ativa = true")
    List<Rotina> findAtivas();

    Optional<Rotina> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
            select count(r) > 0 from Rotina r
            where r.usuario.id = :usuarioId
              and r.codigoLinha = :codigoLinha
              and r.codigoParada = :codigoParada
              and r.horarioEmbarque = :horario
              and r.ativa = true
            """)
    boolean existeAtiva(@Param("usuarioId") Long usuarioId,
                        @Param("codigoLinha") String codigoLinha,
                        @Param("codigoParada") String codigoParada,
                        @Param("horario") String horario);

    @Query("""
            select count(r) > 0 from Rotina r
            where r.usuario.id = :usuarioId
              and r.codigoLinha = :codigoLinha
              and r.codigoParada = :codigoParada
              and r.horarioEmbarque = :horario
              and r.ativa = true
              and r.id <> :id
            """)
    boolean existeOutraAtiva(@Param("usuarioId") Long usuarioId,
                             @Param("codigoLinha") String codigoLinha,
                             @Param("codigoParada") String codigoParada,
                             @Param("horario") String horario,
                             @Param("id") Long id);
}
