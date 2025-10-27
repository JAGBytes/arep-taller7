import { userManager } from "./auth.js";
import { init } from "./logic.js";
// Helper para mostrar datos en la UI
async function setUI(user) {
  if (!user) {
    window.location.href = "/pages/home.html";
    return;
  }
  await init(user);
}

// Salir -> redirigir al endpoint de logout de Cognito
export async function signOutRedirect() {
  const clientId = "7mndnr2bt6r298bvog1c0snfml";
  const logoutUri = "https://fronttaller7.duckdns.org/pages/home.html";
  const cognitoDomain =
    "https://us-east-1g6uraa1ik.auth.us-east-1.amazoncognito.com";
  // construye la URL de logout de Cognito
  const url = `${cognitoDomain}/logout?client_id=${clientId}&logout_uri=${encodeURIComponent(
    logoutUri
  )}`;
  window.location.href = url;
}

document.getElementById("logoutBtn").addEventListener("click", async () => {
  console.log("Logging out...");
  await signOutRedirect();
});

// ——— Manejo del callback (cuando Cognito redirige con ?code=...)
async function handleSigninCallbackIfNeeded() {
  try {
    // si la URL contiene code= o state=, procesamos el callback
    const search = window.location.search;
    if (search.includes("code=") || search.includes("state=")) {
      const user = await userManager.signinCallback();
      setUI(user);

      // limpiamos la URL para quitar ?code=... y no procesarlo otra vez
      if (window.history && window.history.replaceState) {
        const cleanUrl = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
      }
      return;
    }

    // si no hay callback, intentamos recuperar usuario desde el store (session/local)
    const existingUser = await userManager.getUser();
    if (existingUser) {
      setUI(existingUser);
    } else {
      setUI(null);
    }
  } catch (err) {
    console.error("Error en handleSigninCallbackIfNeeded:", err);
    // limpiar UI por seguridad
    setUI(null);
  }
}

// Ejecutar al cargar la página
handleSigninCallbackIfNeeded();
