# Networked Software for Distributed Systems (NSDS)

**Politecnico di Milano**  
**Master of Science in Computer Science and Engineering**  
**Academic Year:** 2026/2027  
**Author:** Riccardo Nasatti  
**Instructors:** Prof. Luca Mottola, Prof. Alessandro Margara  

---

## Course Overview

This repository contains lecture notes, comprehensive study guides, laboratory exercises, and evaluation lab projects for the graduate course **Networked Software for Distributed Systems (NSDS)** at Politecnico di Milano.

The course investigates modern paradigms, architectural abstractions, and programming models for distributed computing infrastructures spanning the entire cloud-to-edge continuum.

### Key Technologies & Syllabus

1. **Actor Model (Akka & Java):** Message-driven concurrency, state encapsulation, finite state machines (`become`/`unbecome`), stashing, fault tolerance with supervision trees (`OneForOneStrategy`, `Resume`, `Restart`), remoting, and decentralized clustering (gossip protocols and $\Phi$-accrual failure detection).
2. **Distributed Message Queues (Apache Kafka):** High-throughput distributed commit logs, partitions, consumer groups, delivery semantics, and streaming architectures.
3. **Flow-Based Computing & Edge Continuum (Node-RED):** Event-driven flow programming, asynchronous messaging, UDP/MQTT integration across edge gateways.
4. **Big Data Processing (Apache Spark):** Resilient Distributed Datasets (RDDs), Spark SQL DataFrames, structured streaming, transformations, and actions.
5. **High-Performance Parallel Computing (MPI):** Distributed memory communication, point-to-point non-blocking primitives, collective communication patterns, and topology virtualizations.
6. **Internet of Things & Embedded Software (Contiki-NG):** Protothreads, low-power wireless networking, RPL routing, sensor simulation (COOJA), and constrained device protocols.

---

## Repository Structure

```text
├── 1_Akka/
│   ├── code/                  # Java/Akka source code & Maven project
│   │   └── src/main/java/lab/ # Laboratory implementations (ex1a, exe2, exe3)
│   ├── slides/                # Official lecture slide decks
│   ├── labs/                  # Laboratory specifications and solutions
│   ├── evaluations/           # Evaluation lab assignments & solutions (2022, 2023)
│   └── Guida_Codice_Akka.md   # Practical code reference and architectural patterns
├── LaTex/                     # Comprehensive LaTeX textbook sources
│   ├── chapters/              # English textbook chapters
│   ├── capitoli/              # Italian textbook chapters
│   ├── img/                   # Architectural schemas and slide diagrams
│   ├── main.tex               # English chapter compiler
│   ├── main_it.tex            # Italian chapter compiler
│   └── main_tmp.tex           # Full book compiler (with title page & TOC)
├── Libro/                     # Compiled PDF course notes
│   ├── Capitolo_01_Akka_IT.pdf
│   ├── Chapter_01_Akka_EN.pdf
│   └── NSDS_Libro_Completo.pdf
└── README.md
```

---

## Development Environment & Prerequisites

* **Java Development Kit:** Java 21 LTS (or higher)
* **Build System:** Apache Maven 3.9+
* **IDE:** Eclipse IDE for Java Developers (with m2e plugin) or IntelliJ IDEA
* **Document Typesetting:** TeX Live / pdfLaTeX with standard academic packages (`amsmath`, `listings`, `tabularx`, `tcolorbox`, `fancyhdr`)

---

## Laboratory Modules

### Module 1: Akka
* **Exercise 1 (`lab.ex1a`):** Concurrency isolation without locks using `IncrementMessage` and `DecrementMessage`.
* **Exercise 2 (`lab.exe2`):** Non-negative state evolution using `AbstractActorWithStash`, `stash()`, and `unstashAll()`.
* **Exercise 3 (`lab.exe3`):** Asynchronous Client-Server Address Book architecture with immutable message protocols (`PutMsg`, `GetMsg`, `ReplyMsg`) and `getSender().tell()` reply semantics.

---

## License

This repository is maintained for academic and educational purposes at Politecnico di Milano.

