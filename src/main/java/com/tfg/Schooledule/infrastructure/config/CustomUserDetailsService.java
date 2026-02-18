package com.tfg.schooledule.infrastructure.config;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. BUSCAR POR EMAIL (Aunque el método se llame loadUserByUsername)
        Usuario usuario = usuarioRepository.findUsuarioByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // 2. MAPEAR ROLES (¡CRUCIAL PARA TU SUCCESS HANDLER!)
        // Asegúrate de añadir "ROLE_" si tu BD no lo tiene
        Collection<GrantedAuthority> autoridades = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre().toUpperCase())) // Ej: ROLE_ADMIN
                .collect(Collectors.toList());

        // 3. DEVOLVER EL OBJETO DE SPRING
        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(), // Aquí debe venir el HASH de la BD ($2a$10$...)
                usuario.getActivo(), // enabled
                true, true, true,
                autoridades
        );
    }
}