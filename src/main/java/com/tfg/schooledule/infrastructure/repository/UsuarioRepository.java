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

  Usuario findByEmail(String email);

  Optional<Usuario> findUsuarioByEmail(String correo);
}
