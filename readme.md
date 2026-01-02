# **SenzMatica Device Management Module (DMS)**

**SenzMatica DMS** is the open-source Device Management Module of the SenzMatica AIoT platform by **SenzMate**.

This repository focuses on the **v1.0.0** open-source release scope: onboarding and managing devices, defining device blueprints, data transcoding, and connectivity credentials so devices can connect over **HTTP(S)** or **MQTT**.

Note: This repository contains **device management** capabilities. Other SenzMatica platform modules may be released separately.

---

## **What’s included in v1.0.0**

### **1\) Product Type Module**

Define reusable **device blueprints** ("product types") describing a class of devices and their capabilities (e.g., sensors, actuators).

Typical use:

* Create a product type for a device family (e.g., *Temperature Sensor v2*).  
* Define its expected telemetry / commands (as supported by the platform).

### **2\) Device Module**

Onboard and manage individual devices.

During onboarding you typically provide:

* **Product type** (the blueprint)  
* **Device ID** (e.g., **IMEI**)  
* **Device name**  
* **Data interval** (minutes)  
* **Batch number**

### **3\) Transcoding Module**

Manage **decoder** and **encoder** implementations (Java-based) to translate data:

* **Decoder**: Convert the incoming device payload (raw stream/message) into a structure SenzMatica understands.  
* **Encoder**: Convert SenzMatica-structured data into a target payload format when exporting data to external systems.

⚠️ **Security note:** Decoder/Encoder files are executable code. Only install code from trusted sources and follow your organization’s secure SDLC practices.

### **4\) Connectivity Module**

Provision device **credentials** and connection settings so devices can connect via:

* **HTTP(S)**  
* **MQTT** (by connecting to the MQTT server provided/managed by SenzMatica DMS)

---

## **Architecture (high-level)**

* **Devices** publish telemetry / receive commands over **HTTP(S)** or **MQTT**.  
* **Connectivity Module** issues and manages device credentials.  
* **Transcoding Module** transforms device payloads via Java-based decoders/encoders.  
* **Device Module** links device identities to a **Product Type** blueprint and applies device-level settings.

---

## **Repositories**

This open-source Device Management Module is split into two main repositories:

* **Backend (Core services)**  
  [https://github.com/senzmatica/magma-core](https://github.com/senzmatica/magma-core)  
* **Frontend (Device Management Portal)**  
  [https://github.com/senzmatica/magma-device-management-portal](https://github.com/senzmatica/magma-device-management-portal)

Both repositories together provide the full Device Management experience.

---

## **Getting started**

This section summarizes local / on‑prem deployment. A **detailed, step‑by‑step guide** is available in the Deployment Guide.

### **Supported environment**

* **OS**: CentOS 9 / Rocky Linux 9 (recommended)  
* **Java**: OpenJDK 8  
* **Node.js**: v16.x  
* **Database**: MongoDB 5.0  
* **MQTT Broker**: VerneMQ 1.12.x  
* **Web server**: Nginx

---

### **High‑level setup flow**

1. Prepare OS user and permissions  
2. Install runtime dependencies (Java, Maven, Node.js)  
3. Install and configure MongoDB  
4. Install and configure VerneMQ (MQTT broker)  
5. Build and run **magma‑core** (backend)  
6. Build and deploy **magma‑device‑management‑portal** (frontend)  
7. Configure firewall and network access

---

### **Detailed deployment guide**

For complete installation commands, configuration files, and production notes, see:

* **SenzMatica Open Source Deployment Guide (CentOS 9\)** — see [Open source - Deployment Guide.docx](https://docs.google.com/document/d/1uUHtqOBjABQ-zD3hEzqJ0voEMQrcPzZqVCbyiyE50d8/edit?usp=sharing)

### **Get started Guide**

For information on getting started with SenzMatica, see:

* **Open source - Get started Guide** — see [Open source - Get started Guide.docx](https://docs.google.com/document/d/14wGirrL1gJyTOsKrH9mQ0flByr0iCMMRZAhLusp7fU8/edit?usp=sharing)

---

## **Device connectivity**

### **MQTT**

Devices can connect to the **SenzMatica DMS MQTT broker** using the credentials created in the **Connectivity Module**.

Typical flow:

1. Create credentials for the device (username/password, token, certificate, etc. depending on configuration).  
2. Configure the device with the broker URL \+ credentials.  
3. Publish telemetry messages to the topic pattern used by your deployment.

### **HTTP(S)**

Devices can send telemetry via HTTPS using credentials created in the **Connectivity Module**.

Typical flow:

1. Create credentials for the device.  
2. Configure the device to POST/PUT to the ingestion endpoint.  
3. Payload is decoded via the assigned decoder.

**Endpoints, topic patterns, and auth mechanisms** are deployment/configuration dependent. See your repository’s configuration files and module docs for the exact values.

---

## **Transcoding (decoder / encoder)**

### **Decoder**

A **decoder** converts raw device data into a standardized internal structure.

Suggested best practices:

* Validate schema and required fields.  
* Handle versioning (device firmware revisions).  
* Fail safely (reject malformed payloads with clear error messages).  
* Avoid expensive operations per message.

### **Encoder**

An **encoder** converts internal structured data into the format required by an external platform.

Suggested best practices:

* Keep mappings explicit and documented.  
* Make output deterministic.  
* Include test cases for sample inputs/outputs.

**Security warning:** Treat decoder/encoder code as plugins with the same risk profile as running third-party code. Review, scan, and sign artifacts if your org requires it.

---

## **Configuration**

Configuration typically includes:

* Broker settings (host, port, TLS, topic permissions)  
* HTTP(S) ingestion settings  
* Database connection (if used)  
* Credential policy (token expiry, rotation, allowed auth types)  
* Module-level feature flags

Check these common locations:

* `application.yml` / `application.properties`  
* `.env`  
* `docker-compose.yml`  
* Helm charts / Kubernetes manifests (if present)

---

## **Contributing**

We welcome contributions\!

1. Fork the repo and create your branch: `git checkout -b feature/my-change`  
2. Commit your changes: `git commit -m "Add my change"`  
3. Push to the branch: `git push origin feature/my-change`  
4. Open a Pull Request

Please:

* Write tests where applicable  
* Keep changes focused and well documented  
* Follow the project’s Code of Conduct

---

## **Code of Conduct**

This project follows the **CNCF Code of Conduct**. Please read `CODE_OF_CONDUCT.md` before participating.

To report a violation:

* Email: **info@senzmate.com**

---

## **Security**

If you discover a security vulnerability, please **do not** open a public issue.

Instead, report it privately to: **info@senzmate.com**

---

## **License**

SenzMatica DMS is licensed under the **Apache License 2.0**. See `LICENSE` for details.

---

## **Trademark notice**

**SenzMate** and **SenzMatica** may be trademarks or registered trademarks of SenzMate Pvt Ltd. This project’s license does not grant permission to use trademarks except as required for reasonable and customary use in describing the origin of the work.

---

## **Maintainers**

Maintained by the SenzMate team and the open-source community.

If you have questions or want to get involved, open a GitHub Discussion/Issue (as appropriate) or contact **info@senzmate.com**.

