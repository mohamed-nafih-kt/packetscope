## 🛰️ Project Objective: PacketScope

**PacketScope** is a high-performance, distributed network analysis suite designed to provide real-time visibility into local and remote network traffic. Unlike standard "all-in-one" apps, PacketScope uses a **decoupled, systems-level architecture** to separate the high-stakes task of packet capture from the resource-intensive task of data visualization.

### 🎯 Core Aims

- **Low-Level Capture Engine:** To build a lightweight Desktop Agent (Java 21/JavaFX) that interacts directly with the OS network stack via **Pcap4J** and **libpcap** to sniff raw Ethernet frames.
- **System Architecture over Frameworks:** To demonstrate mastery of **Pure Java** fundamentals by manually handling concurrency, socket programming, and database connectivity (JDBC), intentionally avoiding high-level abstractions like Spring or Hibernate.
- **Distributed Telemetry:** To implement a persistent storage pipeline where captured packet metadata is transformed from binary to structured data and stored in a **MySQL** database for historical analysis.
- **Interactive Web Dashboard:** To provide a decoupled Web Interface (React/Java `HttpServer`) that allows users to analyze traffic remotely and perform **Manual Packet Injection** via a custom Request Composer.

---

### 🛠️ Technical Pillars (What you're proving)

1. **Concurrency:** Managing the "Producer-Consumer" problem where one thread captures packets while another updates the UI and a third writes to the DB.
2. **Protocol Dissection:** Manually parsing Hex/Binary data into readable IPv4/IPv6, TCP, and UDP structures.
3. **Resource Efficiency:** Building an "Enterprise-grade" tool with a minimal footprint by using native Java components (`HttpServer`, `Properties`, `Record` classes).

---

### 📝 The "Resume Summary"

> _"Developed **PacketScope**, a modular network analysis tool using Java 21. Engineered a multi-threaded desktop capture agent utilizing Pcap4J for raw socket access and a custom lightweight web backend using native Java HttpServer. Focused on bit-level data transformation, manual JDBC resource management, and decoupled systems architecture to provide a high-performance alternative to framework-heavy monitoring solutions."_

---

## 🛰️ Project Objective: PacketScope

**PacketScope** is a high-performance, distributed network analysis suite designed to provide real-time visibility into local and remote network traffic. Unlike standard "all-in-one" apps, PacketScope uses a **decoupled, systems-level architecture** to separate the high-stakes task of packet capture from the resource-intensive task of data visualization.

### 🎯 Core Aims

- **Low-Level Capture Engine:** To build a lightweight Desktop Agent (Java 21/JavaFX) that interacts directly with the OS network stack via **Pcap4J** and **libpcap** to sniff raw Ethernet frames.
- **System Architecture over Frameworks:** To demonstrate mastery of **Pure Java** fundamentals by manually handling concurrency, socket programming, and database connectivity (JDBC), intentionally avoiding high-level abstractions like Spring or Hibernate.
- **Distributed Telemetry:** To implement a persistent storage pipeline where captured packet metadata is transformed from binary to structured data and stored in a **MySQL** database for historical analysis.
- **Interactive Web Dashboard:** To provide a decoupled Web Interface (React/Java `HttpServer`) that allows users to analyze traffic remotely and perform **Manual Packet Injection** via a custom Request Composer.

---

### 🛠️ Technical Pillars (What you're proving)

1. **Concurrency:** Managing the "Producer-Consumer" problem where one thread captures packets while another updates the UI and a third writes to the DB.
2. **Protocol Dissection:** Manually parsing Hex/Binary data into readable IPv4/IPv6, TCP, and UDP structures.
3. **Resource Efficiency:** Building an "Enterprise-grade" tool with a minimal footprint by using native Java components (`HttpServer`, `Properties`, `Record` classes).

---

### 📝 The "Resume Summary" (Copy this!)

> _"Developed **PacketScope**, a modular network analysis tool using Java 21. Engineered a multi-threaded desktop capture agent utilizing Pcap4J for raw socket access and a custom lightweight web backend using native Java HttpServer. Focused on bit-level data transformation, manual JDBC resource management, and decoupled systems architecture to provide a high-performance alternative to framework-heavy monitoring solutions."_

---

# PacketScope

PacketScope is a two-part network observability project consisting of:

- **PacketScope Desktop**  
  A JavaFX-based desktop agent that captures live network traffic using libpcap (via pcap4j),
  decodes packets, and persists them locally.

- **PacketScope Web**  
  A web application that visualizes captured packets, flows, talkers, and timelines,
  providing protocol semantics and replay-oriented observability.

## Architecture Overview

```
NIC
↓
libpcap / Npcap (installed via Wireshark)
↓
pcap4j
↓
PacketScope Desktop (capture + persistence)
↓
Database
↓
PacketScope Web (analysis + visualization)
```

## Requirements

- Java 17+
- libpcap / Npcap  
  (Installing Wireshark satisfies this dependency)

## Notes

This is a hobby / learning project focused on network observability and protocol analysis,
not a production-grade packet capture engine.
