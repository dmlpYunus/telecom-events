# 📡 Argela Telecom Events Platform

Bu proje, bir telekomünikasyon sağlayıcısı için **gerçek zamanlı event işleme** sistemidir.
Spring Boot, Kafka, Redis ve MySQL kullanılarak geliştirilmiştir.
Amaç, abonelerin çağrı, SMS veya veri kullanımına dair event’leri toplamak, işlemden geçirmek, saklamak ve istatistiksel olarak analiz etmektir.

---

## ⚙️ Mimari Bileşenler

| Katman    | Teknoloji              | Açıklama                                      |
| --------- | ---------------------- | --------------------------------------------- |
| API       | Spring Boot (Java 17+) | Event’leri REST endpoint üzerinden alır       |
| Queue     | Apache Kafka           | Event’leri kuyruklayıp tüketiciye iletir      |
| Cache     | Redis                  | Anlık istatistikleri ve son event’leri saklar |
| DB        | MySQL                  | Kalıcı veri saklama (event kayıtları)         |
| Container | Docker Compose         | Tüm bileşenleri tek ortamda çalıştırır        |

---


## Kullanılan Teknolojiler ve Seçim Nedenleri

1. **Spring Boot 3.2.0**
   - Modern Java framework'ü
   - Otomatik konfigürasyon ve dependency injection
   - Production-ready özellikler

2. **Apache Kafka**
   - Yüksek throughput ve düşük latency
   - Event streaming için ideal
   - Scalable ve distributed yapı

3. **Redis**
   - Hızlı cache katmanı
   - Son event'lerin hızlı erişimi için
   - TTL desteği ile otomatik expire

4. **MySQL**
   - Kalıcı veri saklama
   - İlişkisel veri yapısı
   - JPA/Hibernate ile kolay entegrasyon

5. **Lombok**
   - Boilerplate kod azaltma
   - Kod okunabilirliğini artırma

6. **Kafka UI**
   - Kafka topic'lerini, mesajları ve consumer group'ları görsel olarak izleme
   - Real-time mesaj akışını gözlemleme
   - Partition dağılımını ve offset durumunu görüntüleme
   - Debugging ve monitoring için kullanıcı dostu arayüz

7. **Redis Insight**
   - Redis key'lerini ve değerlerini görsel olarak yönetme
   - Cache içeriğini gerçek zamanlı izleme
   - İstatistik bucket'larını ve pattern'leri analiz etme
   - Redis verilerini kolayca keşfetme ve debug etme


## Proje Yapısı
   telecom-events/
├── src/
│   ├── main/
│   │   ├── java/com/argela/telecom_events/
│   │   │   ├── api/          # REST Controller'lar
│   │   │   ├── config/       # Kafka, Redis konfigürasyonları
│   │   │   ├── domain/       # Entity ve Repository'ler
│   │   │   ├── service/      # Business logic
│   │   │   └── web/dto/      # Response DTO'ları
│   │   └── resources/
│   │       └── application.properties
│   └── test/                 # Test dosyaları
├── docker-compose.yml
└── pom.xml


## 🚀 Kurulum Rehberi

Bu bölüm, projeyi başka bir makinede çalıştırmak için gerekli yapılandırma dosyalarını oluşturma adımlarını içermektedir.

**Ön Gereksinimler:**
- Docker Desktop kurulu ve çalışıyor olmalı
- Java JDK 17+ kurulu olmalı
- Maven 3.9+ kurulu olmalı

---

### 📋 1. Docker Compose Dosyası Oluşturma

Proje için gerekli tüm servisleri (MySQL, Kafka, Redis vb.) çalıştırmak üzere `docker-compose.yml` dosyasını oluşturmanız gerekmektedir.

#### 1.1. Docker Compose Dosyasını Oluşturun

`telecom-events/docker-compose.yml` dosyasını oluşturun ve aşağıdaki içeriği ekleyin:

```yaml
services:
  # Zookeeper - Kafka için koordinasyon servisi
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.1
    container_name: telecom-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  # Kafka - Event streaming platformu
  kafka:
    image: confluentinc/cp-kafka:7.6.1
    container_name: telecom-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"  # ⚠️ PORT ÇAKIŞMASI VARSA DEĞİŞTİRİN (örn: "9093:9092")
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:19092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,PLAINTEXT_INTERNAL://0.0.0.0:19092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  # Kafka UI - Kafka yönetim arayüzü
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    ports:
      - "8081:8080"  # ⚠️ PORT ÇAKIŞMASI VARSA DEĞİŞTİRİN (örn: "8082:8080")
    environment:
      KAFKA_CLUSTERS_0_NAME: telecom
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:19092
    depends_on:
      - kafka

  # MySQL - Veritabanı
  mysql:
    image: mysql:8.0
    container_name: telecom-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root          # ⚠️ KENDİ ROOT ŞİFRENİZİ GİRİN
      MYSQL_DATABASE: telecom
      MYSQL_USER: telecom                 # ⚠️ İSTERSENİZ KULLANICI ADINI DEĞİŞTİREBİLİRSİNİZ
      MYSQL_PASSWORD: telecom            # ⚠️ KENDİ ŞİFRENİZİ GİRİN
    ports:
      - "3306:3306"                       # ⚠️ PORT ÇAKIŞMASI VARSA DEĞİŞTİRİN (örn: "3307:3306")
    command: --default-authentication-plugin=mysql_native_password

  # Redis - Cache ve istatistik deposu
  redis:
    image: redis:7-alpine
    container_name: telecom-redis
    ports:
      - "6379:6379"                       # ⚠️ PORT ÇAKIŞMASI VARSA DEĞİŞTİRİN (örn: "6380:6379")

  # Redis Insight - Redis yönetim arayüzü
  redis-insight:
    image: redis/redisinsight:latest
    container_name: redis-insight
    ports:
      - "5540:5540"                       # ⚠️ PORT ÇAKIŞMASI VARSA DEĞİŞTİRİN (örn: "5541:5540")
```

**Önemli Notlar:**

1. **MySQL Şifreleri:**
   - `MYSQL_ROOT_PASSWORD`: MySQL root kullanıcısı için güçlü bir şifre belirleyin
   - `MYSQL_USER` ve `MYSQL_PASSWORD`: Uygulama tarafından kullanılacak kullanıcı adı ve şifre
   - Bu değerleri not edin, çünkü `application.properties` dosyasında da kullanılacaklar

2. **Port Çakışmaları:**
   - Eğer sisteminizde bu portlar kullanılıyorsa, port numaralarını değiştirebilirsiniz
   - Port değişikliği yaparsanız, `application.properties` dosyasındaki ilgili port numaralarını da güncellemeyi unutmayın

3. **Servis Bağımlılıkları:**
   - Kafka, Zookeeper'a bağımlıdır (`depends_on: zookeeper`)
   - Kafka UI, Kafka'ya bağımlıdır (`depends_on: kafka`)
   - Diğer servisler bağımsız çalışabilir

#### 1.2. Servisler ve Portlar Özeti

| Servis | Port | Açıklama |
|--------|------|----------|
| **Zookeeper** | 2181 (internal) | Kafka koordinasyon servisi |
| **Kafka** | 9092 | Event streaming platformu |
| **Kafka UI** | 8081 | Kafka yönetim arayüzü (http://localhost:8081) |
| **MySQL** | 3306 | Veritabanı |
| **Redis** | 6379 | Cache ve istatistik deposu |
| **Redis Insight** | 5540 | Redis yönetim arayüzü (http://localhost:5540) |

---

### 📝 2. Spring Boot Yapılandırması

**Dosya:** `telecom-events/src/main/resources/application.properties`

Bu dosyada, `docker-compose.yml` dosyasında belirlediğiniz değerlerle uyumlu olacak şekilde yapılandırma yapmanız gerekmektedir.

#### 2.1. Application Properties Dosyasını Düzenleyin

```properties
server.port=8080                        # ⚠️ PORT ÇAKIŞMASI VARSA DEĞİŞTİRİN

# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/telecom?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=telecom      # ⚠️ docker-compose.yml'deki MYSQL_USER ile aynı olmalı
spring.datasource.password=telecom      # ⚠️ docker-compose.yml'deki MYSQL_PASSWORD ile aynı olmalı
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=true

spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10

# Kafka Producer 
spring.kafka.bootstrap-servers=localhost:9092  # ⚠️ docker-compose.yml'deki Kafka portu ile aynı olmalı
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Kafka Consumer
spring.kafka.consumer.bootstrap-servers=localhost:9092  # ⚠️ docker-compose.yml'deki Kafka portu ile aynı olmalı
spring.kafka.consumer.group-id=telecom-events-consumer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.listener.ack-mode=record

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379            # ⚠️ docker-compose.yml'deki Redis portu ile aynı olmalı

# App
management.endpoints.web.exposure.include=health,info
management.info.env.enabled=true
```

**Önemli Notlar:**

1. **MySQL Bağlantı Bilgileri:**
   - `spring.datasource.username` değeri, `docker-compose.yml` dosyasındaki `MYSQL_USER` ile **tam olarak aynı** olmalıdır
   - `spring.datasource.password` değeri, `docker-compose.yml` dosyasındaki `MYSQL_PASSWORD` ile **tam olarak aynı** olmalıdır
   - Eğer MySQL portunu değiştirdiyseniz, `spring.datasource.url` içindeki port numarasını da güncelleyin

2. **Kafka Bağlantı Bilgileri:**
   - `spring.kafka.bootstrap-servers` değeri, `docker-compose.yml` dosyasındaki Kafka portu ile uyumlu olmalıdır
   - Örnek: Eğer `docker-compose.yml`'de `"9093:9092"` kullandıysanız, burada `localhost:9093` yazmalısınız

3. **Redis Bağlantı Bilgileri:**
   - `spring.data.redis.port` değeri, `docker-compose.yml` dosyasındaki Redis portu ile uyumlu olmalıdır
   - Örnek: Eğer `docker-compose.yml`'de `"6380:6379"` kullandıysanız, burada `6380` yazmalısınız

4. **Port Uyumluluğu:**
   - Tüm port değişikliklerini hem `docker-compose.yml` hem de `application.properties` dosyalarında tutarlı bir şekilde yapın

---

### 🚀 3. Projeyi Başlatma

#### 3.1. Docker Servislerini Başlatma

1. **Terminal'i açın ve proje dizinine gidin:**
   ```bash
   cd telecom-events
   ```

2. **Docker Compose ile servisleri başlatın:**
   ```bash
   docker-compose up -d
   ```

3. **Container'ların durumunu kontrol edin:**
   ```bash
   docker-compose ps
   ```

   Tüm servislerin "Up" durumunda olduğunu görmelisiniz.

4. **Servislerin loglarını kontrol edin (opsiyonel):**
   ```bash
   docker-compose logs -f
   ```
   - Çıkmak için `Ctrl+C` tuşlarına basın

#### 3.2. Spring Boot Uygulamasını Başlatma

1. **Yeni bir terminal penceresi açın**

2. **Proje dizinine gidin:**
   ```bash
   cd telecom-events
   ```

3. **Maven ile projeyi derleyin:**
   ```bash
   mvn clean install
   ```

4. **Spring Boot uygulamasını başlatın:**
   ```bash
   mvn spring-boot:run
   ```

5. **Uygulamanın başarıyla başladığını doğrulayın:**
   - Konsolda şu logları görmelisiniz:
   ```
   Started TelecomEventsApplication in X.XXX seconds
   Tomcat started on port(s): 8080 (http)
   ```

---

### ✅ 4. Kurulum Doğrulama

#### 4.1. Servislerin Erişilebilirliğini Kontrol Etme

1. **Docker Container'ları:**
   ```bash
   docker ps
   ```
   - 6 container'ın çalıştığını görmelisiniz

2. **Spring Boot Uygulaması:**
   - Tarayıcınızda: http://localhost:8080/actuator/health
   - `{"status":"UP"}` yanıtını görmelisiniz

3. **Kafka UI:**
   - Tarayıcınızda: http://localhost:8081
   - Kafka UI arayüzü açılmalıdır

4. **Redis Insight:**
   - Tarayıcınızda: http://localhost:5540
   - Redis Insight arayüzü açılmalıdır

#### 4.2. İlk Test İsteği

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d "{\"subscriberId\":\"53200123\",\"type\":\"CALL_START\",\"timestamp\":\"2025-01-15T10:00:00Z\",\"details\":{\"calledNumber\":\"+905555555555\"}}"
```

- HTTP 202 (Accepted) yanıtı almalısınız

---

### 🔍 5. Kafka UI Kullanımı

#### 5.1. Erişim ve Yapılandırma

1. **Tarayıcınızda açın:** http://localhost:8081

2. **Kafka cluster otomatik olarak yapılandırılmış olmalıdır**
   - Sol menüden "Clusters" → "telecom" cluster'ını görmelisiniz

#### 5.2. Topic ve Mesajları İnceleme

1. **Sol menüden "Topics" sekmesine gidin**
   - `telecom.events` topic'ini seçin

2. **"Messages" sekmesine gidin**
   - "Load messages" butonuna tıklayın
   - Gönderilen event'lerin JSON formatında görüntülendiğini görmelisiniz
   - Her mesajın hangi partition'a atandığını görebilirsiniz

3. **"Partitions" sekmesinde:**
   - Her partition'ın offset ilerleyişini gözlemleyin
   - Farklı `subscriberId` değerlerinin farklı partition'lara atandığını görebilirsiniz

#### 5.3. Consumer Group'ları İzleme

1. **Sol menüden "Consumer Groups" sekmesine gidin**
   - `telecom-events-consumer` group'unu görmelisiniz
   - Group'a tıklayarak aktif consumer thread'lerini ve lag bilgisini görebilirsiniz

---

### 🔍 6. Redis Insight Kullanımı

#### 6.1. Erişim ve Bağlantı

1. **Tarayıcınızda açın:** http://localhost:5540

2. **İlk açılışta:**
   - "Add Redis Database" butonuna tıklayın
   - **Host:** `telecom-redis` (veya `localhost`)
   - **Port:** `6379`
   - **Database Alias:** `Telecom Events` (opsiyonel)
   - "Add Redis Database" butonuna tıklayın

#### 6.2. Redis Key'lerini Görüntüleme

1. **Sol menüden "Browser" sekmesine gidin**

2. **Key listesi:**
   - Redis'teki tüm key'leri görebilirsiniz
   - Key pattern'leri:
     - `subscriber:*:last` - Her abonenin son event'leri
     - `stats:minute:*` - Dakika bazlı istatistik bucket'ları

3. **Key detaylarını görüntüleme:**
   - Bir key'e tıklayarak içeriğini görüntüleyin
   - **List** tipindeki key'ler için tüm elemanları görebilirsiniz
   - **Hash** tipindeki key'ler için tüm field'ları görebilirsiniz

#### 6.3. İstatistikleri İnceleme

1. **Stats key'lerini bulun:**
   - `stats:minute:*` pattern'ini kullanarak arama yapın

2. **İstatistik bucket'ları:**
   - Her dakika için ayrı bir bucket oluşturulur
   - Format: `stats:minute:<epochMinute>:total` ve `stats:minute:<epochMinute>:types`

3. **Real-time monitoring:**
   - Yeni event'ler gönderildikçe Redis'teki değerlerin güncellendiğini gözlemleyin

---

### 🛠️ 7. Sorun Giderme

#### 7.1. Port Çakışması

**Sorun:** Container'lar başlamıyor, port zaten kullanılıyor

**Çözüm:**
1. Hangi portun kullanıldığını kontrol edin:
   ```bash
   # Windows
   netstat -ano | findstr :3306
   
   # Linux/Mac
   lsof -i :3306
   ```

2. `docker-compose.yml` dosyasında port numarasını değiştirin
3. `application.properties` dosyasında ilgili port numarasını güncelleyin

#### 7.2. MySQL Bağlantı Hatası

**Sorun:** Spring Boot uygulaması MySQL'e bağlanamıyor

**Çözüm:**
1. MySQL container'ının çalıştığını kontrol edin:
   ```bash
   docker ps | grep mysql
   ```

2. `docker-compose.yml` ve `application.properties` dosyalarındaki kullanıcı adı ve şifrelerin **tam olarak aynı** olduğundan emin olun

3. MySQL'e manuel bağlantı testi:
   ```bash
   docker exec -it telecom-mysql mysql -u telecom -ptelecom
   ```

#### 7.3. Kafka/Redis Bağlantı Hatası

**Sorun:** Spring Boot uygulaması Kafka veya Redis'e bağlanamıyor

**Çözüm:**
1. Container'ların çalıştığını kontrol edin:
   ```bash
   docker ps
   ```

2. `application.properties` dosyasındaki port numaralarının `docker-compose.yml` ile uyumlu olduğundan emin olun

---

### 🛑 8. Projeyi Durdurma

#### 8.1. Spring Boot Uygulamasını Durdurma

- Çalışan terminal penceresinde `Ctrl+C` tuşlarına basın

#### 8.2. Docker Container'ları Durdurma

```bash
cd telecom-events
docker-compose down
```

**Not:** Veriler MySQL ve Redis'te kalıcı olarak saklanır (volume'ler korunur).

#### 8.3. Tüm Verileri Silme (Temiz Kurulum)

```bash
docker-compose down -v
```

Bu komut volume'leri de siler, yani MySQL ve Redis'teki tüm veriler kaybolur.

---

### 📝 9. Hızlı Başlangıç Özeti

1. **docker-compose.yml dosyasını oluştur**
   - `telecom-events/docker-compose.yml` dosyasını oluşturun
   - MySQL şifrelerini ve portları kendi ortamınıza göre ayarlayın

2. **application.properties dosyasını düzenle**
   - MySQL kullanıcı adı/şifre docker-compose.yml ile uyumlu olmalı
   - Portlar docker-compose.yml ile uyumlu olmalı

3. **Docker servislerini başlat:**
   ```bash
   cd telecom-events
   docker-compose up -d
   ```

4. **Spring Boot uygulamasını başlat (yeni terminal):**
   ```bash
   cd telecom-events
   mvn spring-boot:run
   ```

5. **Servisleri kontrol et:**
   ```bash
   docker-compose ps
   ```

**Erişim Adresleri:**
- Spring Boot API: http://localhost:8080
- Kafka UI: http://localhost:8081
- Redis Insight: http://localhost:5540
- MySQL: localhost:3306
- Redis: localhost:6379

---

## 🔌 API Kullanımı

### POST `/api/events`

Yeni bir event gönderir:

```json
{
  "subscriberId": "53200123",
  "type": "CALL_START",
  "timestamp": "2025-11-11T19:49:00Z",
  "details": { "calledNumber": "+905555555555" }
}
```

### GET `/api/events/stats?minutes=5`

Son X dakikalık istatistikleri döner :

```json
{
  "totalEvents": 7,
  "topServiceType": "CALL_START",
  "byType": { "CALL_START": 7 },
  "perMinute": [
    { "minute": "2025-11-11 19:49", "count": 7 },
    { "minute": "2025-11-11 19:50", "count": 0 }
  ]
}
```

---


## 🧵 Multithread Test (Postman + Kafka Parallelism)

Bu test, sistemin yüksek hacimli eşzamanlı (multithread) istekleri nasıl yönettiğini göstermek amacıyla yapılmıştır.

### 🔹 Test Senaryosu
Postman aracılığıyla `POST /api/events` endpoint’ine **eşzamanlı 20 adet istek** gönderildi.  
Kullanılan veri dosyası proje kök dizinindedir: multithread_test.csv

### 🔹 CSV Formatı
Dosya şu kolonları içerir:

| subscriberId | type        | timestamp              | calledNumber     |
|---------------|-------------|------------------------|------------------|
| 53200101      | CALL_START  | 2025-11-13T03:25:00Z  | +905555555501    |
| ...           | ...         | ...                    | ...              |
| 53200120      | CALL_START  | 2025-11-13T03:25:00Z  | +905555555520    |

### 🔹 Postman Test Ayarları
1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/events`
3. **Body:**
   ```json
   {
     "subscriberId": "{{subscriberId}}",
     "type": "{{type}}",
     "timestamp": "{{timestamp}}",
     "details": { "calledNumber": "{{calledNumber}}" }
   }


Runner Settings:

Data File → multithread_test.csv
Iterations → 20 (İstediğimiz kadar tekrarlayabilibiliriz.)
Delay → 0

Sonuç:

1. Yöntem: Kafka Logs

2025-11-13T22:34:35.379+03:00  INFO 22336 --- [ntainer#0-1-C-1] c.a.t.service.EventConsumer              : Consumed event for subscriberId=53200119 type=CALL_START

gibi bir log verisi gözlemleyebiliriz.
[...] [ntainer#0-0-C-1]
[...] [ntainer#0-1-C-1]
[...] [ntainer#0-2-C-1]

Orta kısımlar partition ID'yi belirtir (0, 1 ,2 ve 4). Kafka mimarisi gereği “bir partition = bir aktif consumer thread” dir.
multithread tüketim = aktif partition sayısı; şu an 3 paralel tüketim mevcuttur.

Run/Debug işlemi sırasında Call Stack'lerde gözlemlenebilir.

2. Yöntem: Kafka UI

Kafka UI ile Sol Drawer'daki Topics sekmesinden "telecom.events" topic'i seçilir. Sonrasında "Messages" sekmesine gelinerek belirli subscriberId gruplarının farklı partitionlara atandığı gözlemlenir.


## 🧲 Doğrulama ve Gözlem

Bu bölüm, sistemin gerçekten uçtan uca çalıştığını manuel olarak test etmek için hazırlanmıştır.

### 🔹 1. MySQL (Event Kayıtlarını Görüntüleme)

Mysql uygulaması ile application.properties'de bulunan root kimlik bilgileriyle, veya Mysql üzerinde yetkili kullanıcı oluşturulduysa bu bilgiler ile Mysql'e giriş yapılarak "events" tablosu görüntülenir.
Manuel olarak:

```bash
docker exec -it telecom-mysql mysql -u root -p
```

Ardından:

```sql
USE telecom;
SHOW TABLES;
SELECT * FROM events ORDER BY id DESC LIMIT 10;
```

📋 Eğer tablo aşağıdaki gibi doluysa her şey yolundadır:

| id | subscriber_id | type       | timestamp           | details_json                     | created_at          |
| -- | ------------- | ---------- | ------------------- | -------------------------------- | ------------------- |
| 1  | 53200123      | CALL_START | 2025-11-11 19:49:00 | {"calledNumber":"+905555555555"} | 2025-11-11 19:49:05 |

---

### 🔹 2. Kafka (Mesaj Kuyruğunu Kontrol Etme)

1. Yöntem : Kafka Container ile gözlemleme.
Kafka container’ına gir:

```bash
docker exec -it telecom-kafka bash
```

Aşağıdaki komutları çalıştır:

```bash
kafka-topics --bootstrap-server kafka:19092 --list
kafka-topics --bootstrap-server kafka:19092 --describe --topic telecom.events

# Gönderilmiş mesajları görüntüle
kafka-console-consumer \
  --bootstrap-server kafka:19092 \
  --topic telecom.events \
  --from-beginning \
  --max-messages 10
```

🟢 Mesaj JSON’ları gözüküyor ise  `EventProducer` ve `KafkaConsumer` doğru çalışıyor demektir.
  {"subscriberId":"53200123","type":"CALL_START","timestamp":"2025-11-11T19:49:00Z","details":{"calledNumber":"+905555555555"}}



2. Yöntem : `Kafka UI` kullanımı.
 
Kafka UI, Kafka’da akan mesajları grafik arayüz üzerinden incelemeyi sağlar.
Docker Compose ile proje başlatıldığında Kafka UI otomatik olarak şu adreste açılır: http://localhost:8081

Kafka UI üzerinden kontrol adımları:

Sol menüden Topics sekmesine girilir.
Listeden telecom.events topic'ini seçilir.
Açılan ekranda:
Partitions sekmesinde her partition'ın offset ilerleyişini,
Messages sekmesinde gelen event'lerin JSON içeriklerini,
Consumer Groups sekmesinde aktif consumer thread’lerini gözlemlenebilir.
Örnek görüntüler:
Partition 0 → subscriberId 53200103, 53200104
Partition 1 → subscriberId 53200112, 53200113
Partition 2 → subscriberId 53200107, 53200110
Bu görünüm, Kafka’nın partitioning mekanizmasını doğrular ve sistemin multithread çalıştığını UI üzerinde grafiksel olarak gözlemlenmesini sağlar.

---

### 🔹 3. Redis (Cache ve İstatistik Kontrolü)

Redis container’ına girilir:

```bash
docker exec -it telecom-redis redis-cli
```

**Son Event’leri Görmek İçin:**

```bash
KEYS subscriber:*:last
LRANGE subscriber:53200123:last 0 -1
```

**İstatistik Bucket’larını Görmek İçin:**

```bash
KEYS stats:minute:*
GET stats:minute:<epochMinute>:total
HGETALL stats:minute:<epochMinute>:types
```

Eğer değerler dönüyorsa, Redis güncel istatistikleri tutuyor demektir. ✅


2. Yöntem: Redis-Insight (Redis UI)

Docker Compose ile Redis-Insight otomatik olarak şu adreste açılır: http://localhost:5540

Redis-Insight üzerinde kontrol adımları:

Uygulama açıldığında "Add Redis Database" seçilir.
Hostname: telecom-redis
Port: 6379
Bağlantı kurulduktan sonra sol menüden Browser sekmesine girilir.
Burada Redis içinde üretilen tüm key'leri görüntülenir.
subscriber:   *:last → her abonenin son event listesi
stats:minute: * → dakika bazlı istatistik bucket’ları
Bir key'e tıkladığında sağ tarafta:
total değerleri
types hash kayıtları
LIST/LRANGE içindeki event json’ları
şeklinde direkt görüntülenir.
Bu şekilde Redis’in hem event cache, hem de istatistik cache görevlerini başarıyla yerine getirdiğini görsel olarak gözlemleriz.

---

## 📈 Sistem Akışı

```
[REST API] → [Kafka Topic] → [Kafka Consumer] → [Redis + MySQL] → [Stats Endpoint]
```

Tüm bileşenler Docker Compose içinde izole şekilde çalışır.

---


## 🚀 AI Araçlarının Projeye Katkısı

Bu proje geliştirilirken AI destekli kod asistanları aktif olarak kullanılmış ve geliştirme süreci hem hız hem de doğruluk açısından önemli ölçüde iyileştirilmiştir.

Mimari tasarım Aşaması ve geliştirme aşaması kısmında etkin rol oynamıştır. 
Kurulum sırasında karşılaşılan tüm teknik hatalar (MySQL bağlantı hatası, DB dialect hatası, Docker network hataları vb.) AI tarafından analiz edilip çözümlenmiştir.
Tasarım hatalarının erken aşamada önüne geçmiştir.
Multithread, Kafka, Redis ve MySQL gibi kompleks teknolojilerin doğrulanmasını ve testlerini kolaylaştırmıştır.
Proje geliştirme süresini %40–60 kısaltmıştır.

### Yapay Zeka : 
DTO modellerinin tanımlanması.
MySQL repository’lerinin oluşturulması.
EventController yazılımı.
Tüm konfigürasyon dosyalarının üretimi.
Port-hostname ayarlarını doğrulanmasını / hatalarının tespiti.
Redis template ayarları

### AI Kullanımının Avantajları
1. **Hız:** Boilerplate kodların hızlı oluşturulması
2. **Best Practices:** Modern Java ve Spring Boot pattern'lerinin kullanımı
3. **Dokümantasyon:** Kod içi yorumların otomatik eklenmesi
4. **Hata Azaltma:** Syntax hatalarının önlenmesi

### AI Kullanımının Zorlukları
1. **Context Anlama:** Bazen AI'ın proje bağlamını tam anlamaması
2. **Özelleştirme:** AI kodlarının projeye özel ihtiyaçlara göre düzenlenmesi gerekliliği
3. **Debugging:** AI ile oluşturulan kodların debug edilmesi zaman alabilir

**Proje Geliştirici:** Yunus Can Dumlupınar
