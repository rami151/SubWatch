SubWatch


1. Présentation du projet
1.1 Problématique
Un utilisateur qui cumule plusieurs abonnements (vidéo, musique, stockage en ligne, logiciels) n’a pas de vue d’ensemble de ses prélèvements. Les dates de renouvellement diffèrent d’un service à l’autre, les périodes d’essai gratuites passent en facturation sans avertissement, et le coût annuel cumulé n’apparaît qu’en relevant les montants un par un.
1.2 Objectif
Développer une application Android native qui :
recense les abonnements saisis manuellement par l’utilisateur ;
calcule la prochaine échéance de chaque abonnement à partir de sa date d’ancrage et de son cycle de facturation ;
envoie un rappel local N jours avant chaque prélèvement, avec un message spécifique pour la fin d’un essai gratuit ;
affiche le coût mensuel équivalent et le coût annuel total, séparément pour chaque devise ;
fonctionne intégralement hors ligne : les données restent sur l’appareil, sans compte, sans serveur, sans accès bancaire.
1.3 Utilisateurs et périmètre
Un seul profil : le propriétaire du téléphone, qui saisit lui-même ses abonnements. L’application n’a ni compte utilisateur,ni synchronisation. 
1.4 Contraintes 
Équipe : une personne.
Technologies : Java, XML (interfaces) et SQLite pour le stockage, via la bibliothèque Room.
Plateforme : Android 8.0 (API 26) minimum, smartphones, orientation portrait.

2. Analyse de l’existant
Des solutions de suivi d’abonnements existent déjà. Elles se répartissent en deux familles : celles qui lisent les transactions bancaires de l’utilisateur, et celles qui reposent sur une saisie manuelle. Le tableau ci-dessous compare quatre d’entre elles, d’après leur description publique.
Solution
Principe
Limites pour le besoin visé
Abonnements Google Play
Liste intégrée au Play Store des abonnements souscrits via Google Play
Ne couvre pas les services facturés directement par le fournisseur (carte bancaire, PayPal). Pas de vue par catégorie ni de coût global consolidé.
Rocket Money
Détecte les abonnements en se connectant aux comptes bancaires
Exige une liaison bancaire et un compte. Conçue principalement pour le marché américain. Certaines fonctions sont payantes.
Bobby
Saisie manuelle, rappels avant échéance, total mensuel et annuel
Disponible sur iOS uniquement.
Wallos
Application libre de suivi d’abonnements, saisie manuelle, multi-devises, notifications
Application web à héberger soi-même (serveur à installer et à maintenir), non conçue comme application mobile native.


Positionnement de SubWatch : application Android native, saisie manuelle, fonctionnement sans connexion ni compte, sans accès à des données bancaires ou de messagerie, avec suivi des périodes d’essai et rappels locaux.

3. Acteurs et cas d’utilisation
Acteur unique : l’Utilisateur (propriétaire de l’appareil).
ID
Cas d’utilisation
Description sommaire
UC1
Créer un abonnement
Saisir nom, montant, devise, cycle, date de début, catégorie ; option essai gratuit avec date de fin.
UC2
Consulter la liste
Lister les abonnements actifs, triés par prochaine échéance (tri par défaut), par nom ou par montant.
UC3
Consulter une fiche
Voir toutes les informations d’un abonnement, dont sa prochaine échéance calculée.
UC4
Modifier un abonnement
Éditer les champs ; la prochaine échéance est recalculée (RG1).
UC5
Supprimer un abonnement
Suppression après confirmation.
UC6
Archiver / réactiver
Archiver un abonnement résilié ; le réactiver. Un archivé reste consultable mais est exclu des totaux et rappels (RG5).
UC7
Filtrer et rechercher
Filtrer par catégorie et par statut (actif, archivé) ; rechercher par nom.
UC8
Suivre un essai gratuit
Pendant l’essai, l’échéance affichée est la fin d’essai ; à cette date, l’abonnement passe en facturation normale (F8, T9).
UC9
Recevoir des rappels
Notification locale N jours avant chaque échéance, à l’heure choisie ; un appui ouvre la fiche.
UC10
Consulter le tableau de bord
Coûts mensuel/annuel par devise, 5 prochaines échéances, essais en cours.
UC11
Consulter les statistiques
Répartition mensuelle par catégorie et top 5 des abonnements les plus coûteux (devise par défaut, RG9).
UC12
Paramétrer l’application
Devise par défaut, délai et heure des rappels, thème ; toute modification reprogramme la tâche de rappel (RG13).



4. Besoins fonctionnels
Priorités : P1 = indispensable , P2 = prévu , P3 = reporté aux évolutions .
4.1 Gestion des abonnements
ID
Exigence
Priorité
F1
Créer un abonnement avec : nom (50 caractères max.), montant (entier strictement supérieur à 0, dans la plus petite unité de la devise), devise (code ISO 4217), cycle de facturation (hebdomadaire, mensuel, trimestriel, annuel), date de début, catégorie. Notes facultatives (200 caractères max.).
P1
F2
Modifier un abonnement. La prochaine échéance est recalculée après modification (RG1).
P1
F3
Supprimer un abonnement après confirmation.
P1
F4
Archiver un abonnement résilié et le réactiver. Un abonnement archivé reste consultable ; sa réactivation recalcule la prochaine échéance (RG11).
P2
F5
Lister les abonnements actifs, triés par prochaine échéance (tri par défaut), par nom ou par montant.
P1
F6
Filtrer la liste par catégorie et par statut (actif, archivé), et rechercher par nom.
P2


4.2 Essais gratuits
ID
Exigence
Priorité
F7
Marquer un abonnement comme période d’essai, avec une date de fin d’essai obligatoire et postérieure à la date de début (RG10).
P1
F8
Pendant l’essai, l’échéance affichée est la date de fin d’essai. À cette date, l’abonnement passe en facturation normale selon son cycle. La transition est vérifiée par le scénario T9.
P1


4.3 Rappels
ID
Exigence
Priorité
F9
Envoyer une notification locale N jours avant chaque échéance (N = 3 par défaut, réglable de 0 à 14), à l’heure choisie (09:00 par défaut). La tâche est reprogrammée à chaque changement de paramètre (RG13).
P1
F10
La notification indique le nom, le montant et la date de l’échéance. Un appui ouvre la fiche de l’abonnement.
P1
F11
Pour un essai, la notification indique que l’essai se termine et qu’un prélèvement va suivre.
P1
F12
Si l’utilisateur refuse la permission de notification, afficher un bandeau qui explique comment l’activer.
P2


4.4 Tableau de bord et statistiques
ID
Exigence
Priorité
F13
Afficher le coût mensuel équivalent et le coût annuel des abonnements actifs, séparément pour chaque devise.
P1
F14
Afficher les 5 prochaines échéances et le nombre d’essais en cours.
P1
F15
Afficher la répartition des dépenses mensuelles par catégorie (graphique circulaire) et les 5 abonnements les plus coûteux (barres), calculés dans la devise par défaut (RG9).
P2


4.5 Catégories et paramètres
ID
Exigence
Priorité
F16
Fournir des catégories prédéfinies : Vidéo, Musique, Cloud, Logiciels, Éducation, Jeux, Autre.
P1
F17
Régler la devise par défaut, le délai et l’heure des rappels, et le thème (clair, sombre, système). Toute modification des rappels reprogramme la tâche WorkManager (RG13).
P2
F18
Créer, renommer et supprimer des catégories personnalisées.
P3
F19
Afficher les échéances du mois dans une vue calendrier.
P3
F20
Exporter la liste des abonnements au format CSV (sert également de parade contre la perte de données, §12).
P2



5. Règles de gestion
ID
Règle
RG1
Les échéances sont calculées à partir d’une date d’ancrage : échéance n = date d’ancrage + n cycles. La date d’ancrage est la date de début, ou la date de fin d’essai pour un essai. La prochaine échéance est la première échéance supérieure ou égale à la date du jour.
RG2
Si le jour d’ancrage n’existe pas dans le mois visé (31, 30, ou 29 février), l’échéance tombe le dernier jour du mois. Le jour d’ancrage reste celui d’origine : un abonnement ancré au 31 janvier est dû le 28 (ou 29) février, puis le 31 mars.
RG3
Coût mensuel équivalent : hebdomadaire = montant × 52 ÷ 12 ; mensuel = montant ; trimestriel = montant ÷ 3 ; annuel = montant ÷ 12. Coût annuel = coût mensuel équivalent × 12. Les calculs utilisent BigDecimal.
RG4
Pas de conversion de devises en version 1. Les totaux sont calculés et affichés séparément pour chaque devise.
RG5
Un abonnement archivé est exclu des totaux, des statistiques et des rappels.
RG6
Les échéances dépassées sont recalculées à chaque ouverture de l’application et à chaque exécution du traitement quotidien des rappels.
RG7
Un rappel n’est envoyé qu’une seule fois par échéance (colonne last_notified_due).
RG8
Les montants sont stockés en entiers, dans la plus petite unité de la devise (nombre de décimales donné par Currency.getDefaultFractionDigits()).
RG9
Les statistiques par catégorie (F15) et le tableau de bord consolidé sont calculés dans la devise par défaut uniquement ; les abonnements libellés dans une autre devise sont exclus du graphique circulaire et signalés à l’utilisateur. Les totaux par devise (F13) restent affichés séparément.
RG10
La date de fin d’essai doit être strictement postérieure à la date de début ; la saisie est refusée sinon, avec un message explicite.
RG11
La réactivation d’un abonnement archivé recalcule la prochaine échéance depuis la date d’ancrage (RG1), sans tenir compte des périodes d’inactivité.
RG12
La suppression d’une catégorie utilisée conserve les abonnements rattachés (ON DELETE SET NULL) ; ils apparaissent sous la catégorie « Non classé » dans les listes et filtres.
RG13
Toute modification des paramètres de rappel (délai N, heure) annule et reprogramme la tâche WorkManager afin que les nouveaux réglages s’appliquent dès la prochaine échéance.
RG14
Les doublons de nom sont autorisés (aucune contrainte d’unicité sur le nom) : l’utilisateur reste responsable de la saisie, une homonymie n’étant pas une erreur en soi.



6. Exigences non fonctionnelles
Plateforme : Android 8.0 (API 26) minimum, smartphones, orientation portrait.
Hors ligne : toutes les fonctions marchent sans réseau. L’application ne déclare pas la permission INTERNET.
Performance : démarrage à froid en moins de 2 secondes sur l’appareil de test. Une liste de 200 abonnements défile sans saccade. Aucun accès à la base sur le thread principal.
Fiabilité : les rappels sont maintenus après un redémarrage de l’appareil (WorkManager). Les données survivent aux mises à jour de l’application (migrations Room explicites).
Interface : Material Design 3, thèmes clair et sombre, dimensions en dp et sp, description textuelle sur les icônes (contentDescription), zones tactiles d’au moins 48 dp.
Accessibilité : navigation possible avec TalkBack (étiquettes, ordre de focus cohérent), contrastes conformes aux directives Material.
Localisation : tous les textes dans strings.xml (français ; anglais facultatif). Dates et montants formatés selon la Locale.
Confidentialité : données dans le stockage privé de l’application, sauvegarde automatique Android désactivée (allowBackup à false). Aucune donnée personnelle n’est journalisée (logs).
Code : dépôt Git, un commit par tâche terminée, logique métier isolée dans des classes Java sans dépendance Android (testables unitairement).
7. Interfaces et navigation
Navigation : une seule Activity, des Fragments, le composant Navigation de Jetpack et une barre de navigation inférieure pour les trois onglets principaux.
Écran
Contenu
Accès
Tableau de bord
Coûts mensuel et annuel par devise, 5 prochaines échéances, essais en cours
Onglet 1, écran d’accueil
Abonnements
Liste triable et filtrable, champ de recherche, bouton d’ajout
Onglet 2
Statistiques
Graphique par catégorie (devise par défaut), 5 abonnements les plus coûteux
Onglet 3
Fiche abonnement
Toutes les informations, prochaine échéance, actions Modifier, Archiver, Supprimer
Appui sur un élément de la liste ou sur une notification
Formulaire
Création et édition : sélecteur de date, sélecteur de catégorie, option essai gratuit
Bouton d’ajout ou action Modifier
Paramètres
Devise par défaut, rappels, thème
Icône de la barre supérieure


États d’interface à prévoir : liste vide (message d’accueil + bouton d’ajout), saisie invalide (erreurs sous les champs), permission de notification refusée (bandeau F12), chargement des données.

8. Conception technique
8.1 Architecture
Quatre couches : Fragment (interface XML avec ViewBinding), ViewModel avec LiveData, Repository, DAO (Room). Les requêtes Room s’exécutent hors du thread principal (executor dédié ou gestion native des tâches asynchrones) et leurs résultats sont publiés dans des LiveData. Le calcul des échéances et des coûts est réalisé par des classes Java pures (BillingCalculator), testables sans Android.
8.2 Outils et bibliothèques
Composant
Choix
Langage
Java 21
Interface
Layouts XML, ViewBinding, Material Components (Material 3)
Navigation
Jetpack Navigation Component
Base de données
Room Persistence Library (couche d’abstraction officielle Jetpack sur SQLite) : entités et DAO annotés, requêtes vérifiées à la compilation, migrations explicites (objets Migration), clés étrangères déclaratives, observables LiveData natifs.
Tâches planifiées
WorkManager
Graphiques
MPAndroidChart (isolée dans une classe dédiée, remplaçable par un dessin Canvas)
Dates
java.time (disponible à partir de l’API 26)
Préférences
SharedPreferences (devise, rappels, thème)
Tests
JUnit 4 (unitaires), AndroidX Test avec base Room en mémoire (instrumentés)
Outils
Android Studio, Git et GitHub, Figma


8.3 Modèle de données
Entité Subscription (@Entity(tableName = "subscription"), index sur next_due_date et category_id)
Colonne
Type
Contraintes
id
INTEGER
Clé primaire, auto-incrément (@PrimaryKey(autoGenerate = true))
name
TEXT
NOT NULL
amount_minor
INTEGER
NOT NULL, supérieur à 0
currency
TEXT
NOT NULL, code ISO 4217
billing_cycle
TEXT
NOT NULL : WEEKLY, MONTHLY, QUARTERLY, YEARLY
start_date
TEXT
NOT NULL, format yyyy-MM-dd
next_due_date
TEXT
NOT NULL, valeur dérivée, recalculée (RG6)
category_id
INTEGER
Clé étrangère vers Category avec onDelete = SET_NULL (RG12)
is_trial
INTEGER
NOT NULL, 0 ou 1
trial_end_date
TEXT
Renseignée uniquement si is_trial vaut 1 ; postérieure à start_date (RG10)
status
TEXT
NOT NULL : ACTIVE ou ARCHIVED
last_notified_due
TEXT
Dernière échéance ayant déclenché un rappel (RG7)
notes
TEXT
Facultatif
created_at / updated_at
TEXT
Horodatage d’audit, format ISO 8601


Entité Category (@Entity)
Colonne
Type
Contraintes
id
INTEGER
Clé primaire, auto-incrément
name
TEXT
NOT NULL, UNIQUE (index unique Room)
color
INTEGER
Couleur ARGB


Les clés étrangères et contraintes sont déclarées par annotations Room (@ForeignKey, indices) et appliquées par Room lors de la création du schéma SQLite.
8.4 Rappels
Les rappels reposent sur WorkManager et non sur AlarmManager. Les alarmes exactes demandent la permission SCHEDULE_EXACT_ALARM à partir d’Android 12, refusée par défaut sur Android 14 pour la plupart des applications, et elles sont perdues au redémarrage sans récepteur BOOT_COMPLETED. WorkManager conserve ses tâches après un redémarrage.
Un PeriodicWorkRequest unique (24 h), dont le délai initial est calculé pour tomber à l’heure de rappel choisie ; toute modification des paramètres annule et reprogramme la tâche (RG13).
À chaque exécution : recalcul des échéances dépassées (RG6), puis sélection des abonnements actifs dont l’échéance tombe dans les N prochains jours et pour lesquels last_notified_due est différent de l’échéance (RG7).
Un canal de notification dédié (obligatoire depuis Android 8) et la permission POST_NOTIFICATIONS demandée à l’exécution à partir d’Android 13.
L’heure d’envoi est approximative : WorkManager peut la décaler de quelques minutes, ce qui est acceptable pour un rappel de prélèvement.
9. Sécurité et confidentialité
Données stockées exclusivement dans le stockage privé de l’application (/data/data/<package>/databases).
Sauvegarde automatique Android désactivée (android:allowBackup="false") afin d’éviter la copie de la base vers un stockage accessible.
Aucune permission réseau : l’application ne déclare pas INTERNET et ne communique avec rien.
Aucune donnée sensible (montants, noms d’abonnements) n’est écrite dans les logs.
L’APK release est minifié et obfusqué (R8), ce qui réduit la surface d’attaque et la taille.
L’export CSV (F20) est écrit dans le stockage spécifique à l’application (aucune permission de stockage requise en API 26+).

10. Évolutions prévues
Version 1
Vue calendrier mensuel et catégories personnalisées (F18, F19), si non réalisées en version 1.
Import CSV servant de restauration de sauvegarde (complément de F20).
Action « Archiver » directement dans la notification.
Widget d’écran d’accueil listant les prochaines échéances.
Traduction en arabe avec prise en charge de l’affichage de droite à gauche.
Version 2
Conversion de devises, avec taux saisis manuellement ou récupérés par API.
Sauvegarde chiffrée vers un fichier choisi par l’utilisateur (Storage Access Framework).
Abonnements partagés : part de chaque participant.
Historique des changements de prix d’un abonnement.
Version 3
Synchronisation entre appareils : serveur et authentification.
Détection de paiements à partir des notifications ou SMS, sous réserve des restrictions de Google Play sur ces permissions.
Portage iOS.

