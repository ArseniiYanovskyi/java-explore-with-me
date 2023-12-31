# java-explore-with-me
Приложение - афиша. В этой афише можно предложить какое-либо событие от выставки до похода в кино и собрать компанию для участия в нём. 

Архитектура состоит из двух сервисов. Взаимодействие сервисов происходит через HTTP клиент на основе RestTemplate. Сервисы запускаются в своих контейнерах Docker. Оба сервиса используют базу данных PostgreSQL, каждая из которых также запускается в своём контейнере.

### 1 - Основной сервис:
Функционал приватного(для «авторизованных» пользователей) API:
 - Добавлять, редактировать, удалять свои события.
 - Просматривать созданные другими пользователями события и отправлять заявку на участие в них.
 - Принимать/отклонять заявки участия в своём событии.

Функционал API администратора:
 - Добавлять, редактировать и удалять категории событий.
 - Добавлять, редактировать, закреплять на главной странице и удалять подборки мероприятий.
 - Модерация событий пользователей.
 - Добавлять, редактировать и удалять пользователей.

Функционал публичного(для «неавторизованных» пользователей) API:
 - Просмотр подборок событий.
 -  списка событий по указанным критериям поиска.
 - Просмотр конкретного события по его идентификатору(короткая/подробная информация).

### 2 - Сервис статистики:
 - Сохраняет инорфмацию о просмотре события(endpoint, ip).
 - Предоставляет статистическую инорфмацию, на основе которой формируется рейтинг.

Подробная информация о эндпоинтах:
 - [Основной сервис](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json)
 - [Сервис статистики](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json)

###  Добавлена дополнительная функциональность «Комментарии» ([Feature: comments](https://github.com/ArseniiYanovskyi/java-explore-with-me/pull/5)):
 - Добавлять комментария к событию(авторизованным пользователем можно добавлять комментарий к одобренному/отклонённому событию вне зависимости от того, начато/завершено оно или нет. Публикатор события может добавлять комментарий к ещё не рассмотренному администратором событию, к примеру в формате «ответ на возможные вопросы», «допонительная информация» и т. п.), редактировать комментарий публикатором и удалять его.
 - Доступность комментария для прочтения не ограниченна публичным/приватным API.
 - Администратор может удалять комментарии.
 - Добавлять, редактировать, удалять ответ на комментий.
