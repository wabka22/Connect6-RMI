# Connect6-RMI

Сетевая версия игры **Connect6**, реализованная на Java с использованием **RMI**.

## Установка и сборка

```bash
git clone https://github.com/wabka22/Connect6-RMI.git
cd Connect6-RMI
mvn clean package
```

## Запуск

### Сервер
```bash
cd server
java -cp target/classes;../common/target/classes connect6.server.ServerApp
```

### Клиент
```bash
cd client
java -cp target/classes;../common/target/classes connect6.client.ClientApp
```

> На Linux/macOS замените `;` на `:` в пути к классам.

## Требования
- Java 17+
- Maven 3.8+
