package br.com.fiap.nextbus.repository;

import br.com.fiap.nextbus.model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    Optional<Aviso> findByRotinaIdAndDataAviso(Long rotinaId, LocalDate dataAviso);

    @Query("""
            select a from Aviso a
            where a.rotina.usuario.id = :usuarioId
              and a.dataAviso = :data
            order by a.id
            """)
    List<Aviso> findDoUsuarioNoDia(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data);
}
