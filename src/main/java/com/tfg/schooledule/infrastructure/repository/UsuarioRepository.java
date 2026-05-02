package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  Optional<Usuario> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmailAndIdNot(String email, Integer id);

  boolean existsByUsernameAndIdNot(String username, Integer id);

  List<Usuario> findAllByOrderByApellidosAscNombreAsc();

  @org.springframework.data.jpa.repository.Query(
      "SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ALUMNO' ORDER BY u.apellidos ASC, u.nombre ASC")
  List<Usuario> findAllAlumnosOrdenados();

  @org.springframework.data.jpa.repository.Query(
      "SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'PROFESOR' ORDER BY u.apellidos ASC, u.nombre ASC")
  List<Usuario> findAllProfesoresOrdenados();

  Usuario findByEmail(String email);

  Optional<Usuario> findUsuarioByEmail(String correo);
}
