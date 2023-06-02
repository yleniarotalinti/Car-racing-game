In questa directory e' possibile salvare tutta la documentazione relativa al progetto,
file temporanei, istruzioni per per la eventuale creazione/inizializzazione 
del DataBase etc.

L’applicazione che vogliamo realizzare implementerà una gara di automobili, quindi sarà presente una pista su cui correranno più auto contemporaneamente.
Ciascuna automobile potrà muoversi in tutte le direzioni e, in prossimità di una curva, dovrà essere in grado di curvare (quando non pilotata da utente, ci sarà un algoritmo che dovrà farlo…). Si dovrà tenere conto di collisioni tra auto, e di eventi come l’uscita dell’auto dalla pista (valutare se renderlo possibile o meno).


Proposte Agenti: auto e supervisor.

Idea: potremmo avere per esempio un 

- package carRace.agent in cui sarà presente una classe BaseAgent (che implementa Runnable).
  Le classe che estenderanno BaseAgent saranno:
	Supervisor (che estende Base Agent e implementa Observer)
	MoveAgent (quindi tutti gli agenti che si possono muovere, potremmo fare direttamente Cars ma cosi
	se vogliamo aggiungere altri agenti che si muovono si può fare, è più generale)
	Cars che estende MoveAgent.
        Ci sarà un'interfaccia, Moveable, implementata poi dalla classe MoveAgent, in cui andranno
        definiti i metodi che le auto dovranno eseguire.

- package carRace.game, in cui è presente una classe racetrack che salva lo stato della gara, senza però
    renderlo visibile (questo lo fa il carRacePanel).
    Dentro al package è presente anche la classe carRace, in cui è presente il main().


- package carRace.gui, in cui è presente la classe GUI, che rappresenta il JFrame, in cui sarà presente 
    un JPanel, che per noi è la classe carRacePanel.
    carRacePanel comunica direttamente con raceTrack, in pratica lo disegna.
	
- Poi, per gestire movimenti da tastiera o da mouse dovremo implementare KeyListener, quindi per
	esempio aggiungere una classe che si occupa di questo movimento manuale.


Quali sono i compiti degli agenti?

1) SUPERVISOR: 

- deve gestire gli spostamenti di tutti gli agenti presenti.
  Quindi deve conoscere lo stato e la posizione degli agenti e in generale di tutto quello che riguarda la pista
  (controllare che un’auto non esca dalla pista, che non ci siano scontri tra auto ecc..).
  Deve mandare alla grafica la lista di posizioni che ogni personaggio in movimento gli invia, in modo da poter 
  visualizzare graficamente il movimento.
- deve gestire eventi grafici come nascita e morte di un agente (valutare se effettivamente un auto “muore” dopo collisione o meno). 
- deve gestire cambiamenti sulla mappa, per esempio cambiare il tipo di casella in base agli eventi (renderla occupata)
- Quando c’è un evento o un cambio di stato, tutti gli agenti devono comunicare con il supervisor.

Ipotetiche activities del supervisor: 

- G1_SUP_INITIAL_POSITION: viene inviata da TUTTI gli agenti, e notifica la posizione iniziale dell’agente.
- G1_SUP_NOTIFY_REQUEST_MOVE: inviata da TUTTI i MoveAgent, notifica il movimento dell’agente. (Nel payload metto DOVE mi voglio spostare).
- G1_SUP_DEATH: inviata da tutti, e notifica la morte di un agente, per esempio se esce dalla pista.
- G1_SUP_STOP: viene inviata da tutti i MoveAgent, che possono decidere di fermarsi per esempio in caso di guasto…

2) CARS 
- G1_CAR_NOTIFY_CONFIRMED_MOVE: viene inviata dal supervisor a tutti gli agenti, per avvisare dello spostamento di un agente.
- G1_CAR_END_RUN: viene inviata dal supervisor agli agenti ogni volta che ciascuno di questi termina la gara.



