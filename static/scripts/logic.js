let currentUser = null;

const API_BASE_URL =
  "https://o8dquugs9e.execute-api.us-east-1.amazonaws.com/beta/";

async function fetchProperties(api, method = "GET", body = null) {
  const res = await fetch(API_BASE_URL + api, {
    method,
    headers: {
      "Content-Type": "application/json",
    },
    ...(body && { body: JSON.stringify(body) }),
    authorization: `Bearer ${currentUser.access_token}`,
  });
  return res;
}

const stream = {
  posts: [],
};

// Referencias DOM
const postContent = document.getElementById("postContent");
const charCount = document.getElementById("charCount");
const postButton = document.getElementById("postButton");
const postsStream = document.getElementById("postsStream");
const progressBar = document.getElementById("progressBar");

// Actualizar contador de caracteres y barra de progreso
postContent.addEventListener("input", () => {
  const length = postContent.value.length;
  const remaining = 140 - length;
  charCount.textContent = remaining;

  // Actualizar barra de progreso circular
  const circumference = 87.96;
  const progress = (length / 140) * circumference;
  progressBar.style.strokeDashoffset = circumference - progress;

  // Cambiar colores según el progreso
  if (remaining < 20) {
    charCount.classList.add("text-red-500", "font-bold");
    progressBar.style.stroke = "#ef4444";
  } else if (remaining < 40) {
    charCount.classList.remove("text-red-500", "font-bold");
    charCount.classList.add("text-yellow-500", "font-semibold");
    progressBar.style.stroke = "#f59e0b";
  } else {
    charCount.classList.remove(
      "text-red-500",
      "text-yellow-500",
      "font-bold",
      "font-semibold"
    );
    progressBar.style.stroke = "#3b82f6";
  }

  postButton.disabled = length === 0 || length > 140;
});

// Crear post
postButton.addEventListener("click", () => {
  if (!currentUser) return;
  const content = postContent.value.trim();
  if (content.length === 0 || content.length > 140) return;

  const post = {
    content: content,
    userId: currentUser.profile.sub,
  };

  fetchProperties("posts/", "POST", post)
    .then((res) => res.json())
    .then((data) => {
      console.log("Post creado:", data);
      loadPostsByStream();
    })
    .catch((err) => {
      console.error("Error al crear el post:", err);
    });

  postContent.value = "";
  charCount.textContent = "140";
  progressBar.style.strokeDashoffset = "87.96";
  progressBar.style.stroke = "#3b82f6";
  postButton.disabled = true;
  postContent.focus();
});

function loadPostsByStream() {
  fetchProperties("posts/", "GET")
    .then((res) => res.json())
    .then((data) => {
      stream.posts = data;
      stream.posts.forEach(async (post) => {
        await fetchProperties(`users/${post.userId}`, "GET")
          .then((res) => res.json())
          .then((userData) => {
            post.user = {
              username: "@" + userData.username,
              avatar: "fas fa-user",
            };
          });
      });
      renderStream();
    })
    .catch((err) => {
      console.error("Error al obtener los posts:", err);
    });
}
// Renderizar stream
function renderStream() {
  if (stream.posts.length === 0) {
    postsStream.innerHTML = `
                    <div class="text-center text-gray-400 py-12">
                        <i class="fas fa-feather-alt text-4xl mb-4 text-gray-300"></i>
                        <p class="text-lg">No hay tweets aún</p>
                        <p class="text-sm">¡Sé el primero en compartir algo!</p>
                    </div>
                `;
    return;
  }

  postsStream.innerHTML = stream.posts
    .map(
      (post) => `
                <div class="hover:bg-gray-50 transition-colors p-4 cursor-pointer">
                    <div class="flex items-start space-x-3">
                        <div class="w-12 h-12 gradient-bg rounded-full flex items-center justify-center shadow-md flex-shrink-0">
                            <i class="${post.user.avatar} text-white"></i>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center space-x-2 mb-1">
                                <span class="font-bold text-gray-900">${
                                  post.user.username
                                }</span>
                                <span class="text-gray-500 text-sm">${
                                  post.user.username
                                }</span>
                                <span class="text-gray-500 text-sm">·</span>
                                <span class="text-gray-500 text-sm">${formatTime(
                                  new Date(post.createdAt)
                                )}</span>
                            </div>
                            <p class="text-gray-900 leading-relaxed mb-3">${escapeHtml(
                              post.content
                            )}</p>
                            
                            <!-- Tweet Actions -->
                            <div class="flex items-center justify-between max-w-md text-gray-500">
                                <button class="flex items-center space-x-2 hover:text-blue-500 transition-colors group">
                                    <div class="p-2 rounded-full group-hover:bg-blue-50 transition-colors">
                                        <i class="far fa-comment text-sm"></i>
                                    </div>
                                    <span class="text-sm">0</span>
                                </button>
                                
                                <button class="flex items-center space-x-2 hover:text-green-500 transition-colors group">
                                    <div class="p-2 rounded-full group-hover:bg-green-50 transition-colors">
                                        <i class="fas fa-retweet text-sm"></i>
                                    </div>
                                    <span class="text-sm">0</span>
                                </button>
                                
                                <button class="flex items-center space-x-2 hover:text-red-500 transition-colors group" onclick="toggleLike(${
                                  post.id
                                })">
                                    <div class="p-2 rounded-full group-hover:bg-red-50 transition-colors">
                                        <i class="far fa-heart text-sm" id="heart-${
                                          post.id
                                        }"></i>
                                    </div>
                                    <span class="text-sm" id="likes-${
                                      post.id
                                    }">0</span>
                                </button>
                                
                                <button class="flex items-center space-x-2 hover:text-blue-500 transition-colors group">
                                    <div class="p-2 rounded-full group-hover:bg-blue-50 transition-colors">
                                        <i class="fas fa-share text-sm"></i>
                                    </div>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `
    )
    .join("");
}

// Formatear tiempo
function formatTime(date) {
  const now = new Date();
  const diff = Math.floor((now - date) / 1000);

  if (diff < 60) return "ahora";
  if (diff < 3600) return `${Math.floor(diff / 60)}m`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h`;
  return date.toLocaleDateString("es-ES", { day: "numeric", month: "short" });
}

// Escapar HTML
function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

// Permitir Enter para publicar
postContent.addEventListener("keydown", (e) => {
  if (e.key === "Enter" && e.ctrlKey && !postButton.disabled) {
    postButton.click();
  }
});

export function init(user) {
  currentUser = user;
  console.log("Current User:", currentUser);
  fetchProperties("streams/global", "GET")
    .then((res) => res.json())
    .then((data) => {
      document.getElementById("stream").textContent = data.name;
      document.getElementById(
        "stream-date"
      ).textContent = `Actualizado el ${formatTime(new Date(data.updatedAt))}`;
      loadPostsByStream();
    })
    .catch((err) => {
      console.error("Error al obtener los posts:", err);
    });
}
