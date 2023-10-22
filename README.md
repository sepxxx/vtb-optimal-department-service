# Сервис для подбора оптимального отделения банка

## Использованные технологии
* **Java**
* Spring Boot
* **Redis**(main storage for this project)
* Gradle(package manager to manipulate with dependecies)
* Docker(for running appllication in container)

## Создано для участия в хакатоне More Tech 5.0
## WARN!!! Это и близко не production код, все делалось за 1 день WARN!!! 
* Каждый из сервисов запустится лишь в 1 инстансе, связи по сети захардкожены
* Сервисы отдают друг другу объекты, без обертки в респонс, статус ответа нормально не отследить
* Нет обработки ошибок
* Этот репозиторий демонстрационный, проект не многомодульный, папки микросервисов просто закинуты внутрь одной и gradle будет ругаться, если склоните себе

## Шаги для установки (БУКВАЛЬНО 1 НАЖАТИЕ КНОПКИ)

**1.Скачайте файл docker-compose**

```
https://github.com/sepxxx/vtb-optimal-department-service/blob/main/docker-compose.yml
P.S всего один файл, не весь репозиторий. Все что нужно скачается и запустится само.
```
**2.Установите Docker desktop и войдите в профиль**

**3.Запустите развертывание микросервисного приложения, используя docker-compose**

```bash
docker-compose up
```
**4. Подождите пока redis хранилище начнет заполняться данными (1-2мин)**
```
После сбора образов и запуска инстансов, common-rate-service будет запущено на <http://localhost:8080>
```
## Разберем сервисы
### Табличка с назначениями сервисов
| Сервис              | Назначение                                                                                                                                                                                                          |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| common-rate-service | Формирование итоговой оценки оптимальности отделения банка исходя из данных о времени пути от гео юзера до точки и загруженности самого отделения                                                                   |
| data-generator      | Объединяет 2 сервиса: эмуляция работы аппарата для выдачи талонов для очереди в банке + сервис которому этот аппарат сообщает о выдаче нового талона, и который в свою очередь изменяет данные по отделению в Redis |
| load-rate-service   | Забирает данные из Redis, предоставленные data-generator, по запросу common-rate-service предоставляет их ему                                                                                                       |

### Удобная и понятная блок-схема с архитектурой проекта
![Архитектура](/arch.jpg)

## Описание endpoints и sample valid types
### common-rate-service
| Method | Url | Decription                            | Sample Valid Request Type | 
| ------ | --- |---------------------------------------| --------------------------- |
| GET    | http://localhost:8080/offices/optimal | Возвращается Map<OfficeId,OfficeRate> |  common-rate-service request type|

```
common-rate-service request type приходит с frontend
{
    Координаты оффисов, которые попали в указанный на frontend радиус
    "cords":[
        {
            "lat":59.962588,
            "lng": 30.294192
        },
        {
            "lat":59.98553,
            "lng":30.342431
        }
    ],
    Идентификаторы оффисов соответсвующие координатам выше
    "id": [
        26000066,
        26000077
    ],
    Геопозиция юзера
    "user_geo":{
        "lat":  59.93409850421871,
        "lng": 30.30657083072676
    },
    Тип передвижения: driving/walking 
    "move_type": "driving",
    Тип лица:0-Физическое, 1-Юридическое
    "service_type": "0"
}
```
```
load-rate-service response type
{
    "Id(Long)": Rate(Double),
    "Id(Long)": Rate(Double),
    "Id(Long)": Rate(Dobule)
}
```

### load-rate-service
| Method | Url | Decription | Sample Valid Request Type | 
|--------| --- | ---------- | --------------------------- |
| GET    | http://localhost:8081/offices/load/rating/{faceType} |  | load-rate-service request type |

``` load-rate-service request type
{   //id отделений, для которых common-rate-service требует узнать нагрузку
    "ids": [26000066, 26000077, 234232344]
}
```
```
load-rate-service response type
{
    "Id(Long)": Rate(Double),
    "Id(Long)": Rate(Double),
    "Id(Long)": Rate(Dobule)
}
```
