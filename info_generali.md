# Networked Software for Distributed Systems (NSDS)
**Politecnico di Milano**  
**Docenti:** Prof. Luca Mottola (`luca.mottola@polimi.it`), Prof. Alessandro Margara (`alessandro.margara@polimi.it`)

---

## 1. Obiettivo del Corso
Il corso insegna i concetti e le astrazioni fondamentali per lo sviluppo di software in ambienti distribuiti e di rete (dal Cloud al Computing Continuum e all'Internet of Things), attraverso l'uso pratico di sei tecnologie cardine dello stato dell'arte.

---

## 2. Struttura Didattica

Il corso segue un modello fortemente orientato alla pratica:
1. **Video Lectures (Online / YouTube):**
   - Vengono rilasciate settimanalmente su YouTube secondo il calendario.
   - Devono essere visionate autonomamente **prima** della prima sessione di laboratorio della relativa tecnologia.
2. **Lab Work (In presenza in aula):**
   - Sessioni pratiche di programmazione assistita (2–3 ore).
   - I testi degli esercizi vengono forniti all'inizio e le soluzioni ufficiali sono rilasciate subito dopo la sessione.
3. **Evaluation Labs (In presenza in aula):**
   - Prove pratiche valutate a computer (durata: 3 ore).
   - Lavoro in **gruppi da esattamente 3 studenti**, formati **casualmente** dal docente prima di ogni tecnologia (il gruppo cambia per ogni tecnologia).
   - Consegna del codice tramite **WeBeep**. Domande mirate di verifica possono essere poste a campione dai docenti.

---

## 3. Modalità d'Esame e Valutazione

L'esame standard è basato sulla somma dei punteggi ottenuti nei **6 Evaluation Lab**:

| Modulo / Tecnologia | Paradigma / Area | Punti Max |
| :--- | :--- | :---: |
| **1. Akka** | Actor Model | 5 pt |
| **2. Apache Kafka** | Distributed Message Queuing | 5 pt |
| **3. Apache Spark** | Big Data Processing | 5 pt |
| **4. Node-RED** | Computing Continuum / IoT Flows | 6 pt |
| **5. MPI** | Parallel & Distributed Computing (HPC) | 6 pt |
| **6. Contiki-NG** | Internet of Things (Embedded / WSN) | 6 pt |
| **TOTALE** | | **33 pt** |

- **Lode:** assegnata con punteggio complessivo $\ge 31.5$.
- **Opzione Esame Orale:**
  - È sempre possibile passare all'esame orale (individuale su tutte le 6 tecnologie: teoria + piccoli esercizi di codice), ma si perdono i punti accumulati nei laboratori.
  - Se si saltano 1 o 2 Evaluation Lab: si può recuperare sostenendo un esame orale per quei soli lab.
  - Se si saltano $\ge 3$ Evaluation Lab: si passa automaticamente all'esame orale completo.
  - Se non si è soddisfatti del voto di al massimo 1 Evaluation Lab: è possibile fare l'orale integrativo per quel singolo modulo.

---

## 4. Calendario Indicativo dei Moduli e degli Evaluation Lab

> *Nota: fare sempre riferimento al foglio del calendario aggiornato per eventuali variazioni di aula e orari.*

| Settimana | Tecnologia | Attività Principali | Data Evaluation Lab |
| :--- | :--- | :--- | :--- |
| **W1** | **Akka** | Video online + Lab 1 & 2 in aula | **9 Novembre** (3 ore) |
| **W2** | **Apache Kafka** | Video online + Lab in aula | **16 Novembre** (3 ore) |
| **W3** | **Node-RED** | Video online + Lab in aula | **23 Novembre** (3 ore) |
| **W4** | **Apache Spark** | Video online + Lab in aula | **1 Dicembre** (3 ore) |
| **W5 & W7**| **MPI** | Video online + Lab in aula | **14 Dicembre** (3 ore) |
| **W6 & W8**| **IoT (Contiki-NG)**| Video online + Lab in aula | **21 Dicembre** (3 ore) |

---

## 5. Focus Modulo 1: Akka (Attuale)

- **Docente di riferimento:** Prof. Luca Mottola
- **Punteggio Lab:** 5 punti
- **Materiali e Lezioni:**
  - **Lecture 1.1 (Teoria):** [YouTube Link](https://youtu.be/EcvRwDGPkOY) | [Slide Dropbox](https://www.dropbox.com/scl/fi/gwz05c31dnt5ktu3s56pq/1-Actors-and-Akka.pdf?rlkey=fsse5vksumk3xx2gappac5aux&dl=0)
  - **Lecture 1.1 (Coding):** [YouTube Link](https://youtu.be/qHGPQjTu4kI)
  - **Lecture 1.2 (Teoria):** [YouTube Link](https://youtu.be/AZl-HmtnKog) | [Slide Dropbox](https://www.dropbox.com/scl/fi/c83tvficmqx5xstsrbhp8/2-Actors-and-Akka.pdf?rlkey=uy6e4qdw5e0cfw1m1gpzuoz3p&dl=0)
  - **Lecture 1.3 (Teoria):** [YouTube Link](https://youtu.be/AZl-HmtnKog) | [Slide Dropbox](https://www.dropbox.com/scl/fi/mjeq2ti2yr70i311r0www/3-Actors-and-Akka.pdf?rlkey=2h6td3j7o7gjot1qpcd5lxbdx&dl=0)
  - **Lecture 1.3 (Coding):** [YouTube Link](https://youtu.be/YUG0UWl5bl0)
- **Esercitazioni e Prove d'Esame degli anni precedenti:**
  - [Lab 1 Testo](https://www.dropbox.com/scl/fi/i954a7g8abf95q7s9cj9k/Actors-and-Akka-Lab-1-of-2.pdf?rlkey=vgudthfg6ehk0yyl86rdurnh6&dl=0) | [Lab 1 Soluzioni](https://www.dropbox.com/scl/fi/8v46337cixoe0bjdoix1y/labSolutions-1-of-2.zip?rlkey=sqqk1g25hi9xtoi86ov76173b&dl=0)
  - [Lab 2 Testo](https://www.dropbox.com/scl/fi/hphgug0rq5qvna6a3f1q5/Actors-and-Akka-Lab-2-of-2.pdf?rlkey=x0k8ddrz4car2xdisuytrqpt8&dl=0) | [Lab 2 Soluzioni](https://www.dropbox.com/scl/fi/99w5k7t2egm9chrc8q2cv/labSolutions-2-of-2.zip?rlkey=pfme9imgm28dulzo3vkfmftu4&dl=0)
  - [Evaluation Lab 2022 (Testo + Codice)](https://www.dropbox.com/scl/fi/ojd1w5i1p6cbtq0vg26s9/evaluation22.zip?rlkey=w2wbk7zzev4citafpyo0cojux&dl=0)
  - [Evaluation Lab 2023 (Testo + Codice)](https://www.dropbox.com/scl/fi/5u5q8lczoeltrqgeecl3o/evaluation23.zip?rlkey=qa1kmrc329aon8c8znh58odet&dl=0)

---

## 6. Link Utili e Canali di Riferimento

- **Calendario Quest'Anno (Ufficiale e Aggiornato):** [Google Spreadsheet](https://docs.google.com/spreadsheets/d/1WtYCLEQ_3vum9dZtbDNKw9WSlM_v4uAsYfmhBuDQBJU/edit?usp=sharing)
- **Calendario Anno Scorso (Archivio di riferimento):** [Google Spreadsheet 2023/2024](https://docs.google.com/spreadsheets/d/1-VuzqgQQFVxxmxi4B1kgBdkabwUfBQ9yAAh4dTNssy4/edit?gid=1577429949#gid=1577429949)
- **Piattaforma del Corso:** WeBeep (per annunci ufficiali, consegna compiti ed eventuali modifiche orari)
- **Slide Introduttive Ufficiali:** [Introduction.pdf](https://www.dropbox.com/scl/fi/zv2m583icxvn00o9zv310/Introduction.pdf?rlkey=qhz1dxcckffre1mh1vnvkq3qm&dl=0)

