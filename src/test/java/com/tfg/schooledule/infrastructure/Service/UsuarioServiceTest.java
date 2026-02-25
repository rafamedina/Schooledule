package com.tfg.schooledule.infrastructure.Service;

import com.tfg.schooledule.domain.DTO.AlumnoProfileDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    public void testGetAlumnoProfile_Success() {
        Integer usuarioId = 1;
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .username("testuser")
                .nombre("Test")
                .apellidos("User")
                .email("test@example.com")
                .build();

        Centro centro = Centro.builder().nombre("Centro Test").build();
        CursoAcademico curso = CursoAcademico.builder().nombre("2025/2026").build();
        Grupo grupo = Grupo.builder().nombre("DAM2").centro(centro).cursoAcademico(curso).build();
        Imparticion imparticion = Imparticion.builder().grupo(grupo).build();
        Matricula matricula = Matricula.builder()
                .alumno(usuario)
                .imparticion(imparticion)
                .centro(centro)
                .build();

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(matriculaRepository.findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(usuarioId))
                .thenReturn(Optional.of(matricula));

        AlumnoProfileDTO profile = usuarioService.getAlumnoProfile(usuarioId);

        assertNotNull(profile);
        assertEquals("testuser", profile.getUsername());
        assertEquals("Centro Test", profile.getCentroNombre());
        assertEquals("DAM2", profile.getGrupoNombre());
        assertEquals("2025/2026", profile.getCursoAcademico());
    }
}
