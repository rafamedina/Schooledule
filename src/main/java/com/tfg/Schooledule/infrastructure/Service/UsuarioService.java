package com.tfg.schooledule.infrastructure.Service;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public boolean comprobarPassword(String email,String password){
       Optional<Usuario> usuario = usuarioRepository.findUsuarioByEmail(email);
       if(usuario.isEmpty()){
        return false;
       }

        return passwordEncoder.matches(password, usuario.get().getPasswordHash());
    }

    public Optional<Usuario> buscarPorCorreo(String email){
        return usuarioRepository.findUsuarioByEmail(email);
    }


}
