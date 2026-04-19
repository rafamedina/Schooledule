/**
 * Lógica del modal de calificaciones del profesor.
 * Vanilla JS, sin frameworks. CSRF leído de meta tags (Spring Security).
 */

// ── Estado interno ─────────────────────────────────────────────────────────────
let currentMatriculaId = null;
let currentData = null; // TeacherStudentGradesDTO del servidor

// ── Inicio ─────────────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  wireRosterClicks();
  wireModalButtons();
});

/** Vincula clicks y teclas en cada fila de alumno. */
export function wireRosterClicks() {
  document.querySelectorAll(".roster__item").forEach((row) => {
    row.addEventListener("click", () => openModal(row.dataset.matriculaId));
    row.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        openModal(row.dataset.matriculaId);
      }
    });
  });
}

/** Vincula botones de cierre y guardado del modal. */
function wireModalButtons() {
  const modal = document.getElementById("gradesModal");
  document.getElementById("closeGrades").addEventListener("click", closeModal);
  document
    .getElementById("closeGradesTop")
    .addEventListener("click", closeModal);
  document.getElementById("saveGrades").addEventListener("click", saveGrades);

  // ESC cierra el modal nativo del dialog
  modal.addEventListener("cancel", (e) => {
    e.preventDefault();
    closeModal();
  });
}

/** Abre el modal cargando las notas del alumno via API. */
async function openModal(matriculaId) {
  currentMatriculaId = matriculaId;
  const modal = document.getElementById("gradesModal");
  const body = document.getElementById("gradesBody");

  body.innerHTML =
    '<p style="padding:1rem;color:var(--ink-muted)">Cargando…</p>';
  modal.showModal();
  trapFocus(modal);

  try {
    const resp = await fetch(`/profe/api/matricula/${matriculaId}/notas`, {
      headers: { Accept: "application/json" },
    });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    currentData = await resp.json();
    renderModal(currentData);
  } catch (err) {
    body.innerHTML = `<p style="padding:1rem;color:var(--hot)">Error al cargar las notas: ${err.message}</p>`;
  }
}

/** Renderiza todo el contenido del modal a partir del DTO del servidor. */
function renderModal(data) {
  document.getElementById("gradesModalTitle").textContent = data.alumnoNombre;
  document.getElementById("gradesModalLabel").textContent =
    data.imparticionLabel;
  updateMediaGlobal(data.mediaGlobal);

  const body = document.getElementById("gradesBody");
  body.innerHTML = "";

  data.periodos.forEach((p) => {
    body.appendChild(renderPeriodo(p));
  });
}

/** Construye el DOM de un periodo con su tabla de items evaluables. */
function renderPeriodo(periodo) {
  const section = document.createElement("section");
  section.className =
    "periodo-section" + (periodo.cerrado ? " periodo-section--cerrado" : "");
  section.dataset.periodoId = periodo.id;

  const pesoLabel = periodo.peso != null ? `${periodo.peso}%` : "—";
  const mediaFmt = periodo.media != null ? formatNota(periodo.media) : "—";

  section.innerHTML = `
    <div class="periodo-header">
      <span class="periodo-title">${escHtml(periodo.periodoNombre)}</span>
      <span class="peso-chip">Peso ${pesoLabel}</span>
      ${periodo.cerrado ? '<span class="cerrado-chip">Cerrado</span>' : ""}
      <span class="periodo-media" data-periodo-media="${
        periodo.id
      }">${mediaFmt}</span>
    </div>
    <table class="grade-table" aria-label="Items evaluables de ${escHtml(
      periodo.periodoNombre,
    )}">
      <thead>
        <tr>
          <th scope="col">Ítem</th>
          <th scope="col">Tipo</th>
          <th scope="col">Fecha</th>
          <th scope="col">Nota</th>
          <th scope="col">Comentario</th>
        </tr>
      </thead>
      <tbody id="periodo-body-${periodo.id}"></tbody>
    </table>`;

  const tbody = section.querySelector(`#periodo-body-${periodo.id}`);
  periodo.items.forEach((item) => {
    const tr = renderItemRow(item, periodo);
    tbody.appendChild(tr);
  });

  return section;
}

/** Construye una fila de la tabla para un item evaluable. */
function renderItemRow(item, periodo) {
  const tr = document.createElement("tr");
  const fechaFmt = item.fecha
    ? new Date(item.fecha).toLocaleDateString("es-ES")
    : "—";
  const valorStr = item.valor != null ? item.valor : "";

  tr.innerHTML = `
    <td>${escHtml(item.itemNombre)}</td>
    <td><span class="grade-table__tipo">${escHtml(
      item.tipoActividad,
    )}</span></td>
    <td>${fechaFmt}</td>
    <td>
      <label class="sr-only" for="nota-${item.itemEvaluableId}">
        Nota para ${escHtml(item.itemNombre)} en ${escHtml(
          periodo.periodoNombre,
        )}
      </label>
      <input
        class="grade-input"
        type="number"
        id="nota-${item.itemEvaluableId}"
        name="nota-${item.itemEvaluableId}"
        step="0.01" min="0" max="10"
        data-item-id="${item.itemEvaluableId}"
        data-periodo-id="${periodo.id}"
        data-calificacion-id="${item.calificacionId ?? ""}"
        value="${valorStr}"
        ${periodo.cerrado ? 'disabled aria-disabled="true"' : ""}
        aria-label="Nota para ${escHtml(item.itemNombre)}"/>
    </td>
    <td>
      <label class="sr-only" for="comment-${item.itemEvaluableId}">
        Comentario para ${escHtml(item.itemNombre)}
      </label>
      <input
        class="comment-input"
        type="text"
        id="comment-${item.itemEvaluableId}"
        name="comment-${item.itemEvaluableId}"
        data-item-id="${item.itemEvaluableId}"
        maxlength="1000"
        value="${escHtml(item.comentario ?? "")}"
        ${periodo.cerrado ? 'disabled aria-disabled="true"' : ""}
        placeholder="Comentario opcional"/>
    </td>`;

  // Recalculo reactivo de la media del periodo al editar
  tr.querySelector(".grade-input").addEventListener("input", () => {
    recomputeClientMedias(periodo.id);
  });

  return tr;
}

/**
 * Recalcula la media del periodo en el cliente después de cada cambio de input.
 * El servidor es la fuente autoritativa; esto es solo retroalimentación visual.
 */
function recomputeClientMedias(periodoId) {
  const inputs = document.querySelectorAll(
    `input.grade-input[data-periodo-id="${periodoId}"]`,
  );
  const valores = [];
  inputs.forEach((inp) => {
    const v = parseFloat(inp.value);
    if (!isNaN(v) && inp.value.trim() !== "") valores.push(v);
  });

  const mediaEl = document.querySelector(`[data-periodo-media="${periodoId}"]`);
  if (!mediaEl) return;

  if (valores.length === 0) {
    mediaEl.textContent = "—";
  } else {
    const media = valores.reduce((a, b) => a + b, 0) / valores.length;
    mediaEl.textContent = formatNota(media);
  }
}

/** Envía las notas al servidor y re-renderiza el modal con la respuesta autoritativa. */
async function saveGrades() {
  const btn = document.getElementById("saveGrades");
  btn.disabled = true;
  btn.textContent = "Guardando…";

  const entries = [];
  document
    .querySelectorAll("input.grade-input:not([disabled])")
    .forEach((inp) => {
      const itemId = parseInt(inp.dataset.itemId, 10);
      const valorRaw = inp.value.trim();
      const valor = valorRaw !== "" ? parseFloat(valorRaw) : null;
      const commentEl = document.getElementById(`comment-${itemId}`);
      const comentario = commentEl ? commentEl.value.trim() || null : null;
      entries.push({ itemEvaluableId: itemId, valor, comentario });
    });

  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector(
    'meta[name="_csrf_header"]',
  )?.content;

  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

  try {
    const resp = await fetch(
      `/profe/api/matricula/${currentMatriculaId}/notas`,
      {
        method: "POST",
        headers,
        body: JSON.stringify({
          matriculaId: parseInt(currentMatriculaId, 10),
          entries,
        }),
      },
    );

    if (!resp.ok) {
      const errData = await resp.json().catch(() => ({}));
      throw new Error(
        errData.message || errData.error || `HTTP ${resp.status}`,
      );
    }

    currentData = await resp.json();
    renderModal(currentData); // re-render con datos del servidor
  } catch (err) {
    alert(`Error al guardar: ${err.message}`);
  } finally {
    const saveBtn = document.getElementById("saveGrades");
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.textContent = "Guardar";
    }
  }
}

/** Cierra el modal y devuelve el foco al elemento que lo abrió. */
function closeModal() {
  const modal = document.getElementById("gradesModal");
  modal.close();
  currentMatriculaId = null;
  currentData = null;
  releaseFocus();
}

/** Actualiza el display de media global. */
function updateMediaGlobal(media) {
  const el = document.getElementById("mediaGlobal");
  el.textContent = media != null ? formatNota(media) : "—";
}

// ── Focus trap (accesibilidad modal) ────────────────────────────────────────────
let lastFocused = null;
const FOCUSABLE =
  'button, [href], input:not([disabled]), select, textarea, [tabindex]:not([tabindex="-1"])';

function trapFocus(modal) {
  lastFocused = document.activeElement;
  const focusable = modal.querySelectorAll(FOCUSABLE);
  if (focusable.length) focusable[0].focus();

  modal.addEventListener("keydown", handleFocusTrap);
}

function handleFocusTrap(e) {
  if (e.key !== "Tab") return;
  const modal = document.getElementById("gradesModal");
  const focusable = Array.from(modal.querySelectorAll(FOCUSABLE));
  const first = focusable[0];
  const last = focusable[focusable.length - 1];

  if (e.shiftKey && document.activeElement === first) {
    e.preventDefault();
    last.focus();
  } else if (!e.shiftKey && document.activeElement === last) {
    e.preventDefault();
    first.focus();
  }
}

function releaseFocus() {
  const modal = document.getElementById("gradesModal");
  modal.removeEventListener("keydown", handleFocusTrap);
  if (lastFocused) lastFocused.focus();
}

// ── Utilidades ──────────────────────────────────────────────────────────────────
function formatNota(n) {
  return parseFloat(n).toFixed(2);
}

function escHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
