# Bucket4j Architecture Demo

Учебный демонстрационный проект на Java 17 и Spring Boot 3.5.7. Он показывает работу Bucket4j 8.21.0 на примере глобального ограничения HTTP-запросов.

## Ограничитель

Приложение создаёт один общий Spring Singleton Bean `Bucket`:

- вместимость — 5 токенов;
- каждый запрос расходует 1 токен;
- пополнение — 5 токенов каждые 60 секунд через `refillIntervally`;
- стратегия синхронизации — `SYNCHRONIZED`.

Все клиенты используют одно состояние в памяти. Это пример для изучения архитектуры, а не production-ready распределённый rate limiter.

## Запуск

Требуется JDK 17 или новее и Maven.

```bash
mvn spring-boot:run
```

Приложение запускается на `http://localhost:8080`.

Swagger UI доступен по адресу `http://localhost:8080/swagger-ui/index.html`. При открытии корневого адреса `http://localhost:8080/` приложение автоматически перенаправляет на Swagger UI.

## API

```bash
curl -i http://localhost:8080/api/test
```

Первые пять быстрых последовательных запросов возвращают `200 OK` и `Запрос разрешён`. Шестой возвращает `429 Too Many Requests` и `Превышен лимит запросов`. После очередного пополнения токены снова доступны.

### Демонстрационные методы Bucket4j

Эти endpoint'ы помогают изучать API Bucket4j через Swagger:

- `GET /api/bucket/available` — вызывает `getAvailableTokens()` и показывает остаток токенов.
- `GET /api/bucket/estimate?tokens=1` — вызывает `estimateAbilityToConsume()` без списания токенов.
- `GET /api/bucket/consume?tokens=1` — вызывает `tryConsumeAndReturnRemaining()` и возвращает остаток после операции.
- `GET /api/bucket/consume-as-much-as-possible` — вызывает `tryConsumeAsMuchAsPossible()` и списывает все доступные токены.
- `GET /api/bucket/add-tokens?tokens=1` — вызывает `addTokens()`.
- `GET /api/bucket/reset` — вызывает `reset()` и возвращает bucket к начальному состоянию.

Последние четыре endpoint'а изменяют состояние общего bucket и предназначены только для демонстрации.

## Тесты

```bash
mvn test
```

Интеграционный тест проверяет реальный Bucket4j через Spring MVC: пять запросов разрешаются, шестой отклоняется, а Spring Bean имеет тип `SynchronizedBucket`.

## Архитектура Bucket4j для отладки

В `BucketConfig` используется Builder: `Bucket4j.builder()`, `withSynchronizationStrategy`, `addLimit` и `build`. Внутри библиотеки полезно поставить breakpoint в:

- `AbstractBucket.tryConsume(long)` — общий алгоритм операции;
- `SynchronizedBucket.tryConsumeImpl(...)` — синхронизированная реализация;
- `BucketState.tryConsume(...)` — изменение состояния токенов.

Основные типы для изучения: `Bucket`, `BucketConfiguration`, `Bandwidth`, `AbstractBucket`, `SynchronizedBucket`, `BucketState` и `BucketListener`. Это демонстрирует Builder и Template Method.

## Изменение сценария

Чтобы изменить лимит, отредактируйте `Bandwidth.classic(5, ...)` в `BucketConfig`. Для другого периода измените `Duration.ofMinutes(1)`. Для сравнения реализаций замените `SynchronizationStrategy.SYNCHRONIZED` на `LOCK_FREE` и проверьте тип созданного Bean.

## Ограничения

- используется один общий Bucket для всех клиентов;
- состояние хранится только в памяти приложения;
- перезапуск сбрасывает состояние;
- нет отдельного ограничения по IP или пользователю;
- Redis, база данных, Spring Security и распределённое хранилище намеренно не используются.
