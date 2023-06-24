# Spammer
Программа для ОС Android, осуществляющая обмен зашифрованными сообщениями. 

По нажатию кнопки происходит переход на второй экран (MessengerActivity), где в методе OnCreate() осуществляется генерация ключей.

![image](https://github.com/Kattarinea/Spammer/assets/65298723/c4349c5a-a88c-4d63-92c7-d7add5e48862)

Было решено использовать встроенный в android studio сервис Firebase – Realtime Database. В первую очередь проект был подключен к Firebase через настройки. Далее были добавлены все необходимые зависимости. При запуске приложения с зависимостями, установленными по умолчанию, при сборке возникли ошибки, поэтому в файле build.grable у зависимостей была повышена версия до 20.1.0. 

Для начала работы требуется создать FirebaseDatabase, внутри которой будут располагаться наши DatabaseReference. Так были созданы myRef и keysRef. Первый указывает на пулл со всеми сообщениями, а второй на единственное значение – ключ.

Для DatabaseReference есть listener, который отлавливает происходящие изменения в database. Так в методе onChildAdded() для myRef при возникновении какого-то нового сообщения, приложение будет брать значение из snapshot, которое будет шифрованным, расшифровывать его при помощи ранее сгенерированного сеансового ключа, и выводить изначальный текст на экран.

При подключении первый пользователь смотрит на наличие записей в keysRef. Если их там нет, он понимает, что он подключился первым. Следовательно, он генерирует свой публичный и секретный ключ. Публичный кладет в keysRef. Второй пользователь видит данное значение, забирает его себе. Генерирует сеансовый ключ, зашифровывает его полученным публичным ключом первого клиента и кладет обратно в keysRef. Первый клиент видит изменения, расшифровывает полученные данные своим секретным ключом и таким образом получает сеансовый ключ. Так у обоих пользователей есть один сеансовый ключ, при использовании которого будет происходить шифрование сообщений.

Реализация шифрования/расшифрования ключей и данных представлена в классе Crypt. Функция generateKey() генерирует публичный и секретный ключи клиента; encryptData() шифрует данные на сеансовом ключе; decryptData() расшифровывает данные на сеансовом ключе; secretKeySpec() генерирует сеансовый ключ; encryptSecKey() шифрует сеансовый ключ для его дальнейшей передачи; decryptSecKey() расшифровывает сеансовый ключ.

![image](https://github.com/Kattarinea/Spammer/assets/65298723/2bedeb64-02ec-40a1-b726-2a4b3c1517b9)


