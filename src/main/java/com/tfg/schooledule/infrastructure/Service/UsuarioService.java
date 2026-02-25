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

    @Autowired
    private com.tfg.schooledule.infrastructure.repository.CalificacionRepository calificacionRepository;

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

    public com.tfg.schooledule.domain.DTO.GradeDashboardDTO getStudentGrades(Integer usuarioId, Integer periodoId) {
        java.util.List<com.tfg.schooledule.domain.entity.Calificacion> calificaciones = calificacionRepository
                .findByAlumnoIdAndPeriodoId(usuarioId, periodoId);

        if (calificaciones.isEmpty()) {
            return com.tfg.schooledule.domain.DTO.GradeDashboardDTO.builder()
                    .gradesByModulo(new java.util.HashMap<>())
                    .build();
        }

        String periodoNombre = calificaciones.get(0).getItemEvaluable().getPeriodoEvaluacion().getNombre();

        java.util.Map<String, java.util.List<com.tfg.schooledule.domain.DTO.GradeDTO>> gradesByModulo = new java.util.HashMap<>();

        for (com.tfg.schooledule.domain.entity.Calificacion calif : calificaciones) {
            String moduloNombre = calif.getMatricula().getImparticion().getModulo().getNombre();

            com.tfg.schooledule.domain.DTO.GradeDTO gradeDTO = com.tfg.schooledule.domain.DTO.GradeDTO.builder()
                    .itemNombre(calif.getItemEvaluable().getNombre())
                    .valor(calif.getValor())
                    .comentario(calif.getComentario())
                    .fecha(calif.getItemEvaluable().getFecha())
                    .tipoActividad(calif.getItemEvaluable().getTipo().name())
                    .build();

            gradesByModulo.computeIfAbsent(moduloNombre, k -> new java.util.ArrayList<>()).add(gradeDTO);
        }

        return com.tfg.schooledule.domain.DTO.GradeDashboardDTO.builder()
                .periodoNombre(periodoNombre)
                .gradesByModulo(gradesByModulo)
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

    public Optional<Usuario> buscarPorNombreUsuario(String username) {
        return usuarioRepository.findByUsername(username);
    }


}
