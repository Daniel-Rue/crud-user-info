Данный проект - это разработанное средствами Java, Spring REST-API приложение. В нём реализована CRUD-модель для работы с данными о пользователе.

Приложение разработано на основе Трёхуровневой архитектуры: service, controller, domain. Для взаимодействия с бд использовал Hibernate. В качестве бд выбрал PostgreSQL.

Для реализации функционала по работе с фотографиями я выбирал между тремя подходами:
1) Самым правильным вариантом, как я считаю, является хранение в бд ссылок на объект, а сами файлы фотографий будут храниться в облачных хранилищах. Но для этого нужно оплачивать облако, и я решил, что для тестового задания это избыточно.
2) Хранить фотографии в формате Base64 прямо в бд, но я знаю, что это является плохой практикой, потому что запросы начнут очень много весить.
3) Я выбрал сохранять фотографии в файловой системе, а в бд хранить путь к этим файлам. Остановился на этом подходе, так как он больше всего похож на первый самый оптимальный подход.

Для реализации возможности авторизации я использовал JWT, т.к его достаточно просто реализовать, этот стандарт является более защищённым и актуальным в отличии от Base Auth, а OAuth - избыточен, т.к. потребуется реализовывать отдельный сервер для авторизации.
