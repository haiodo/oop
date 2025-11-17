---
title: 'Сокеты в Java'
marp: true
---

## Слайд 1: Введение в сокеты

- **Определение сокета**: сокет — это конечная точка двусторонней связи между двумя программами, работающими на сетевом уровне.
- **Роль сокетов в сетевом взаимодействии**: сокеты позволяют программам обмениваться данными через сеть, будь то локальная сеть или интернет.

---

## Слайд 2: Типы сокетов

- **TCP-сокеты**: обеспечивают надёжную и упорядоченную доставку данных.
- **UDP-сокеты**: используются для быстрой передачи данных без гарантии доставки.

---

## Слайд 3: Создание TCP-сокета в Java

- **Импорт необходимых классов**: `import java.net.ServerSocket;` `import java.net.Socket;`
- **Создание сервера**: `ServerSocket serverSocket = new ServerSocket(port);`
- **Ожидание подключения клиента**: `Socket clientSocket = serverSocket.accept();`

---

## Слайд 4: Создание UDP-сокета в Java

- **Импорт необходимых классов**: `import java.net.DatagramSocket;` `import java.net.DatagramPacket;`
- **Создание сокета**: `DatagramSocket datagramSocket = new DatagramSocket(port);`

---

## Слайд 5: Работа с потоками ввода-вывода в TCP-сокетах

- **Получение входного потока**: `InputStream inputStream = clientSocket.getInputStream();`
- **Получение выходного потока**: `OutputStream outputStream = clientSocket.getOutputStream();`
- **Чтение и запись данных**: использование потоков для чтения и записи данных в формате байтов.

---

## Слайд 6: Пример чтения данных из TCP-сокета

```java
byte[] buffer = new byte[1024];
int bytesRead = inputStream.read(buffer);
String data = new String(buffer, 0, bytesRead);
```

---

## Слайд 7: Пример записи данных в TCP-сокет

```java
String message = "Hello, client!";
byte[] messageBytes = message.getBytes();
outputStream.write(messageBytes);
```

---

## Слайд 8: Работа с потоками ввода-вывода в UDP-сокетах

Отправка данных: `datagramSocket.send(datagramPacket);`
Приём данных: `datagramSocket.receive(datagramPacket);`

---

## Слайд 9: Пример отправки данных через UDP-сокет

```java
byte[] data = "Hello, UDP!".getBytes();
DatagramPacket datagramPacket = new DatagramPacket(data, data.length, address, port);
datagramSocket.send(datagramPacket);
```

---

## Слайд 10: Пример приёма данных через UDP-сокет

```java
byte[] buffer = new byte[1024];
DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
datagramSocket.receive(datagramPacket);
String data = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
```
