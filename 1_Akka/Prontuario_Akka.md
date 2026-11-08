# Prontuario Rapido di Akka Java per Evaluation Lab

Questo prontuario raccoglie la sintassi fondamentale, i comandi atomici e i template completi delle classi per affrontare i compiti di valutazione su Akka (Evaluation Lab).

---

# PARTE 1: Prontuario Sintetico dei Comandi Atomici

### 1. Invio Messaggi (Tell)
* `target.tell(msg, ActorRef.noSender());`
  * **Cosa fa:** Invia un messaggio asincrono in modalità fire-and-forget dall'esterno del sistema (es. dal `main`).
* `target.tell(msg, self());`
  * **Cosa fa:** Invia un messaggio asincrono dall'interno di un attore, impostando se stesso come mittente per consentire eventuali risposte.
* `sender().tell(replyMsg, self());`
  * **Cosa fa:** Risponde al mittente dell'ultimo messaggio estratto dalla mailbox.

### 2. Inoltro Trasparente (Forward)
* `target.forward(msg, getContext());`
  * **Cosa fa:** Inoltra il messaggio a un altro attore **preservando il `sender()` originale intatto** (usato da dispatcher, router e proxy).

### 3. Richiesta Sincrona con Future (Ask)
* `Future<Object> f = ask(target, msg, timeoutMs);`
  * **Cosa fa:** Invia una richiesta creando un attore temporaneo e restituisce un `Future` che conterrà la risposta.
* `Object res = f.result(duration, null);`
  * **Cosa fa:** Blocca in modo sincrono il thread corrente in attesa del completamento del `Future`. Lancia `TimeoutException` se il tempo scade.

### 4. Creazione Attori
* `sys.actorOf(props, "nome");`
  * **Cosa fa:** Crea un attore top-level (supervisionato dallo User Guardian predefinito di Akka). Si usa tipicamente nel `main`.
* `getContext().actorOf(props, "nome");`
  * **Cosa fa:** Crea un attore **figlio** all'interno dell'attore corrente. Permette al padre di supervisionare il ciclo di vita e i guasti del figlio.

### 5. Configurazione e Props
* `Props.create(ClasseAttore.class);`
  * **Cosa fa:** Crea l'oggetto ricetta per un attore con costruttore vuoto.
* `Props.create(ClasseAttore.class, () -> new ClasseAttore(arg1, arg2));`
  * **Cosa fa:** Crea la ricetta per un attore con costruttore parametrico.

### 6. Macchina a Stati (Become)
* `getContext().become(nuovoReceive);`
  * **Cosa fa:** Sostituisce a runtime il comportamento di ricezione dei messaggi con un altro metodo `Receive`.

### 7. Accantonamento Messaggi (Stash)
* `stash();`
  * **Cosa fa:** Mette da parte il messaggio corrente in una coda FIFO ausiliaria senza elaborarlo (richiede `AbstractActorWithStash`).
* `unstashAll();`
  * **Cosa fa:** Svuota la coda di stash, reinserendo tutti i messaggi accumulati **in testa alla mailbox primaria** nell'ordine originale.

### 8. Gestione del Tempo Asincrona (Scheduler)
* `getContext().system().scheduler().scheduleOnce(delay, self(), msg, getContext().dispatcher(), self());`
  * **Cosa fa:** Programma l'invio asincrono di un messaggio differito nel tempo (es. un segnale di fine lavoro) senza bloccare i thread del sistema.

### 9. Politiche di Supervisione
* `new OneForOneStrategy(maxRetries, timeWindow, decider);`
  * **Cosa fa:** Applica la decisione di recupero solo e unicamente all'attore figlio che ha sollevato l'eccezione.
* `new AllForOneStrategy(maxRetries, timeWindow, decider);`
  * **Cosa fa:** Applica la decisione a tutti i figli del supervisore se uno di essi fallisce.
* `SupervisorStrategy.resume();`
  * **Cosa fa:** Ignora l'errore e passa al messaggio successivo. **Conserva intatto lo stato in memoria dell'attore.**
* `SupervisorStrategy.restart();`
  * **Cosa fa:** Distrugge l'istanza e richiama il costruttore. **Azzera lo stato in memoria**, ma preserva i messaggi pendenti nella mailbox.
* `SupervisorStrategy.stop();`
  * **Cosa fa:** Arresta definitivamente l'attore.

---

# PARTE 2: Template Completi delle Classi

## 1. I Messaggi

### `Msg.java` (Base / Generico)
```java
package lab;

import java.io.Serializable;

public class Msg implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String payload;

    public Msg(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
```

### `ConfigMsg.java` (Passaggio di Riferimenti)
```java
package lab;

import java.io.Serializable;
import akka.actor.ActorRef;

public class ConfigMsg implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ActorRef ref;

    public ConfigMsg(ActorRef ref) {
        this.ref = ref;
    }

    public ActorRef getRef() {
        return ref;
    }
}
```

---

## 2. Il Main Onnicomprensivo (`MasterMain.java`)

Mostra: creazione del sistema, supervisore, creazione del server come figlio supervisionato via `ask`, client, collegamento con `ConfigMsg`, invio con `tell` sincrono/asincrono e terminazione.

```java
package lab;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class MasterMain {

    public static void main(String[] args) {
        final ActorSystem sys = ActorSystem.create("MasterSystem");
        final Duration timeout = Duration.create(5, SECONDS);

        // 1. Creazione del Supervisore (Top-level)
        final ActorRef supervisor = sys.actorOf(SupervisorActor.props(), "supervisor");

        // 2. Creazione del Server come FIGLIO del Supervisore tramite ASK
        ActorRef server = null;
        Future<Object> futureServer = ask(supervisor, ServerActor.props(), 5000);
        try {
            server = (ActorRef) futureServer.result(timeout, null);
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Creazione del Client
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");

        // 4. Configurazione delle dipendenze a runtime
        client.tell(new ConfigMsg(server), ActorRef.noSender());

        // 5. Invio comandi asincroni (Tell)
        client.tell(new Msg("Primo task"), ActorRef.noSender());
        client.tell(new Msg("Secondo task"), ActorRef.noSender());

        // 6. Invio sincrono (Ask) direttamente dal Main
        Future<Object> queryFuture = ask(server, new Msg("Query diretta"), 5000);
        try {
            Msg reply = (Msg) queryFuture.result(timeout, null);
            System.out.println("MAIN: Risposta sincrona ricevuta: " + reply.getPayload());
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }

        // Attesa input per terminare
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();
    }
}
```

---

## 3. L'Attore Client (`ClientActor.java`)

Riceve configurazione, inoltra con `tell`, e usa `ask` con `Future` per interrogazioni sincrone.

```java
package lab;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ClientActor extends AbstractActor {

    private ActorRef server;
    private final Duration timeout = Duration.create(5, SECONDS);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMsg.class, this::onConfig)
                .match(Msg.class, this::onMsg)
                .build();
    }

    private void onConfig(ConfigMsg msg) {
        this.server = msg.getRef();
        System.out.println("CLIENT: Configurato con il server.");
    }

    private void onMsg(Msg msg) {
        System.out.println("CLIENT: Invio richiesta asincrona al server: " + msg.getPayload());
        server.tell(msg, self());

        // Esempio di interrogazione sincrona (Ask)
        Future<Object> future = ask(server, new Msg("Ask-Check"), 5000);
        try {
            Msg reply = (Msg) future.result(timeout, null);
            System.out.println("CLIENT: Risposta ricevuta: " + reply.getPayload());
        } catch (TimeoutException | InterruptedException e) {
            System.err.println("CLIENT: Timeout durante ask.");
        }
    }

    public static Props props() {
        return Props.create(ClientActor.class);
    }
}
```

---

## 4. L'Attore Server Completo (`ServerActor.java`)

Integra:
* `AbstractActorWithStash`
* Macchina a stati (`ready` e `busy`) con `become`
* `stash()` e `unstashAll()`
* Simulazione elaborazione asincrona non-bloccante con `scheduleOnce`
* Iniezione di guasto per verifica supervisore

```java
package lab;

import java.time.Duration;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ServerActor extends AbstractActorWithStash {

    private static final Duration DELAY = Duration.ofMillis(1000);
    private int processedCount = 0;

    // Messaggio interno usato come timer
    public static class InternalDoneMsg {
        private final String data;
        private final ActorRef requester;

        public InternalDoneMsg(String data, ActorRef requester) {
            this.data = data;
            this.requester = requester;
        }

        public String getData() { return data; }
        public ActorRef getRequester() { return requester; }
    }

    @Override
    public Receive createReceive() {
        return ready();
    }

    // STATO 1: PRONTO
    private Receive ready() {
        return receiveBuilder()
                .match(Msg.class, this::onMsgReady)
                .build();
    }

    // STATO 2: OCCUPATO (Bufferizza le nuove richieste nello Stash)
    private Receive busy() {
        return receiveBuilder()
                .match(Msg.class, this::onMsgBusy)
                .match(InternalDoneMsg.class, this::onDone)
                .build();
    }

    private void onMsgReady(Msg msg) throws Exception {
        // Controllo simulazione guasto
        if ("CRASH".equalsIgnoreCase(msg.getPayload())) {
            System.err.println("SERVER: Simulazione crash...");
            throw new Exception("Errore simulato nel Server!");
        }

        System.out.println("SERVER: Inizio elaborazione: " + msg.getPayload());

        // Salva il requester e imposta lo stato busy
        InternalDoneMsg done = new InternalDoneMsg(msg.getPayload(), sender());
        getContext().become(busy());

        // Pianifica il completamento asincrono senza bloccare il thread
        getContext().system().scheduler().scheduleOnce(
                DELAY,
                self(),
                done,
                getContext().dispatcher(),
                self());
    }

    private void onMsgBusy(Msg msg) {
        System.out.println("SERVER: Occupato! Accantono nello stash: " + msg.getPayload());
        stash();
    }

    private void onDone(InternalDoneMsg done) {
        processedCount++;
        System.out.println("SERVER: Lavoro completato (" + processedCount + "): " + done.getData());

        // Risposta al richiedente originario
        done.getRequester().tell(new Msg("REPLY: " + done.getData()), self());

        // Ripristino stato iniziale e svuotamento dello Stash
        getContext().become(ready());
        unstashAll();
    }

    public static Props props() {
        return Props.create(ServerActor.class);
    }
}
```

---

## 5. L'Attore Supervisore (`SupervisorActor.java`)

Integra:
* `OneForOneStrategy` con `resume()` o `restart()`
* Creazione dei figli nel proprio contesto (`getContext().actorOf`)
* Inoltro con `forward`

```java
package lab;

import java.time.Duration;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class SupervisorActor extends AbstractActor {

    // Strategia di supervisione
    private static final SupervisorStrategy STRATEGY = new OneForOneStrategy(
            5, // Massimo tentativi
            Duration.ofMinutes(1),
            DeciderBuilder
                    // RESUME: conserva lo stato in memoria
                    .match(Exception.class, e -> SupervisorStrategy.resume())
                    // RESTART: resetta lo stato (nuova istanza), preserva mailbox
                    // .match(Exception.class, e -> SupervisorStrategy.restart())
                    .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return STRATEGY;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // Creazione del figlio sotto il contesto del supervisore
                .match(Props.class, props -> {
                    ActorRef child = getContext().actorOf(props);
                    getSender().tell(child, getSelf());
                })
                .build();
    }

    public static Props props() {
        return Props.create(SupervisorActor.class);
    }
}
```

