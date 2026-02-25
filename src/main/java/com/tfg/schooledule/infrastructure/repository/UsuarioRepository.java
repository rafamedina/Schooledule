package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByEmail(String email);

    Usuario findByEmail(String email);

    Optional<Usuario> findUsuarioByEmail(String correo);
}
