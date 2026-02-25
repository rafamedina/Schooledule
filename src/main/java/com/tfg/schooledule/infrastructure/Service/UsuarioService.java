package com.tfg.schooledule.infrastructure.Service;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.tfg.schooledule.infrastructure.repository.MatriculaRepository matriculaRepository;

    public com.tfg.schooledule.domain.DTO.AlumnoProfileDTO getAlumnoProfile(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        com.tfg.schooledule.domain.entity.Matricula matricula = matriculaRepository
                .findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(usuarioId)
                .orElseThrow(() -> new RuntimeException("Matricula no encontrada"));

        com.tfg.schooledule.domain.entity.Grupo grupo = matricula.getImparticion().getGrupo();

        return com.tfg.schooledule.domain.DTO.AlumnoProfileDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .centroNombre(matricula.getCentro().getNombre())
                .grupoNombre(grupo.getNombre())
                .cursoAcademico(grupo.getCursoAcademico().getNombre())
                .build();
    }


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
