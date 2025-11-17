---
marp: true
theme: default
---

# Java I/O и неблокирующий ввод-вывод

## Java I/O & NIO

---

# Java I/O

- Потоки ввода-вывода (Stream)
- Чтение/запись файлов
- Буферизированный ввод-вывод
- Работа с текстом и бинарными данными

---

# Базовые классы I/O

- InputStream/OutputStream
- Reader/Writer
- File
- FileInputStream/FileOutputStream
- BufferedReader/BufferedWriter

---

# Пример традиционного I/O

```java
try (BufferedReader reader = new BufferedReader(
        new FileReader("input.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}
```

---

# Java NIO

- Появился в Java 1.4
- Неблокирующий ввод-вывод
- Буферы и каналы
- Селекторы
- Memory-mapped файлы

---

# Основные компоненты NIO

- Buffer
- Channel
- Selector
- ByteBuffer
- FileChannel
- SocketChannel

---

# Буферы в NIO

```java
ByteBuffer buffer = ByteBuffer.allocate(1024);
// Запись в буфер
buffer.put(data);
// Подготовка к чтению
buffer.flip();
// Чтение из буфера
byte b = buffer.get();
```

---

# Каналы в NIO

```java
try (FileChannel channel = FileChannel.open(
        Paths.get("file.txt"), StandardOpenOption.READ)) {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    channel.read(buffer);
}
```

---

# Селекторы: Основы

- Позволяют отслеживать состояние множества каналов
- Работают только с SelectableChannel
- Основные операции: OP_READ, OP_WRITE, OP_CONNECT, OP_ACCEPT

---

# Селекторы: Регистрация каналов

```java
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false);
serverChannel.bind(new InetSocketAddress(8080));

Selector selector = Selector.open();
serverChannel.register(selector, SelectionKey.OP_ACCEPT);
```

---

# Селекторы: Цикл обработки событий

```java
while (true) {
    selector.select();
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    Iterator<SelectionKey> iter = selectedKeys.iterator();

    while (iter.hasNext()) {
        SelectionKey key = iter.next();
        if (key.isAcceptable()) {
            // Обработка нового подключения
        } else if (key.isReadable()) {
            // Чтение данных
        }
        iter.remove();
    }
}
```

---

# Селекторы: Обработка ошибок

```java
try {
    if (key.isValid()) {
        if (key.isReadable()) {
            // Чтение данных
        }
    } else {
        key.cancel();
        key.channel().close();
    }
} catch (IOException e) {
    key.cancel();
    key.channel().close();
}
```

---

# Memory-Mapped Files

```java
try (FileChannel channel = FileChannel.open(
        Paths.get("bigfile.dat"), StandardOpenOption.READ)) {
    MappedByteBuffer buffer = channel.map(
        FileChannel.MapMode.READ_ONLY, 0, channel.size());
}
```

---

# Заключение

- I/O для простых операций
- NIO для сложных сценариев
- Выбор зависит от задачи
- NIO сложнее в использовании
- I/O проще для понимания
