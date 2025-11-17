---
title: Реализация клиент-серверного взаимодействия на Java
marp: true
---

<!-- Слайд 1: Введение -->

# Клиент-Серверное взаимодействие в Java

**Цель:** Научиться создавать клиенты и серверы на Java для обмена данными.  
**Инструменты:** `Socket`, `ServerSocket`.

<!--
_Преподаватель: Подчеркнуть важность понятия разделения ролей между приложениями._ -->

---

<!-- Слайд 2: Что такое клиент-сервер? -->

# Клиент-Серверная модель

**Сервер:**

- Ожидает подключений.
- Обрабатывает запросы и отправляет ответы.

**Клиент:**

- Инициирует соединение с сервером.
- Отправляет данные и получает результат.

_Пример:_ Клиент — браузер, сервер — сайт (например, `www.google.com`).

---

<!-- Слайд 3: Протоколы обмена -->

# Протоколы обмена данными

- **TCP/IP:** Управляет передачей пакетов надежно.
- **HTTP/HTTPS:** Используется в вебе (клиент-сервер).

**Важно!** Для Java используем **Java I/O API** с `Socket` и `ServerSocket`.

<!--
_Преподаватель: Объяснить, как протоколы определяют формат обмена._
-->

---

<!-- Слайд 4: Реализация сервера -->

# Сервер на Java

```java
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(1234); // Порт
        System.out.println("Сервер запущен!");

        while (true) {
            Socket client = server.accept(); // Ожидание подключения
            new Thread(new ClientHandler(client)).start();
        }
    }
}
```

_Комментарий:_ `1234` — произвольный порт. Сервер работает бесконечно до перезапуска.

---

<!-- Слайд 5: Обработка запросов -->

# Обработчик клиента (Server часть)

```java
class ClientHandler implements Runnable {
    private final Socket client;

    public ClientHandler(Socket s) { this.client = s; }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            String request = in.readLine();
            System.out.println("Получено: " + request);
            out.println("Эхо: " + request); // Ответ клиенту

        } catch (IOException e) {
            System.err.println("Ошибка обработки клиента");
        } finally {
            try { client.close(); } catch(IOException e) {}
        }
    }
}
```

<!--
_Преподаватель: Объяснить потоки ввода-вывода и закрытие соединения._
-->

---

<!-- Слайд 6: Реализация клиента -->

# Клиент на Java

```java
public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234); // Адрес сервера и порт

        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println("Привет, сервер!");
        System.out.println("Ответ: " + in.readLine());

        socket.close();
    }
}
```

_Комментарий:_ `localhost` — для тестирования на одном компьютере.

---

<!-- Слайд 7: Тестирование -->

# Запуск и проверка

1. Запустить сервер (`Server`).
2. Отдельно запустить клиента (`Client`).
   **Ожидаемый вывод:**

- Сервер: `Получено: Привет, сервер!`
- Клиент: `Ответ: Эхо: Привет, сервер!`

---

<!-- Слайд 8: Отработка исключений -->

# Обработка ошибок и исключений

```java
try (ServerSocket server = new ServerSocket(1234)) {
    // ... основной код ...
} catch (IOException e) {
    System.err.println("Ошибка сервера!");
    e.printStackTrace();
}

// Для клиентов: аналогичный try-catch вокруг Socket и ввода-вывода.
```

_Важно:_ Используйте `try-with-resources` для автоматического закрытия ресурсов!

---

<!-- Слайд 9: Заключение -->

# Итоги

- **Клиент** → Инициирует соединение.
- **Сервер** → Обрабатывает запросы и возвращает результат.
- Основные классы: `Socket` и `ServerSocket`.

_Преподаватель: Подвести итоги, ответить на вопросы._

<!--
### Комментарии для преподавателя:
1. **Начало:** Убедитесь, что студенты понимают базовые термины (порт, сокет). Пример с браузером и сайтом поможет визуализировать архитектуру.
2. **Код сервера/клиента:** Рекомендуется запустить демонстрационный пример во время лекции — студенты увидят, как работает код "на лету".
3. **Исключения:** Объясните, почему важно обрабатывать `IOException` и использовать блоки `try-with-resources`.
4. **Домашнее задание:** Следующий урок можно начать с проверки выполнения задач (например, сервер для квадратов).
5. **Вопросы студентам:**
   - "Какой класс отвечает за создание сервера?"
   - "Почему нужно закрывать `Socket` после завершения работы?"

P.S. Для продвинутых: Можно упомянуть NIO (`AsynchronousServerSocketChannel`) и фреймворки вроде RMI или Spring Boot для расширения темы.

-->
