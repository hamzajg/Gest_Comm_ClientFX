# language: fr
Fonctionnalité: Connexion et profil utilisateur
  Le lancement demande qui utilise l'application, le profil montre le compte
  et son rôle, et les préférences (thème, langue) et le mot de passe
  appartiennent à la personne connectée.

  @w1
  Scénario: Sign in with valid credentials
    Étant donné une application lancée sans session
    Et un compte « operator » avec un mot de passe valide
    Quand l'utilisateur saisit son nom et son mot de passe
    Alors il voit le bureau avec l'onglet Bienvenue
    Et le profil connaît son nom d'utilisateur et son rôle

  @w1
  Scénario: Refuse wrong credentials
    Étant donné une application lancée sans session
    Quand l'utilisateur saisit un mot de passe incorrect
    Alors la fenêtre de connexion reste affichée
    Et un message d'erreur nomme le refus du serveur
    Et aucun bureau n'est construit

  @w1
  Scénario: Identity endpoint reports the role
    Étant donné un utilisateur authentifié « staff »
    Quand le client demande GET /api/me
    Alors la réponse porte son nom d'utilisateur, son e-mail et le rôle « admin »
    Et le même appel avec un compte ordinaire renvoie le rôle « operator »

  @w1
  Scénario: No stored password
    Étant donné une connexion réussie
    Quand l'utilisateur relance l'application
    Alors la session se rouvre avec le jeton de rafraîchissement
    Et aucun mot de passe n'est écrit dans les préférences de l'application

  @w2
  Scénario: Profile shows identity and role
    Étant donné une session ouverte en tant qu'administrateur
    Quand l'utilisateur ouvre l'onglet Profil
    Alors il lit son nom, son e-mail et le rôle « Administrateur »

  @w2
  Scénario: Change password
    Étant donné une session ouverte
    Quand l'utilisateur saisit son mot de passe actuel et un nouveau mot de passe valide
    Alors le serveur accepte le changement
    Et le prochain lancement accepte le nouveau mot de passe
    Et un mot de passe actuel incorrect est refusé avec le message du serveur

  @w2
  Scénario: Sign out returns to login
    Étant donné une session ouverte
    Quand l'utilisateur choisit « Se déconnecter »
    Alors le jeton de rafraîchissement est révoqué
    Et la fenêtre de connexion réapparaît sans le bureau

  @w3
  Scénario: Theme survives a restart
    Étant donné une session ouverte
    Quand l'utilisateur choisit le thème clair dans son profil
    Alors le bureau passe immédiatement en palette claire
    Et après un redémarrage le profil ouvre la même palette

  @w3
  Scénario: Language switches the shell
    Étant donné une session ouverte en français
    Quand l'utilisateur choisit « English » dans son profil
    Alors les menus, le profil et le tiroir s'affichent en anglais
    Et après un redémarrage l'application démarre en anglais

  @w4
  Scénario: Role gates destructive actions
    Étant donné une session ouverte en tant qu'opérateur
    Quand il ouvre le tiroir d'actions sur la liste des clients
    Alors les actions « Supprimer » et « Modifier » ne sont pas proposées
    Et en tant qu'administrateur elles le sont

  @w4
  Scénario: All record screens follow the language
    Étant donné la langue « English »
    Quand l'utilisateur ouvre la liste des clients et le formulaire client
    Alors les en-têtes de colonnes et les libellés du formulaire sont en anglais
