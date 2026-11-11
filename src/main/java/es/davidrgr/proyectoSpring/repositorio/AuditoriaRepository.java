package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.modelo.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

	@Query(value = "select a from Auditoria a where lower(a.usuario) like lower(concat('%', :q, '%'))",
			countQuery = "select count(a) from Auditoria a where lower(a.usuario) like lower(concat('%', :q, '%'))")
	Page<Auditoria> buscar(@Param("q") String q, Pageable pageable);
}
