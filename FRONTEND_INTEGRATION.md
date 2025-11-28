# 🔗 Intégration Frontend - Backend Spring Boot

## ✅ Configuration pour le Frontend React

### Variables d'environnement

Créez un fichier `.env` dans le dossier `client/` :

```bash
# API Backend Spring Boot
VITE_API_URL=http://localhost:8080/api

# Note : Les endpoints sont disponibles sur :
# - /api/auth/* (alias pour compatibilité)
# - /api/v1/auth/* (endpoints officiels)
```

**Port par défaut** : Le backend Spring Boot tourne sur le port **8080** (pas 4000 comme Node.js).

---

## 📡 Endpoints d'authentification disponibles

### Authentification

| Endpoint | Méthode | Description | Corps de requête |
|----------|---------|-------------|------------------|
| `/api/auth/register` | POST | Inscription | `{ email, password, firstName, lastName }` |
| `/api/auth/login` | POST | Connexion | `{ email, password }` |
| `/api/auth/refresh` | POST | Rafraîchir le token | Aucun (refresh token en cookie) |
| `/api/auth/logout` | POST | Déconnexion | Aucun |
| `/api/auth/me` | GET | Utilisateur courant | Aucun (token en header) |

### Utilitaires

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/csrf-token` | GET | Token CSRF (retourne "disabled") |

---

## 🔐 Format des réponses

### Réponse d'authentification (Login/Register/Refresh)

```typescript
{
  "accessToken": "eyJhbGc...",      // JWT access token (5 min)
  "refreshToken": "eyJhbGc...",     // JWT refresh token (7 jours, aussi en cookie)
  "tokenType": "Bearer",
  "expiresIn": 300,                 // Secondes (5 minutes)
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "avatar": null,
    "role": "OWNER",                // OWNER | VET | ADMIN
    "emailVerified": false,
    "createdAt": "2025-01-28T10:00:00Z",
    "updatedAt": "2025-01-28T10:00:00Z"
  }
}
```

### Champs utilisateur

| Champ | Type | Description |
|-------|------|-------------|
| `id` | string (UUID) | Identifiant unique |
| `email` | string | Email de l'utilisateur |
| `firstName` | string \| null | Prénom |
| `lastName` | string \| null | Nom |
| `avatar` | string \| null | URL de l'avatar |
| `role` | "OWNER" \| "VET" \| "ADMIN" | Rôle utilisateur |
| `emailVerified` | boolean | Email vérifié |
| `createdAt` | string (ISO 8601) | Date de création |
| `updatedAt` | string (ISO 8601) | Dernière modification |

---

## 🔒 Gestion des tokens

### Access Token
- **Durée de vie** : 5 minutes (standard bancaire)
- **Stockage** : Mémoire (variable React)
- **Envoi** : Header `Authorization: Bearer {token}`
- **Également en cookie HTTP-only** (optionnel)

### Refresh Token
- **Durée de vie** : 7 jours
- **Stockage** : Cookie HTTP-only (sécurisé)
- **Nom du cookie** : `refresh_token`
- **Envoi** : Automatique via cookie

### Flux d'authentification

```
1. Login/Register
   → Backend retourne accessToken + user
   → Refresh token envoyé en cookie HTTP-only

2. Requêtes API
   → Frontend envoie: Authorization: Bearer {accessToken}
   → withCredentials: true (pour envoyer les cookies)

3. Token expiré (401)
   → Frontend appelle /api/auth/refresh
   → Backend lit refresh_token depuis cookie
   → Retourne nouveau accessToken

4. Refresh échoue
   → Utilisateur déconnecté
```

---

## 🛡️ Validation des mots de passe

**Exigences backend (niveau bancaire)** :
- **12 caractères minimum** (au lieu de 8)
- Au moins 1 minuscule
- Au moins 1 majuscule
- Au moins 1 chiffre
- Au moins 1 caractère spécial : `@$!%*?&#^()_+-=[]{}etc.`

**Mise à jour requise dans le frontend** :

```typescript
// client/src/pages/RegisterPage.tsx
password: z.string()
  .min(12, "Le mot de passe doit contenir au minimum 12 caractères")
  .regex(
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^()_+\-=\[\]{};':"\\|,.<>\/`~])/,
    "Le mot de passe doit contenir : 1 minuscule, 1 majuscule, 1 chiffre et 1 caractère spécial"
  ),
```

---

## 🍪 Configuration des cookies

### Cookies envoyés par le backend

| Cookie | Valeur | HttpOnly | Secure | SameSite | Path |
|--------|--------|----------|--------|----------|------|
| `access_token` | JWT | ✅ | Dev: ❌<br>Prod: ✅ | Strict | `/` |
| `refresh_token` | JWT | ✅ | Dev: ❌<br>Prod: ✅ | Strict | `/api/v1/auth` |

**Important** : En développement, `Secure=false` pour permettre HTTP. En production, `Secure=true` (HTTPS obligatoire).

---

## 🌐 CORS

Le backend autorise :
- **Origins** : `http://localhost:5173` (frontend Vite)
- **Methods** : GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Headers** : Content-Type, Authorization, X-Requested-With, Accept, X-CSRF-Token
- **Credentials** : true (cookies autorisés)

### Configuration axios requise

```typescript
// client/src/lib/api.ts
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,  // ✅ OBLIGATOIRE pour les cookies
  headers: {
    'Content-Type': 'application/json'
  }
});
```

---

## 🔴 Différences avec l'API Node.js

| Aspect | Node.js (ancien) | Spring Boot (nouveau) |
|--------|------------------|----------------------|
| **Port** | 4000 | 8080 |
| **Base URL** | `/api` | `/api` (alias) ou `/api/v1` |
| **Champs réponse** | snake_case | camelCase |
| **Password min** | 8 chars | 12 chars + complexité |
| **Access token** | 15 min | 5 min (bancaire) |
| **CSRF** | Activé | Désactivé (JWT stateless) |

---

## ✅ Checklist d'intégration

- [ ] Mettre à jour `VITE_API_URL=http://localhost:8080/api` dans `.env`
- [ ] Vérifier `withCredentials: true` dans axios
- [ ] Mettre à jour validation password (12 chars + complexité)
- [ ] Tester inscription avec mot de passe complexe
- [ ] Tester login et stockage des tokens
- [ ] Tester refresh automatique sur 401
- [ ] Vérifier que les cookies sont bien envoyés

---

## 🐛 Debugging

### Problème : 401 Unauthorized sur toutes les requêtes

**Cause** : Access token non envoyé ou expiré

**Solution** :
1. Vérifier que `Authorization: Bearer {token}` est dans les headers
2. Vérifier que le token n'est pas expiré (5 min max)
3. Tester `/api/auth/refresh` pour obtenir un nouveau token

### Problème : CORS errors

**Cause** : Credentials non envoyés ou origin non autorisé

**Solution** :
1. Vérifier `withCredentials: true` dans axios
2. Vérifier que l'origin est `http://localhost:5173`
3. Check la console pour les erreurs CORS détaillées

### Problème : 400 Bad Request sur /register

**Cause** : Mot de passe ne respecte pas les exigences

**Solution** :
1. Vérifier que le password a 12+ caractères
2. Vérifier la présence de : minuscule, majuscule, chiffre, spécial
3. Exemple valide : `MyP@ssw0rd123`

---

## 📞 Support

En cas de problème, vérifier :
1. Les logs backend : `mvn spring-boot:run` (affiche les erreurs détaillées)
2. Les logs frontend : Console navigateur (F12)
3. Les requêtes réseau : Onglet Network (F12)
4. La documentation Swagger : `http://localhost:8080/swagger-ui.html` (si activé)

---

**Version** : Spring Boot 3.4.1 | Spring Security 6.4
**Dernière mise à jour** : 28 janvier 2025
