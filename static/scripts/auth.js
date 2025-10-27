import {
  UserManager,
  WebStorageStateStore,
} from "https://cdn.jsdelivr.net/npm/oidc-client-ts@2/+esm";

// CONFIGURA con tus valores reales
const cognitoAuthConfig = {
  authority: "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_g6UrAa1iK",
  client_id: "7mndnr2bt6r298bvog1c0snfml",
  redirect_uri: "https://fronttaller7.duckdns.org", // debe coincidir EXACTO con lo configurado en Cognito
  response_type: "code",
  scope: "openid email",
  // opcional: mantener estado en sessionStorage/localStorage
  userStore: new WebStorageStateStore({ store: window.localStorage }),
};

export const userManager = new UserManager(cognitoAuthConfig);
