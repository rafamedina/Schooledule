
let formulario = document.getElementById("formulario");
let email = document.getElementById("email");
let password = document.getElementById("password");
let btnRegistro = document.getElementById("btnRegistro");
var toast = document.getElementById("toast")

fetch('/killSession')

formulario.addEventListener('submit',(e)=> {
    e.preventDefault();

    var emailMetido = email.value.trim()
    var passwordMetido = password.value.trim()

    fetch('/loginSession', {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: emailMetido,
            password: passwordMetido
        })
    })
        .then(async (response) => {
            // 1. Leemos la respuesta del servidor (sea éxito o error)
            const textoRespuesta = await response.text();

            // 2. Comprobamos si el status es 200-299
            if (response.ok) {
                // Intentamos convertirlo a JSON por si acaso, si no, devolvemos texto
                try {
                    return JSON.parse(textoRespuesta);
                } catch (err) {
                    return textoRespuesta; // Era texto plano ("Inicio de sesión exitoso")
                }
            } else {
                // 3. SI FALLA (401, 404):
                // Lanzamos un error con el texto QUE ENVÍA EL SERVIDOR ("Contraseña incorrecta")
                // Si el servidor no envió texto, usamos el statusText genérico
                throw new Error(textoRespuesta || response.statusText);
            }
        })
        .then((data) => {
            // 1. Limpiamos el string de roles (ej: "-ADMIN-PROFESOR" -> ["ADMIN", "PROFESOR"])
            // El filter(Boolean) elimina los strings vacíos que deja el split
            const listaRoles = data.roles.split("-").filter(Boolean);

            // 2. Lógica de decisión
            if (listaRoles.length > 1) {
                // --- CASO A: Varios roles -> Abrimos el <dialog> ---
                abrirDialogoRoles(listaRoles);
            } else if (listaRoles.length === 1) {
                // --- CASO B: Un solo rol -> Entramos directo ---
                seleccionarRol(listaRoles[0]);
            } else {
                mostrarToast("Error: Usuario sin roles asignados");
            }
        })



        .catch((error) => {

                // --- ERROR ---
                console.error("Fallo en login:", error);

                // 4. Usamos error.message, que contiene el texto que lanzamos en el paso 3
                toast.textContent = error.message;

                toast.style.display = "flex";
                toast.style.backgroundColor = "red"; // Opcional: Para que se vea que es error

                setTimeout(() => {
                    toast.style.display = "none";
                }, 3000);
            });
});

function abrirDialogoRoles(roles) {
    const dialog = document.getElementById("dialogoRoles");
    const contenedor = document.getElementById("contenedorBotones");

    // Limpiamos botones viejos
    contenedor.innerHTML = "";

    // Generamos botones dinámicamente
    roles.forEach(rol => {
        const btn = document.createElement("button");
        btn.textContent = rol; // Texto del botón: ADMIN, ALUMNO, etc.

        // Al hacer click, cerramos el dialog y enviamos el rol
        btn.onclick = () => {
            dialog.close(); // Método nativo para cerrar
            seleccionarRol(rol);
        };

        contenedor.appendChild(btn);
    });

    // ¡MAGIA! showModal() abre el dialog y bloquea el fondo
    dialog.showModal();
}

function seleccionarRol(rolElegido) {
    console.log("Rol elegido:", rolElegido);

    // Fetch para guardar el rol en sesión y redirigir
    fetch('/control', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rol: rolElegido })
    })
        .then(response => {
            if (response.ok) {
                return response.json(); // Esperamos el JSON con la URL { "redirectUrl": "..." }
            } else {
                throw new Error("Error al establecer el rol");
            }
        })
        .then(data => {
            // AQUÍ OCURRE LA MAGIA: El servidor nos dice a dónde ir
            console.log("Redirigiendo a:", data.redirectUrl);
            window.location.href = data.redirectUrl;
        })
        .catch(error => {
            console.error(error);
            alert("Hubo un problema al seleccionar el perfil.");
        });

}