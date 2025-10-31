# 🩺 Ensuring Security in Cyber-Physical Systems for Healthcare Applications

This project implements a **secure Cyber-Physical System (CPS)** for healthcare applications that ensures reliable, encrypted, and authenticated communication between **IoT edge nodes (ESP32)** and a **Spring Boot–PostgreSQL backend** deployed on **Render Cloud**.  
The system focuses on **real-time heart-rate monitoring** using the **MAX30102 sensor**, integrated with **Cipher-based encryption (MAC + Nonce)** and **DDoS attack detection** for enhanced data security and availability.

---

## 👥 Collaborators
**Abhinav Anpan** · **Atharva Bomle** · **Devansh Dhopte** · **Om Telrandhe** · **Parth Wankar**  

Guided by **Dr. Rakesh Kadu**, RCOEM Nagpur  

---

## ⚙️ Project Overview
This project enhances CPS edge node security in healthcare by integrating **IoT, embedded systems, and cybersecurity techniques**.  
It enables **real-time data acquisition**, **secure transmission**, and **intelligent attack prevention** through encryption and source validation.

### 🔑 Core Features
- **Cipher-based Encryption (MAC + Nonce):** Secures ESP32–server communication and prevents data interception.  
- **DDoS Detection & Prevention:** Filters unauthorized or dummy requests, validating only legitimate ESP32 sources.  
- **Real-time Data Transmission:** Streams patient heart-rate readings from **MAX30102 sensor** to the cloud with <2s latency.  
- **Cloud Integration:** Backend and database hosted on **Render**, providing 99% uptime and consistent performance.  

---

## 🧩 System Architecture
1. **ESP32** collects heart-rate data from **MAX30102 sensor** using I2C communication.  
2. Data is encrypted via a **Cipher (MAC + Nonce)** and transmitted securely over **HTTP POST**.  
3. The **Spring Boot REST API** (deployed on Render) authenticates and decrypts incoming data.  
4. **PostgreSQL** stores validated readings, while the system blocks unauthorized or repetitive requests as part of **DDoS prevention**.

---

## 🧠 Tech Stack
**Hardware:** ESP32 · MAX30102  
**Backend:** Spring Boot · Java · PostgreSQL  
**Cloud:** Render  
**Protocols:** HTTP · Wi-Fi  
**Security:** Cipher Encryption · MAC + Nonce Authentication · DDoS Detection  
**Tools:** Arduino IDE · HTTPClient · JSON  

---

## 🔧 Setup & Deployment

### 1. Clone the Repository
```bash
git clone https://github.com/Abhinavan2004/Ensuring-Security-in-CPS-for-Healthcare-Applications.git
### 2. Configure Backend

- Open the **Spring Boot** project and update your **PostgreSQL credentials** in `application.properties`.  
- Deploy the backend to **Render Cloud** and note your API endpoint (e.g.,  
  `https://cps-backend-07r0.onrender.com/patientData/postPatientData`).  

---

### 3. Setup ESP32

- Open the project code in **Arduino IDE**.  
- Install the following libraries:
  - `WiFi.h`  
  - `HTTPClient.h`  
  - `MAX30105.h`  
  - `heartRate.h`  
- Update your **Wi-Fi credentials** and **Render endpoint** in the ESP32 code.  
- Upload the code to the board and monitor **serial output** for live heart-rate readings.  

---

### 4. Verify

- Confirm successful **HTTP requests** from ESP32 in the Serial Monitor.  
- Check **PostgreSQL entries** in your Render dashboard to verify real-time data logging.  

---

## 🔐 Security Highlights

- End-to-end encryption using custom **Cipher (MAC + Nonce)**.  
- **Device-level authentication** ensuring data is accepted only from verified ESP32 nodes.  
- **DDoS mitigation** by filtering repetitive or malformed requests.  
- Maintains **confidentiality**, **integrity**, and **availability** of patient data.  

---

## 📈 Results

| Metric | Result |
|--------|---------|
| Secure Data Transmission | **99% Success Rate** |
| Average Latency | **1.7 seconds** |
| Attack Detection Accuracy | **~96%** |
| Cloud Uptime | **99% (Render)** |

---

## 🙌 Acknowledgments

- Department of Information Technology, **RCOEM, Nagpur**  
- **Dr. Rakesh Kadu**, Project Guide  
- **ESP32** and **Spring Boot Developer Communities**  
- **Render Cloud** for reliable deployment infrastructure  

---
