package com.tfg.schooledule.infrastructure.Controller;

import com.tfg.schooledule.domain.DTO.LoginRequest;
import com.tfg.schooledule.domain.DTO.UsuarioDTO;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping()
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String showLoginForm() {
        // Esto busca un archivo llamado "login.html" dentro de /templates
        return "login";
    }

    @PostMapping("/loginSession")
    @ResponseBody
    public ResponseEntity<UsuarioDTO> inicioSesion(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            if (usuarioService.comprobarPassword(loginRequest.getEmail(), loginRequest.getPassword())) {

                String roles = usuarioOpt.get().getRoles().stream()
                        .map(Rol::getNombre)
                        .collect(Collectors.joining("-"));

                UsuarioDTO usuarioInfo = new UsuarioDTO(usuarioOpt.get().getId(),
                        usuarioOpt.get().getUsername(), usuarioOpt.get().getNombre(), usuarioOpt.get().getApellidos(), usuarioOpt.get().getEmail(),
                        usuarioOpt.get().getActivo(), usuarioOpt.get().getFechaRegistro(), roles);

                session.setAttribute("usuarioLogueado", usuarioInfo);

                return ResponseEntity.ok(usuarioInfo);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();        }
    }


    @RequestMapping("/killSession" )
    public String matarSesion( HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/control")
    @ResponseBody
    public ResponseEntity<Map<String, String>> establecerRolYRedirigir(@RequestBody Map<String, String> payload, HttpSession session) {

        // 1. Recuperamos el usuario de la sesión
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Obtenemos el rol que viene del fetch
        String rolSeleccionado = payload.get("rol");

        // 3. Actualizamos el usuario en sesión (Sobre-escribimos el rol activo)
        usuario.setRoles(rolSeleccionado);
        session.setAttribute("usuarioLogueado", usuario);

        // 4. Decidimos a dónde mandar al usuario según el rol
        String urlDestino = "/home"; // Por defecto

        if ("ROLE_ADMIN".equalsIgnoreCase(rolSeleccionado)) {
            urlDestino = "/admin/dashboard"; // Cambia por tus rutas reales
        } else if ("ROLE_PROFESOR".equalsIgnoreCase(rolSeleccionado)) {
            urlDestino = "/profe/dashboard";
        } else if ("ROLE_ALUMNO".equalsIgnoreCase(rolSeleccionado)) {
            urlDestino = "/alumno/dashboard";
        }

        // 5. Devolvemos la URL al JavaScript en formato JSON
        return ResponseEntity.ok(Collections.singletonMap("redirectUrl", urlDestino));
    }

}
