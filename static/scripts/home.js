import { userManager } from "./auth.js";

document.getElementById("loginBtn").addEventListener("click", async () => {
  try {
    console.log("Iniciando signinRedirect...");
    await userManager.signinRedirect();
  } catch (e) {
    console.error("Error al iniciar signinRedirect:", e);
    alert("Error iniciando el login. Mira la consola.");
  }
});