# url-shortner-project
=================

Organisation du projet:
-----------------------
- bin -> Gestion du module des dépendances
- metier -> Contient uniquement le module permettant de vérifier les liens
- model -> Contient tous les accès à la base de donnée (en fait, seul insertNewLink.js est utilisé car il y a un problème de synchronisation avec le routage)
- nbproject -> Repertoire système
- node_modules -> Contient tous les modules du projet
- public -> Contient les fichiers javascript (Pour le client) et css
- routes -> Tous le fichiers de routes nécessaire à la gestion
- socket -> Repertoire de socket.io pour les gestion des socket
- tests -> Contient tous les test effectués avec tape
- views -> Template permettant l'affichage
- app.js -> Contient toute la partie gestion du projet
- package.json -> Fichier d'identification du projeet


Modules utilisés:
-----------------
- express -> Framework utilisé
- mongoose -> Permet l'accès à mongoLab pour le persistance
- nunjucks -> Usine à template permettant de simplifier la création des vues
- shortid -> Permet de créer des id uniques pour les liens
- socket.io -> Permet une gestion simplifié des sockets
- tape -> Permet d'effectuer des test


Route:
------
- addLink.js -> Appelé quand on veut ajouter un nouveau lien
- admin.js -> Appelé par l'administrateur
- index.js -> Non utilisé
- toLink.js -> Permet la redirection à partid de l'in du lien
- user.js -> Appelé par les utilisateurs
