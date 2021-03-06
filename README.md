# TFS Android Spring 2021

## Домашнее задание по лекции "View & ViewGroup"

 Нужно реализовать:

**1. Кастомную View для отображения Emoji реакции и кол-ва реакций**
- Можно использовать Emoji из unicode.
- Вьюха должна поддерживать 2 состояния и менять их по клику:
    - состояние когда emoji в сообщении выбрано нами и когда не выбрано. Для этой цели можно использовать state_selected. Как это сделать смотрите в семинаре
- Должна быть возможность задавать из кода emoji и кол-во реакций

**2. FlexBoxLayout, который способен располагать своих детей по следующему принципу:**
- Каждый ребенок помещается в строку
- Если следующий ребенок не влезает в эту строку, то необходимо перенести его на следующую
- Последним ребенком должен быть ImageView с иконкой + (с помощью нее можно будет в дальнейшем добавлять Emoji)

**3. Кастомную ViewGroup, которая отображает сообщение вместе с реакциями т.е.
у вас будет класс унаследованный от ViewGroup, который умеет отображать следующее:**
- Аватар пользователя
- Имя пользователя
- Текст сообщения
- Emoji-реакции(FlexBoxLayout с кастомными вьюхами внутри)


[Ссылка на дизайн](https://www.figma.com/file/cTA9Cy4ix1VjiW7MgYy5tL/TFS-ANDROID?node-id=73%3A258)

## Домашнее задание по лекции "RecyclerView"

Нужно реализовать:

**1. Экран чата**
- Экран со списком сообщений
    - так же на этот экран надо добавить разделители по датам (как на дизайне)
    - сделать это можно как через отдельный viewType, так и через itemDecoration, на ваш выбор
- Добавить поле ввода сообщения
  - если поле ввода пустое — отображается крестик (в будущем будет возможность отослать файл/фото)
  - если поле ввода НЕ пустое — отображаем самолётик 
- При нажатии на "отправить" — добавлять в список новое сообщение

**2. Экран с выбором смайликов**
- BottomSheetDialog со списком смайликов
- Открывается при долгом клике на сообщение
- При клике на смайл диалог закрывается и смайл отображается как реакция под сообщением (см. дизайн)

**3. Реакции под сообщением**
- Справа от реакции — число, оно приходит с "бэка" (пока захардкожено в коде). Моделька реакций приходит с бэка в виде списка(`List<Reaction>`). В модельке `Reaction` есть поле `user_id` пользователя, который тыкнул на эту реакцию.  
- Если вы добавили реакцию и число равно одному, то при повторном клике на реакцию в теле сообщения реакция удаляется. Мог и кто-то другой добавить, потом сверху вы ещё кликнули, счётчик стал равен двум. Но потом тот кто добавлял — передумал и убрал реакцию, кликнув ещё раз по ней. И если вы сейчас тоже ткнёте — реакция должна исчезнуть (в модельке с бэка будет приходить пустой лист)
- "Плюс" в списке реакций появляется когда уже хоть одна реакция есть. До этого реакции добавляются только через лонг тап

## Домашнее задание по лекции "Fragments"

Нужно реализовать (используйте стабовый данные):

**1. Использовать BottomNavigationView**

**2. Первый экран**
- Список стримов. На экране сверху 2 вкладки: стримы на которые вы подписаны и список вообще всех стримов
- По клику на стрим грузим и раскрываем список топиков стрима
- При клике на топик переходим на экран обсуждения топика

**2. Второй экран**
- Список контактов (экран, на который данные приходят из бэка. Пока делайте эмуляцию загрузки данных)

**2. Третий экран**
- Экран детали профиля

## Домашнее задание по лекции "Асинхронное взаимодействие + RxJava"

Нужно реализовать (используйте стабовый данные):

**1. Все длительные операции выполняем асинхронно**
- Используем библиотеку [RxJava](https://github.com/ReactiveX/RxJava/tree/2.x)
- Запросы в сеть/базу данных и то, что сейчас симулирует эти запросы
- Поиск сообщений
- Вычислительные операции, вроде маппинга списков
- Работу с DiffUtils

**2. Реализовать поиск по списку все стримов**
- Поиск не должен повторно дергать методы выдачи резульата если не был изменен поисковой запрос
- Поиск должен фильтровать пустые строки
- Поиск не должен дергать методы выдачи результата на каждый символ, если пользователь быстро вводит запрос в строку поиска.

**3. Сделать обработки ошибок**
- Показывать пользователю ошибку в случае ошибки отправки сообщения или других кейсов. Можно временно испльзловать какой-то рандомайзер, 
  который будет, условно кидать ошибку на каждое N-ое сообщение. Ошибку показать можно через SnackBar или другие инструменты на ваше усмотрение.

**4. Реализовать скелетон загрузки**
- При выполнении каких-либо длительных операций, необходимо показывать скелетон загрузки для пользователя (если конечно, что-то реально грузится).
  Можно использовать [Shimmer-Android](https://github.com/facebook/shimmer-android)

## Домашнее задание по лекции "Работа с сетью"

В данном задании вам предлагается наконец-то начать работать не с моками, а с реальными данными. Вам необходимо осуществлять запросы непосредственно к [методам Zulip api](https://zulip.com/api).

Ваше приложение должно получать стримы, топики, сообщения в топиках, профили и статус пользователя(active/idle/offline) непосредственно через апи.
Также через апи нужно посылать сообщения в топики, выставлять и удалять реакции.

API key и email для авторизации пока что нужно захардкодить.
Из них вы должны получить строку Basic access authentication - в нашем случае это пара email, API key, закодированные в base64.
Чтобы получить эту строку вы можете воспользоваться методом Credentials.basic("sample@mail.ru", "api_key").
Полученную строку вам нужно подставлять в хэдер авторизации(Authorization) при каждом запросе.

Текстовый статус "In a meeting" у пользователя уберите из дизайна.
Вместо зеленого статуса online у вас будет active(зеленый)/idle(оранжевый)/offline(красный).
Кнопку Log out уберите.

Требования:
Для запросов к апи использовать Retrofit + RxJava

## Домашнее задание по лекции "Хранение данных"

**1. Кешировать списки стримов, топиков и сообщений**
- Теперь если у нас есть какие-то данные в кешах, то мы сперва показываем их (вместо шиммера), 
  а после получения данных из сети показываем актуальные данные
- Если БД пустая по конкретным топикам, то показываем шиммер  

**2. Реализовать пагинацию при подгрузке сообщений**
- Подгружать только по 20 сообщений за раз
- Начинать загрузку следующей страницы необходимо как только пользователь доскроллил до n-5 позиции, где n - это размер списка сообщений в адаптере
- В базе данных хранить последних 50 сообщений и показывать их при старте приложения до тех пор, пока не подгрузятся новые сообщения из сети

**3. Доп задание (не обязательно): реализовать отправку вложений и их получение

## Курсовая работа

Сейчас у вас уже есть почти полноценное приложение для обмена сообщениями!
Но для полного счастья не хватает всего нескольких фич, добавив которые, вы сможете использовать ваше приложение, как полноценный(почти) аналог zulip и пересесть в него если телегу забанят)

Что нужно доделать:
- Всё то, что не успели доделать и привести код в порядок. Это самый важный момент. 
  За чистый код и сделанные все пункты из домашек уже 5 баллов.
  
Фичи, которые идут ниже добавляют по 1 баллу к первым 5
- Нужно научиться создавать свои каналы
- Научиться открывать канал и показывать в нём сообщения из разных топиков
- По клику на топик можем перейти в него (как раньше из списка стримов и топиков)
- Ну а в канале неплохо было бы иметь возможность писать в разные топики (которые создаются, если они новые)
- Поменять логику по лонг тапу. Раньше мы сразу открывали список реакций, а теперь будет давать больше действий. Открывается bottom sheet и там можно:
  - Поставить реакцию
  - Удалить сообщение
  - Отредактировать сообщение
  - Поменять топик у этого сообщения
  - Скопировать сообщение в буфер обмена

Доп. задание:
- Задача со звёздочкой - научиться прикреплять картинку/файлы 
- Автотесты



