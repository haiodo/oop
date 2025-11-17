---
marp: true
theme: default
paginate: true
---

# Java Memory Model: Happens-Before

---

## Проблема: видимость изменений между потоками

Без каких-либо гарантий JVM и процессор вправе:
- **переупорядочивать** инструкции
- **кэшировать** значения в регистрах или L1-кэше ядра

```java
// Поток A
ready = true;     // (1)
data  = 42;       // (2)

// Поток B
if (ready) {      // (3)
    use(data);    // (4) — data может быть 0!
}
```

**Поток B может увидеть `ready == true`, но при этом `data == 0`** — порядок (1) и (2) может быть переставлен.

---

## Что такое Happens-Before?

**Happens-Before** — отношение между операциями, гарантирующее:

> Если операция **A** happens-before **B**, то все записи, сделанные в A и до A, **видны** потоку, выполняющему B.

```
A happens-before B
  ↓
Все записи A видны в B
```

Это не означает, что A выполняется раньше по времени — только то, что эффекты A **не будут скрыты** от B.

---

## Правила Happens-Before в Java (JLS §17.4.5)

| Правило | Гарантия |
|---------|----------|
| **Program Order** | Каждое действие в потоке HB следующему в том же потоке |
| **Monitor Lock** | `unlock(m)` HB `lock(m)` того же монитора |
| **Volatile** | Запись в `volatile` HB чтению того же поля |
| **Thread Start** | `thread.start()` HB любому действию в этом потоке |
| **Thread Join** | Любое действие в потоке HB возврату из `join()` |
| **Transitivity** | Если A HB B и B HB C, то A HB C |

---

## Правило 1: Program Order

Внутри одного потока всё выполняется в порядке программы:

```java
// Поток A — гарантированный порядок
int x = 1;     // (1)
int y = x + 1; // (2) — видит x == 1
System.out.println(y); // (3) — видит y == 2
```

```
(1) ──HB──► (2) ──HB──► (3)
```

⚠️ **Это гарантия только для одного потока.** Другой поток не видит x и y без дополнительных механизмов.

---

## Правило 2: volatile — запись HB чтению

```java
volatile boolean ready = false;
volatile int data = 0;

// Поток A
data  = 42;      // (1)
ready = true;    // (2) — volatile запись

// Поток B
if (ready) {     // (3) — volatile чтение
    use(data);   // (4) — гарантировано видит data == 42
}
```

```
Поток A:  (1) ──HB──► (2: volatile write)
                              │
                              └──HB──► (3: volatile read) ──HB──► (4)
```

Так как `(2) HB (3)` и `(1) HB (2)`, по транзитивности `(1) HB (4)`.

---

## Правило 2: volatile — пример с флагом остановки

```java
class Worker implements Runnable {
    private volatile boolean stopped = false;

    public void stop() { stopped = true; }  // Поток-управляющий

    @Override
    public void run() {
        while (!stopped) {       // Поток-рабочий всегда видит актуальное значение
            doWork();
        }
    }
}
```

Без `volatile` JIT может **оптимизировать цикл** в бесконечный, закэшировав `stopped = false`.

---

## Правило 3: synchronized — unlock HB lock

```java
int value = 0;

// Поток A
synchronized (lock) {
    value = 100;   // (1)
}                  // (2) unlock

// Поток B
synchronized (lock) { // (3) lock — ждёт unlock от A
    System.out.println(value); // (4) гарантировано видит 100
}
```

```
Поток A: (1) ──HB──► (2: unlock)
                           │
                    HB────►(3: lock) ──HB──► (4)
```

---

## Правило 3: synchronized — неправильное использование

```java
// ❌ Неверно: разные мониторы — нет гарантии
synchronized (lock1) { value = 100; }
synchronized (lock2) { System.out.println(value); } // может быть 0!

// ✅ Верно: один монитор
synchronized (lock) { value = 100; }
synchronized (lock) { System.out.println(value); }  // гарантированно 100
```

**Happens-Before создаётся только между unlock и lock одного и того же монитора.**

---

## Правило 4: Thread.start() HB действиям потока

```java
int x = 0;

x = 42;                  // (1) в главном потоке
Thread t = new Thread(() -> {
    System.out.println(x); // (3) гарантировано видит 42
});
t.start();               // (2) — HB любому действию в t
```

```
Главный поток: (1) ──HB──► (2: start())
                                 │
                          HB────►(3: начало потока t)
```

Всё, что записано **до** `start()`, **видно** внутри стартующего потока.

---

## Правило 5: Действия потока HB Thread.join()

```java
int result = 0;

Thread t = new Thread(() -> {
    result = computeHeavy(); // (1) внутри потока
});
t.start();
t.join();                    // (2) — ждём завершения

System.out.println(result);  // (3) гарантировано видит результат (1)
```

```
Поток t:       (1) ──HB──► завершение потока
                                  │
                           HB────►(2: join() возвращается) ──HB──► (3)
```

---

## Правило 6: final-поля — безопасная публикация

```java
class Point {
    final int x;
    final int y;

    Point(int x, int y) {
        this.x = x;  // (1) запись в final
        this.y = y;  // (2) запись в final
    }                // (3) конструктор завершён
}

Point p = new Point(3, 5);
// Любой поток, получивший ссылку на p, гарантированно видит x=3, y=5
```

`final`-поля имеют особую гарантию: после завершения конструктора их значения видны **всем потокам без дополнительной синхронизации**.

---

## Транзитивность: составные цепочки

Если A HB B и B HB C, то A HB C.

```java
// Поток A
data = 42;         // (1)
synchronized(m) {
    flag = true;   // (2) — unlock после этого HB lock в B
}

// Поток B
synchronized(m) {  // (3) — lock видит unlock A
    if (flag) {    // (4)
        use(data); // (5) — транзитивно: (1) HB (2) HB (3) HB (5)
    }
}
```

```
(1) ──HB──► (2: unlock) ──HB──► (3: lock) ──HB──► (4) ──HB──► (5)
└──────────────────────────────────────────────────────────────────┘
           data=42 гарантированно виден в точке (5)
```

---

## Типичные ошибки: гонка без HB

```java
// ❌ Двойная проверка без volatile — сломанный Singleton
class Singleton {
    private static Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {               // (1) чтение — нет HB!
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton(); // (2) запись
                }
            }
        }
        return instance; // может вернуть частично инициализированный объект!
    }
}
```

Проблема: `(2)` может быть виден как частичная запись в `(1)` у другого потока.

---

## Исправление: volatile + Double-Checked Locking

```java
// ✅ Правильный Double-Checked Locking
class Singleton {
    private static volatile Singleton instance; // volatile!

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                    // volatile-запись создаёт HB для всех последующих volatile-чтений
                }
            }
        }
        return instance;
    }
}
```

Теперь: запись `instance = new Singleton()` HB чтению `instance == null` у других потоков.

---

## AtomicInteger и java.util.concurrent

`java.util.concurrent` строит свои гарантии на тех же правилах HB:

```java
AtomicInteger counter = new AtomicInteger(0);

// Поток A
counter.set(42);          // volatile-запись внутри

// Поток B
int v = counter.get();    // volatile-чтение — гарантированно видит 42
```

```java
// Все операции до put() HB операциям после get() в ConcurrentHashMap
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Поток A
map.put("key", 100); // HB

// Поток B
Integer v = map.get("key"); // видит 100
```

---

## Шпаргалка: как создать Happens-Before

| Что нужно | Механизм |
|-----------|----------|
| Видимость флага остановки | `volatile` |
| Атомарная операция + видимость | `synchronized` или `AtomicXxx` |
| Передать данные в новый поток | Записать до `thread.start()` |
| Получить результат потока | Читать после `thread.join()` |
| Безопасно опубликовать объект | `final`-поля или `volatile`-ссылка |
| Неизменяемый объект (immutable) | Все поля `final` — публикация безопасна всегда |

---

## Вопросы для самопроверки

1. Поток A записывает `x = 1`, затем делает `thread.start()`. Поток B читает `x`. Есть ли гарантия?
2. Два потока синхронизируются на **разных** объектах. Есть ли Happens-Before?
3. Чем отличается `volatile` от `synchronized` с точки зрения HB?
4. Почему `instance = new Object()` без `volatile` небезопасно в DCL?
5. Что произойдёт, если убрать `volatile` из примера с флагом `stopped`?
