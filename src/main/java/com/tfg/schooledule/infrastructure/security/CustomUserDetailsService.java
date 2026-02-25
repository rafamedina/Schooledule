package com.tfg.schooledule.infrastructure.security;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository userRepository;

    public CustomUserDetailsService(UsuarioRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = userRepository.findUsuarioByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getActivo(),
                true,
                true,
                true,
                usuario.getRoles().stream()
                        .map(rol -> new org.springframework.security.core.authority.SimpleGrantedAuthority(rol.getNombre()))
                        .collect(Collectors.toSet()));
    }
}
