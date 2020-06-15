package com.example.sbcryptochat;

import com.google.gson.Gson;

public class Protocol {
    // public final static int - объявленные константы:
    //Статус группового чата приравнен к единице (GROUP_CHAT=1)
    //Констана 1 о том, что это означает Групповой чат - это предусмотрено на сервере
    public final static int GROUP_CHAT=1;
    //Статус пользователя (offline, online) приравнен к единице (USER_STATUS=1)
    public final static int USER_STATUS=1;
    //Статус сообщения приравнен к двойке (MESSAGE=2)
    public final static int MESSAGE=2;
    //Статус имени пользователя приравнен к тройке (USER_NAME=3)
    public final static int USER_NAME=3;

    // 1-статус пользователя(offline, online)
    // 2-текстовое сообщение
    // 3- имя пользователя. Например "3{ name: \"Мишаня\" }";


    //Создан класс (Объект) UserName
    static class UserName {
        //в классе UserName объявлено поле name
        private String name;

        //добавлен технический код (конструктор + геттеры и сеттеры) для создания объекта UserName
        //сгенерируем конструктор Code -> Generate -> Constructor -> OK (Alt+Insert)
        public UserName(String name) {
            this.name = name;
        }

        //сгенерируем геттеры и сеттеры Code -> Generate -> Getter and Setter -> OK (Alt+Insert)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    // Класс Message будем исползовать Для входящих и исходящих сообщений
    static class Message{
        //кто отправитель сообщения
        //переменная типа long это тоже что int только для длинных чисел
        private long sender; // кто отправитель
        private String encodedText; // текст сообщения (позже текст будет шифрован)
        private long receiver; // кто получатель сообщения

        //сгенерируем конструктор для класса static class Message.  Code -> Generate -> Constructor -> encodedText:String -> OK (Alt+Insert)
        public Message(String encodedText) {
            this.encodedText = encodedText;
        }

        //сгенерируем геттеры и сеттеры Code -> Generate -> Getter and Setter (sender:long, encodedText:String, sender:receiver)  -> OK (Alt+Insert)

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public String getEncodedText() {
            return encodedText;
        }

        public void setEncodedText(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getReceiver() {
            return receiver;
        }

        public void setReceiver(long receiver) {
            this.receiver = receiver;
        }
    }

    //Создан класс (Объект) User{
    static class User{
        private String name;
        private long id;

        //сгенерируем пустой конструктор Code -> Generate -> Constructor -> name:String -> Select None (Alt+Insert)
        public User() {
        }

        //сгенерируем геттеры и сеттеры Code -> Generate -> Getter and Setter -> name:String, id:long -> OK (Alt+Insert)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    //
    static class UserStatus{
        private boolean connected; //true = подключился, false = отключился
        private User user;

        //сгенерируем пустой конструктор Code -> Generate -> Constructor -> connected:boolean -> Select None (Alt+Insert)
        public UserStatus() {
        }

        //сгенерируем геттеры и сеттеры Code -> Generate -> Getter and Setter -> connected:boolean, user:User -> OK (Alt+Insert)

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    //Распаковка сообщения
    public static Message unpackMessage (String json){
        Gson g =new Gson();
        // из json распаковать объект
        //"2{ sender: 1, receiver:4, encodedText: "Привет"}";
        //json.substring(1)-позволяет отсечь первый символ - это цифра 2
        //Message.class хранит "2{ sender: 1, receiver:4, encodedText: "Привет"}"
        return g.fromJson(json.substring(1), Message.class);

    }

    //Распаковка статуса UserStatus
    public static UserStatus unpackStatus(String json){
        Gson g =new Gson();
        // из json распаковать объект, из json получить статус пользователя
        return g.fromJson(json.substring(1), UserStatus.class);

    }

    // Запаковка сообщения
    public static String packMessage (Message m){
        Gson g =new Gson();
        // тип сообщения -2{отправитель: 1, получатель:4, текстСообщения: "Привет"}
        return MESSAGE + g.toJson(m); // возвращает "2{ sender: 1, receiver:4, encodedText: "Привет"}";
    }

    //На вход принимает "Мишаня" -> На выход выдает "3{ name: \"Мишаня\" }";
    public static String packName(UserName name) {
        //Воспользуемся библиотекой implementation 'com.google.code.gson:gson:2.8.6' из файла build.gradle
        //Создадим объект для конвертации "Мишаня" -> "3{ name: \"Мишаня\" }";
        Gson g =new Gson();
        //3 + "{ name: \"Мишаня\" }" return "3{ name: \"Мишаня\" }"
        return USER_NAME + g.toJson(name); //возвращает "3{ name: \"Мишаня\" }"

    }


    //получить тип сообщения
    //если Сервер пришлет "2{ sender: 1, receiver:4, encodedText: "Привет"}" => то мы скажем, что это сообщение с Типом 2.
    //на входе мы получаем само json-сообщения, а на выходе нам нужно сказать какого оно Типа
    public static int getType(String json){
        //если переменная json не задана (пуста) или это строка у которой длинна равна нулю, то возвращать (-1), мы тогда
        // не знаем какого типа это сообщение.
        if (json == null || json.length()==0){
            return -1;
        }
        //json.substring(0, 1)-вернет строку, состоящую из идного первого символа (это константы 1, 2 или 3 в нашем случае)
        //Integer.parseInt() приводит json.substring(0, 1) к типу int
        return Integer.parseInt(json.substring(0, 1));
    }
}
