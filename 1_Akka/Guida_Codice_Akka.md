# Guida Definitiva al Codice di Akka (Java)
**Corso:** Networked Software for Distributed Systems (NSDS) — Politecnico di Milano  
**Obiettivo:** Prontuario completo di codice, sintassi e pattern architetturali per superare l'Evaluation Lab di Akka.

---

## Indice degli Argomenti

1. [Definizione dei Messaggi e Regola di Immutabilità](#1-definizione-dei-messaggi-e-regola-di-immutabilità)
2. [Creazione e Istanziazione degli Attori (Props Pattern)](#2-creazione-e-istanziazione-degli-attori-props-pattern)
3. [Ciclo di Vita e Callback Hook](#3-ciclo-di-vita-e-callback-hook)
4. [Pattern di Ricezione e Message Matching](#4-pattern-di-ricezione-e-message-matching)
5. [Pattern di Comunicazione: Tell, Reply, Forward e Ask](#5-pattern-di-comunicazione-tell-reply-forward-e-ask)
6. [Modifica Dinamica del Comportamento (Become & Unbecome)](#6-modifica-dinamica-del-comportamento-become--unbecome)
7. [Il Meccanismo di Stash (Accantonamento Temporaneo)](#7-il-meccanismo-di-stash-accantonamento-temporaneo)
8. [Tolleranza ai Guasti e Strategie di Supervisione](#8-tolleranza-ai-guasti-e-strategie-di-supervisione)
9. [Schedulazione Temporizzata Non Bloccante (Scheduler)](#9-schedulazione-temporizzata-non-bloccante-scheduler)
10. [Trasparenza di Posizione e Remoting TCP](#10-trasparenza-di-posizione-e-remoting-tcp)
11. [Master Example: Sistema Completo Integrato](#11-master-example-sistema-completo-integrato)

---

## 1. Definizione dei Messaggi e Regola di Immutabilità

### Teoria e Regola Ingegneristica
All'interno della medesima JVM, Akka scambia i messaggi passando direttamente il puntatore di memoria nello heap (zero-copy per massimizzare il throughput).
* **Regola aurea:** I messaggi **devono essere rigorosamente immutabili**.
* Tutti i campi devono essere `private final`.
* Nessun metodo setter, solo costruttore completo e metodi getter.
* La classe deve essere `public final` e implementare `Serializable`.

### Sintassi Standard
```java
import java.io.Serializable;

public final class TemperatureMsg implements Serializable {
    private final String sensorId;
    private final double value;
    private final long timestamp;

    public TemperatureMsg(String sensorId, double value, long timestamp) {
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getSensorId() { return sensorId; }
    public double getValue() { return value; }
    public long getTimestamp() { return timestamp; }
}
```

---

## 2. Creazione e Istanziazione degli Attori (Props Pattern)

### Teoria e Meccanica
Non si crea MAI un attore con `new MioAttore()`. L'interazione con l'attore avviene **esclusivamente** tramite un riferimento opaco `ActorRef`.
* Per creare un attore serve un oggetto `Props` (ricetta immutabile di configurazione).
* Pattern consigliato dal PoliMi: metodo statico di fabbrica `props(...)` all'interno dell'attore.
* **Top-Level Actors:** creati dall'`ActorSystem` tramite `system.actorOf(...)`.
* **Child Actors:** creati dall'attore genitore tramite `getContext().actorOf(...)`.

### Esempio Pratico
```java
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class SimpleWorkerActor extends AbstractActor {
    private final String workerId;

    // Costruttore privato o protetto
    public SimpleWorkerActor(String workerId) {
        this.workerId = workerId;
    }

    // Static Factory Method per Props
    public static Props props(String workerId) {
        return Props.create(SimpleWorkerActor.class, () -> new SimpleWorkerActor(workerId));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(String.class, msg -> System.out.println(workerId + " ricevuto: " + msg))
            .build();
    }

    public static void main(String[] args) {
        // 1. Inizializzazione ActorSystem
        ActorSystem system = ActorSystem.create("SystemDemo");

        // 2. Creazione attore top-level
        ActorRef worker = system.actorOf(SimpleWorkerActor.props("Worker-1"), "myWorker");

        // 3. Invio messaggio
        worker.tell("Ciao Akka!", ActorRef.noSender());
    }
}
```

---

## 3. Ciclo di Vita e Callback Hook

### Teoria
Un attore passa attraverso gli stati di creazione, esecuzione, riavvio (*restart*) e arresto (*stop*). È possibile intercettare queste transizioni per allocare o rilasciare risorse esterne (socket, file, timer).

| Metodo Callback | Momento di Chiamata | Uso Tipico |
| :--- | :--- | :--- |
| `preStart()` | Subito dopo la creazione, prima del 1° messaggio | Inizializzazione stato, avvio timer |
| `preRestart(reason, message)` | Sulla vecchia istanza prima del riavvio | Cleanup risorse, chiusura file |
| `postRestart(reason)` | Sulla nuova istanza appena ricreata | Ripristino connessioni |
| `postStop()` | Dopo che l'attore è stato fermato | Pulizia finale, rilascio handle OS |

### Esempio Pratico
```java
import akka.actor.AbstractActor;
import java.util.Optional;

public class LifecycleActor extends AbstractActor {

    @Override
    public void preStart() {
        System.out.println("[LIFECYCLE] preStart: Attore inizializzato e pronto.");
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        System.out.println("[LIFECYCLE] preRestart: Guasto causato da: " + reason.getMessage());
        super.preRestart(reason, message); // Termina ricorsivamente i figli e chiama postStop()
    }

    @Override
    public void postRestart(Throwable reason) {
        System.out.println("[LIFECYCLE] postRestart: Nuova istanza avviata dopo crash.");
        super.postRestart(reason); // Di default richiama preStart()
    }

    @Override
    public void postStop() {
        System.out.println("[LIFECYCLE] postStop: Attore definitivamente arrestato.");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(String.class, msg -> {
                if ("crash".equals(msg)) throw new RuntimeException("Simulazione guasto!");
                System.out.println("Messaggio elaborato: " + msg);
            })
            .build();
    }
}
```

---

## 4. Pattern di Ricezione e Message Matching

### Teoria
In Akka con Java, il comportamento di un attore si definisce implementando il metodo `createReceive()` tramite il `ReceiveBuilder`.
* Le clausole `.match()` vengono valutate **in ordine di scrittura**.
* La prima clausola che soddisfa il tipo di classe (o il predicato) viene eseguita.
* Se nessun match ha successo, il messaggio viene scartato o inviato allo stream dei **Dead Letters**.

### Sintassi e Match Specifici
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
        // 1. Match su classe specifica
        .match(IncrementMessage.class, msg -> {
            this.counter += msg.getDelta();
        })
        // 2. Match su classe con predicato condizionale
        .match(TemperatureMsg.class, msg -> msg.getValue() < 0, msg -> {
            System.err.println("Rilevata temperatura sottozero: " + msg.getValue());
        })
        // 3. Match esatto su valore specifico (matchEquals)
        .matchEquals("RESET", msg -> {
            this.counter = 0;
        })
        // 4. Fallback per qualsiasi altro messaggio non previsto
        .matchAny(msg -> {
            System.out.println("Messaggio sconosciuto ricevuto: " + msg.getClass());
            unhandled(msg); // Invia ai Dead Letters
        })
        .build();
}
```

---

## 5. Pattern di Comunicazione: Tell, Reply, Forward e Ask

### Teoria e Differenze Architetturali
1. **`tell(msg, sender)` (Fire-and-forget):** Asincrono puro, non bloccante. Mette il messaggio in coda ed esce.
   * Da dentro un attore: `target.tell(msg, self())`
   * Da fuori un attore (main): `target.tell(msg, ActorRef.noSender())`
2. **Rispondere al mittente (`sender().tell`):** Il destinatario recupera il mittente con `sender()` e risponde passando se stesso `self()`.
3. **`forward(msg, getContext())`:** Inoltra il messaggio a un terzo attore **mantenendo il mittente originario intatto** (fondamentale per proxy e dispatcher!).
4. **`ask(target, msg, timeout)`:** Invia un messaggio e restituisce una `scala.concurrent.Future`. Viene creato un *Promise Actor* temporaneo. Se atteso in modo sincrono con `Await.result()`, blocca il thread chiamante (usare solo ai confini del sistema o nei test).

### Esempio Pratico: Tell, Forward e Ask
```java
import akka.actor.*;
import static akka.pattern.Patterns.ask;
import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

public class CommunicationDemo {

    // Attore che calcola il quadrato
    public static class ComputeActor extends AbstractActor {
        public static Props props() { return Props.create(ComputeActor.class, ComputeActor::new); }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                .match(Integer.class, n -> {
                    int res = n * n;
                    // Risposta al mittente originale
                    sender().tell(res, self());
                })
                .build();
        }
    }

    // Attore Proxy che inoltra preservando il mittente
    public static class ProxyActor extends AbstractActor {
        private final ActorRef worker;
        public ProxyActor(ActorRef worker) { this.worker = worker; }
        public static Props props(ActorRef worker) {
            return Props.create(ProxyActor.class, () -> new ProxyActor(worker));
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                .match(Integer.class, n -> {
                    System.out.println("Proxy sta inoltrando il dato: " + n);
                    // FORWARD: preserva il sender originale!
                    worker.forward(n, getContext());
                })
                .build();
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("CommSystem");
        ActorRef worker = system.actorOf(ComputeActor.props(), "worker");
        ActorRef proxy = system.actorOf(ProxyActor.props(worker), "proxy");

        // Esempio ASK Pattern dal main:
        Duration timeout = Duration.create(3, TimeUnit.SECONDS);
        Future<Object> future = ask(proxy, 7, 3000);

        Integer result = (Integer) Await.result(future, timeout);
        System.out.println("Risultato ottenuto tramite Ask: " + result); // Stampa 49

        system.terminate();
    }
}
```

---

## 6. Modifica Dinamica del Comportamento (Become & Unbecome)

### Teoria
Evita catene di `if/else` e variabili booleane come `boolean isConnected`. Permette di rimpiazzare l'intera logica di ricezione con una nuova macchina a stati a runtime usando:
* `getContext().become(nuovoReceive)`: Sostituisce il comportamento corrente.
* `getContext().become(nuovoReceive, false)`: Inserisce il nuovo comportamento in uno stack.
* `getContext().unbecome()`: Ripristina il comportamento precedente dallo stack.

### Esempio Pratico: Attore con Stato Disconnesso/Connesso
```java
import akka.actor.AbstractActor;
import akka.actor.Props;

public class ToggleActor extends AbstractActor {

    public static class ConnectMsg {}
    public static class DisconnectMsg {}
    public static class DataMsg {
        private final String data;
        public DataMsg(String data) { this.data = data; }
        public String getData() { return data; }
    }

    public static Props props() { return Props.create(ToggleActor.class, ToggleActor::new); }

    @Override
    public Receive createReceive() {
        return disconnectedBehavior(); // Stato iniziale
    }

    // Comportamento 1: Disconnesso
    private Receive disconnectedBehavior() {
        return receiveBuilder()
            .match(ConnectMsg.class, m -> {
                System.out.println("Connessione stabilita. Passo allo stato CONNESSO.");
                getContext().become(connectedBehavior());
            })
            .match(DataMsg.class, m -> {
                System.err.println("ERRORE: Dati rifiutati, sono disconnesso!");
            })
            .build();
    }

    // Comportamento 2: Connesso
    private Receive connectedBehavior() {
        return receiveBuilder()
            .match(DataMsg.class, m -> {
                System.out.println("Elaboro dato: " + m.getData());
            })
            .match(DisconnectMsg.class, m -> {
                System.out.println("Disconnessione richiesta. Torno a DISCONNESSO.");
                getContext().become(disconnectedBehavior());
            })
            .build();
    }
}
```

---

## 7. Il Meccanismo di Stash (Accantonamento Temporaneo)

### Teoria e Semantica di Mailbox
Se un messaggio arriva in un momento in cui l'attore non è pronto per elaborarlo (es. buffer pieno, attore in fase di inizializzazione o sospensione), l'attore non deve perderlo né scartarlo.
1. Estendere **`AbstractActorWithStash`**.
2. Chiamare **`stash()`** per spostare il messaggio corrente in una coda secondaria privata.
3. Quando si cambia stato, chiamare **`unstashAll()`**.
4. **Regola fondamentale:** I messaggi estratti con `unstashAll()` vengono reinseriti in **TESTA alla Mailbox primaria**, mantenendo il loro ordine relativo originale.

### Esempio Pratico: Contatore che Non Diventa Mai Negativo (Lab 1 Ex 2)
```java
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class NonNegativeCounterActor extends AbstractActorWithStash {
    private int value = 0;

    public static class IncMsg {}
    public static class DecMsg {}

    public static Props props() {
        return Props.create(NonNegativeCounterActor.class, NonNegativeCounterActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(IncMsg.class, m -> {
                value++;
                System.out.println("Incremento. Nuovo valore = " + value);
                // Unstash: se c'erano decrementi in attesa, ora possono essere processati
                unstashAll();
            })
            .match(DecMsg.class, m -> {
                if (value == 0) {
                    System.out.println("Valore a 0! Accantono (stash) il decremento...");
                    stash();
                } else {
                    value--;
                    System.out.println("Decremento eseguito. Nuovo valore = " + value);
                }
            })
            .build();
    }
}
```

---

## 8. Tolleranza ai Guasti e Strategie di Supervisione

### Teoria: La Filosofia "Let It Crash"
L'attore figlio non gestisce errori fatali con blocchi `try/catch`. Se si verifica un'eccezione, fallisce e delega al **Supervisore** (il genitore che lo ha creato con `getContext().actorOf()`).

Il supervisore definisce la politica implementando `supervisorStrategy()`:
* **`OneForOneStrategy`:** Si applica **solo** all'attore che ha fallito.
* **`AllForOneStrategy`:** Si applica a **tutti** i figli del supervisore.

### Le 4 Direttive di Supervisione
1. **`SupervisorStrategy.resume()`:** L'attore NON muore. Lo stato interno in memoria viene **mantenuto intatto** e l'attore passa al messaggio successivo. *(Usato quando l'errore è dovuto a un dato non valido che va semplicemente ignorato).*
2. **`SupervisorStrategy.restart()`:** L'attore viene distrutto e ricreato. Lo stato viene **azzerato** ai valori iniziali del costruttore, ma la **Mailbox viene preservata**.
3. **`SupervisorStrategy.stop()`:** L'attore viene terminato per sempre; la mailbox residua viene scartata.
4. **`SupervisorStrategy.escalate()`:** Il supervisore ammette di non saper gestire l'errore e lo rilancia al proprio genitore.

### Esempio Pratico: Configurazione della Supervisione
```java
import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import java.time.Duration;

public class ManagerSupervisorActor extends AbstractActor {

    // Definizione della strategia: max 10 tentativi in 1 minuto
    private static final SupervisorStrategy STRATEGY = new OneForOneStrategy(
        10,
        Duration.ofMinutes(1),
        DeciderBuilder
            // 1. Se dato non valido -> RESUME (mantieni la memoria)
            .match(IllegalArgumentException.class, e -> {
                System.out.println("[SUPERVISORE] IllegalArgumentException -> RESUME");
                return SupervisorStrategy.resume();
            })
            // 2. Se stato corrotto (NullPointer) -> RESTART (resetta stato, tieni mailbox)
            .match(NullPointerException.class, e -> {
                System.out.println("[SUPERVISORE] NullPointerException -> RESTART");
                return SupervisorStrategy.restart();
            })
            // 3. Altre eccezioni -> STOP
            .match(Exception.class, e -> SupervisorStrategy.stop())
            .build()
    );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return STRATEGY;
    }

    public static Props props() {
        return Props.create(ManagerSupervisorActor.class, ManagerSupervisorActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Props.class, childProps -> {
                // Il supervisore crea il figlio: ne è automaticamente il genitore
                ActorRef child = getContext().actorOf(childProps, "managedChild");
                sender().tell(child, self());
            })
            .build();
    }
}
```

---

## 9. Schedulazione Temporizzata Non Bloccante (Scheduler)

### Teoria e Divieto di `Thread.sleep`
* **MAI usare `Thread.sleep()` all'interno di un attore!** Poiché gli attori condividono un thread pool ristretto, `Thread.sleep` blocca il thread del dispatcher, paralizzando decine di altri attori.
* Per simulare ritardi o inviare messaggi periodici si usa lo **Scheduler nativo di Akka**:
  `getContext().system().scheduler().scheduleOnce(durata, destinatario, messaggio, dispatcher, mittente)`.

### Esempio Pratico: Elaborazione Ritardata Non Bloccante
```java
import akka.actor.AbstractActor;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

public class DelayEchoActor extends AbstractActor {
    public static class ProcessMsg {
        private final String text;
        public ProcessMsg(String text) { this.text = text; }
        public String getText() { return text; }
    }

    public static Props props() { return Props.create(DelayEchoActor.class, DelayEchoActor::new); }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ProcessMsg.class, msg -> {
                System.out.println("Ricevuto: " + msg.getText() + ". Pianifico risposta tra 2 secondi...");

                // Invece di Thread.sleep(2000), inviamo un messaggio ritardato a sender()
                getContext().system().scheduler().scheduleOnce(
                    Duration.create(2, TimeUnit.SECONDS),
                    sender(),
                    "ELABORATO: " + msg.getText().toUpperCase(),
                    getContext().dispatcher(),
                    self()
                );
            })
            .build();
    }
}
```

---

## 10. Trasparenza di Posizione e Remoting TCP

### Teoria
Inviare un messaggio a un attore su un'altra macchina richiede la stessa sintassi: `actorRef.tell(msg, self())`.
* Configurazione in `application.conf` con protocollo Netty TCP: porta, hostname.
* Risoluzione remota tramite URI gerarchico con **`actorSelection`**:
  `akka.tcp://NomeSistema@IP_Remoto:Porta/user/NomeAttore`.

### Esempio Pratico
```java
// 1. Configurazione Server (in codice o in application.conf):
// Porta 6123, nome sistema "ServerSys", attore "serverActor"

// 2. Client che contatta il Server Remoto via ActorSelection:
public class RemoteClientActor extends AbstractActor {
    private final String remotePath = "akka.tcp://ServerSys@127.0.0.1:6123/user/serverActor";

    @Override
    public void preStart() {
        // Risoluzione tramite ActorSelection
        ActorSelection selection = getContext().actorSelection(remotePath);
        
        // Invio del primo messaggio: quando il server risponde, 
        // sender() conterrà il suo concreto ActorRef
        selection.tell("Ping dal Client Remoto", self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(String.class, reply -> {
                System.out.println("Risposta ricevuta dal server remoto: " + reply);
                // Da questo momento possiamo salvare sender() per comunicazioni dirette
                ActorRef serverRef = sender();
            })
            .build();
    }
}
```

---

## 11. Master Example: Sistema Completo Integrato

Questo esempio finale condensa **tutti i pattern visti finora** in un unico scenario realistico in stile Evaluation Lab PoliMi:
* **Attore Supervisore (`SensorSupervisorActor`)** con politica `OneForOneStrategy` e recupero `Resume` su letture errate.
* **Attore Elaboratore con Stash e Become (`ResilientProcessorActor`)**:
  * Mantiene la media corrente delle misure.
  * Se riceve `PauseMsg`, passa a comportamento `paused` e accantona le letture con `stash()`.
  * Se riceve `ResumeMsg`, rilascia tutto con `unstashAll()`.
  * Se riceve un valore negativo ($< 0$), lancia `IllegalArgumentException` (il supervisore applica `Resume` preservando la media precedente).
* **Pattern `tell` e `forward`** per l'instradamento trasparente.

```java
package it.polimi.nsds.akka.master;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class AkkaMasterSystem {

    // ==========================================
    // 1. MESSAGGI IMMUTABILI
    // ==========================================
    public static final class MeasureMsg implements Serializable {
        private final String sensorId;
        private final double value;
        public MeasureMsg(String sensorId, double value) {
            this.sensorId = sensorId;
            this.value = value;
        }
        public String getSensorId() { return sensorId; }
        public double getValue() { return value; }
    }

    public static final class PauseMsg implements Serializable {}
    public static final class WakeupMsg implements Serializable {}
    public static final class QueryAvgMsg implements Serializable {}

    // ==========================================
    // 2. ATTORE PROCESSOR CON STASH E BECOME
    // ==========================================
    public static class ResilientProcessorActor extends AbstractActorWithStash {
        private double sum = 0.0;
        private int count = 0;

        public static Props props() {
            return Props.create(ResilientProcessorActor.class, ResilientProcessorActor::new);
        }

        @Override
        public Receive createReceive() {
            return activeBehavior(); // Stato iniziale attivo
        }

        // Comportamento ATTIVO
        private Receive activeBehavior() {
            return receiveBuilder()
                .match(MeasureMsg.class, m -> {
                    // Validazione: i valori negativi sollevano eccezione
                    if (m.getValue() < 0) {
                        System.err.printf("[%s] ERRORE: Valore non valido (%.2f)! Sollevo eccezione...%n",
                                self().path().name(), m.getValue());
                        throw new IllegalArgumentException("Temperatura negativa!");
                    }
                    count++;
                    sum += m.getValue();
                    double avg = sum / count;
                    System.out.printf("[%s] Elaborata misura: %.2f | Media attuale: %.2f (Campioni: %d)%n",
                            self().path().name(), m.getValue(), avg, count);
                })
                .match(PauseMsg.class, m -> {
                    System.out.printf("[%s] Ricevuto PAUSE. Passo a modalita di sospensione (PAUSED).%n",
                            self().path().name());
                    getContext().become(pausedBehavior());
                })
                .match(QueryAvgMsg.class, m -> {
                    double avg = count > 0 ? (sum / count) : 0.0;
                    sender().tell(avg, self());
                })
                .build();
        }

        // Comportamento PAUSED (accumula le misure con stash)
        private Receive pausedBehavior() {
            return receiveBuilder()
                .match(WakeupMsg.class, m -> {
                    System.out.printf("[%s] Ricevuto WAKEUP! Ripristino stato attivo e svuoto lo Stash.%n",
                            self().path().name());
                    getContext().become(activeBehavior());
                    unstashAll(); // Reinserisce i messaggi in TESTA alla mailbox!
                })
                .match(MeasureMsg.class, m -> {
                    System.out.printf("[%s] Attore in pausa: accantono misura (%.2f) nello stash.%n",
                            self().path().name(), m.getValue());
                    stash(); // Accantona il messaggio
                })
                .match(QueryAvgMsg.class, m -> {
                    // Risponde comunque anche da in pausa
                    double avg = count > 0 ? (sum / count) : 0.0;
                    sender().tell(avg, self());
                })
                .build();
        }
    }

    // ==========================================
    // 3. ATTORE SUPERVISORE CON ONE-FOR-ONE STRATEGY
    // ==========================================
    public static class SensorSupervisorActor extends AbstractActor {
        private ActorRef processorChild;

        // Politica di supervisione: RESUME preserva lo stato (somma e conteggio)
        private static final SupervisorStrategy STRATEGY = new OneForOneStrategy(
            10,
            Duration.create(1, TimeUnit.MINUTES),
            DeciderBuilder
                .match(IllegalArgumentException.class, e -> {
                    System.out.println("[SUPERVISOR] Intercettata IllegalArgumentException -> RESUME!");
                    return SupervisorStrategy.resume();
                })
                .matchAny(e -> SupervisorStrategy.restart())
                .build()
        );

        @Override
        public SupervisorStrategy supervisorStrategy() {
            return STRATEGY;
        }

        public static Props props() {
            return Props.create(SensorSupervisorActor.class, SensorSupervisorActor::new);
        }

        @Override
        public void preStart() {
            // Crea il figlio gestito
            processorChild = getContext().actorOf(ResilientProcessorActor.props(), "processorChild");
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                // Inoltro trasparente con FORWARD (mantiene il mittente originale intatto)
                .matchAny(msg -> processorChild.forward(msg, getContext()))
                .build();
        }
    }

    // ==========================================
    // 4. METODO MAIN DI SIMULAZIONE
    // ==========================================
    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MasterAkkaSystem");

        // Istanziazione del supervisore di sistema
        ActorRef supervisor = system.actorOf(SensorSupervisorActor.props(), "sensorSupervisor");

        System.out.println("--- FASE 1: Inserimento misure valide ---");
        supervisor.tell(new MeasureMsg("S1", 20.0), ActorRef.noSender());
        supervisor.tell(new MeasureMsg("S1", 30.0), ActorRef.noSender());

        Thread.sleep(300);

        System.out.println("\n--- FASE 2: Inserimento misura errata (Test Supervisione Resume) ---");
        supervisor.tell(new MeasureMsg("S1", -15.0), ActorRef.noSender()); // Fallimento!
        supervisor.tell(new MeasureMsg("S1", 25.0), ActorRef.noSender());  // La media deve continuare da 25

        Thread.sleep(300);

        System.out.println("\n--- FASE 3: Test Stash e Become (Pausa e accumulo) ---");
        supervisor.tell(new PauseMsg(), ActorRef.noSender());
        supervisor.tell(new MeasureMsg("S2", 40.0), ActorRef.noSender()); // Andrà nello stash
        supervisor.tell(new MeasureMsg("S2", 50.0), ActorRef.noSender()); // Andrà nello stash

        Thread.sleep(300);

        System.out.println("\n--- FASE 4: Risveglio ed elaborazione dello Stash ---");
        supervisor.tell(new WakeupMsg(), ActorRef.noSender()); // UnstashAll eseguirà 40.0 e 50.0

        Thread.sleep(500);

        // Chiusura pulita del sistema
        system.terminate();
    }
}
```

---

### Riepilogo di Cosa Ricordare a Memoria per il Lab
1. **Messaggi:** `public final class NomeMsg implements Serializable` con campi `private final` e solo getter.
2. **Creazione:** `Props.create(Classe.class, () -> new Classe(arg))` dentro a un metodo statico `props()`.
3. **Ricezione:** `receiveBuilder().match(Classe.class, msg -> { ... }).build()`.
4. **Stash:** Estendere `AbstractActorWithStash`, chiamare `stash()` per accumulare e `unstashAll()` al cambio di stato.
5. **Become:** `getContext().become(nuovoMetodoReceive())`.
6. **Inoltro:** `destinatario.forward(msg, getContext())`.
7. **Supervisione Resume:** `SupervisorStrategy.resume()` non azzera le variabili d'istanza dell'attore.
8. **Mai bloccare:** `scheduleOnce(...)` al posto di `Thread.sleep()`.

