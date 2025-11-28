# 📊 Données de Test - Pet Care API

Ce dossier contient les données de test pour l'application Pet Care.

## 🛡️ Sécurité

Les données de test sont **uniquement chargées** dans les environnements **dev** et **test**.

- ✅ **Dev/Test** : Données chargées automatiquement
- ❌ **Production** : Données **JAMAIS** chargées (protection via `contexts: prod`)

## 📁 Fichiers

### `clinics.csv`
**10 cliniques vétérinaires** réparties dans toute la France :

| Ville | Clinique | Horaires |
|-------|----------|----------|
| Paris (75001) | Clinique Vétérinaire du Centre | Lun-Ven: 9h-19h, Sam: 9h-17h |
| Paris (75005) | Cabinet Vétérinaire Saint-Germain | Lun-Ven: 8h30-18h30, Sam: 10h-16h |
| Paris (75008) | Clinique Vétérinaire des Animaux | 24h/24 (urgences) |
| Lyon (69003) | VetoLyon - Clinique Part-Dieu | Lun-Ven: 9h-19h, Sam: 9h-13h |
| Talence (33400) | Clinique Vétérinaire Bordeaux Sud | Lun-Ven: 8h-20h, Sam: 9h-18h |
| Nice (06000) | Cabinet Vétérinaire Nice Riviera | Lun-Sam: 9h-18h |
| Toulouse (31000) | Clinique Vétérinaire Toulouse Capitole | Lun-Ven: 9h-19h |
| Marseille (13002) | Clinique Vétérinaire Marseille Vieux-Port | Lun-Dim: 8h-22h (urgences) |
| Nantes (44000) | VetoNantes - Clinique de l'Erdre | Lun-Ven: 9h-19h, Sam: 9h-17h |
| Strasbourg (67000) | Clinique Vétérinaire Strasbourg Europe | Lun-Ven: 8h30-18h30 |

### `vets.csv`
**20 vétérinaires** avec spécialités variées :

#### Spécialités disponibles :
- 🩺 **Médecine générale** (Sophie Martin, Isabelle Michel, Julien Rodriguez, Vincent Richard)
- 🔬 **Chirurgie** (Pierre Dubois, Sébastien Gonzalez)
- 💊 **Dermatologie** (Marie Lefebvre, Nathalie Robert)
- ❤️ **Cardiologie** (Thomas Rousseau)
- 🚑 **Urgences et soins intensifs** (Émilie Bernard, Aurélie Lopez)
- 🐰 **NAC - Nouveaux Animaux de Compagnie** (Alexandre Moreau)
- 🐱 **Médecine féline** (Camille Petit)
- 🦴 **Orthopédie** (Nicolas Durand)
- 🦷 **Dentisterie** (Julie Laurent)
- 👁️ **Ophtalmologie** (François Simon)
- 🐾 **Reproduction** (Laurent Garcia)
- 🧠 **Comportementalisme** (Céline Martinez)
- 📷 **Imagerie médicale** (Caroline Sanchez)
- 🥗 **Nutrition** (David Perez)

## 🔄 Chargement des données

### Automatique (au démarrage de l'application)

Les données sont chargées automatiquement par **Liquibase** au premier démarrage :

```yaml
spring:
  liquibase:
    contexts: dev,test  # Configuration dans application.yml
```

### Manuel (via Liquibase CLI)

```bash
# Forcer le rechargement
liquibase --contexts=dev update

# Rollback des données de test
liquibase --contexts=dev rollback-count 1
```

## 🔑 UUIDs de référence

### Cliniques
Les IDs des cliniques suivent le pattern :
```
550e8400-e29b-41d4-a716-4466554400XX
```
où XX va de 01 à 10.

### Vétérinaires
Les IDs des vétérinaires suivent le pattern :
```
660e8400-e29b-41d4-a716-4466554400XX
```
où XX va de 01 à 20.

## 📝 Utilisation dans les tests

### Exemple 1 : Récupérer toutes les cliniques de Paris

```bash
GET /v1/clinics/search?query=Paris
```

**Résultat attendu :** 3 cliniques

### Exemple 2 : Récupérer les vétérinaires spécialistes en chirurgie

```bash
GET /v1/vets/specialty/Chirurgie
```

**Résultat attendu :** 2 vétérinaires (Pierre Dubois, Sébastien Gonzalez)

### Exemple 3 : Récupérer les vétérinaires d'une clinique spécifique

```bash
GET /v1/vets/clinic/550e8400-e29b-41d4-a716-446655440001
```

**Résultat attendu :** 2 vétérinaires (Sophie Martin, Pierre Dubois)

## 🗑️ Nettoyage

Pour supprimer les données de test :

```sql
DELETE FROM vets WHERE id LIKE '660e8400-e29b-41d4-a716-4466554400%';
DELETE FROM clinics WHERE id LIKE '550e8400-e29b-41d4-a716-4466554400%';
```

Ou via Liquibase :

```bash
liquibase rollback-count 2
```

## ⚙️ Configuration

### Fichier Liquibase

Le chargement est configuré dans :
```
db/changelog/v1/017-insert-test-data.yaml
```

Avec le contexte :
```yaml
context: dev,test
```

### Protection Production

Dans `application-prod.yml` :
```yaml
spring:
  liquibase:
    contexts: prod  # 🛡️ Empêche le chargement des données de test
```

## 📚 Références

- [Liquibase Load Data](https://docs.liquibase.com/change-types/load-data.html)
- [Liquibase Contexts](https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html)
