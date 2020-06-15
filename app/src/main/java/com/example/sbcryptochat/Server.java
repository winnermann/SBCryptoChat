package com.example.sbcryptochat;

import android.util.Log;
import android.util.Pair;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Server {
    //<номер пользователя, имя пользователя>
    //в переменную names по номеру пользователя будет записываться его имяи наоборот, когда нужно узнать имя, то его можно получить по номеру пользователя
    Map<Long, String> names = new ConcurrentHashMap<>();
    //35.214.3.133:8881
    // создана переменная client
    WebSocketClient client;

    //private Consumer - это (объект) механизм, кот. уведомляет наш MainActivity.java о пришедшем сообщении
    //Consumer передает в MainActivity объект (название onMessageReceived;), в нашем случае две переменных(строку с именем пользователя, и строку с сообщением)
    private Consumer <Pair<String, String>> onMessageReceived;

    //сгенерируем конструктор Code -> Generate -> Constructor -> onMessageReceived:Consumer<Pair<String, String>> -> OK (Alt+Insert)
    //мы создали конструктор Server
    public Server(Consumer<Pair<String, String>> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect() {
        // Создана переменная address URL
        URI address;
        try {
            //в переменную address положено значение ws://35.214.3.133:8881
            address = new URI("ws://35.214.3.133:8881");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return; //выйти из метода connect() и ничего не делать
        }
        //создаем новый WebSocketClient
        client = new WebSocketClient(address) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                //логирование
                Log.i("SERVER", "Connection to server is open");
                //сказать серверу своё имя. 1-статус пользователя(offline, online), 2-текстовое сообщение, 3- имя пользователя.
                //это больше не нужно заменяем
//                String myName = "3{ name: \"Мишаня\" }";
//                client.send(myName);
                //заменяем на это:






                //что делать при подключении

            }

            @Override
            //научимся принимать сообщения в методе onMessage
            public void onMessage(String message) {
                Log.i("SERVER", "Got message from server: " + message);

                //что делать при поступлении сообщения с сервера

                //получить Тип (Type) от сообщения (message)
                int type = Protocol.getType(message);
                //Если тип сообщения USER_STATUS, то
                if (type == Protocol.USER_STATUS){
                    //надо обработать факт подключения или отключения пользователя

                    //вызвать этот метод когда Статус пользователя меняется
                    userStatusChanged(message);

                }

                //Если приходит текстовое сообщение с типом MESSAGE, то
                if (type == Protocol.MESSAGE){
                    //надо показать сообщение на экране
                    //отображение сообщения вынесем в отдельный метод private void displayIncomingMessage (String json){
                    //вызовем метод displayIncomingMessage (String json){
                    displayIncomingMessage (message);
                }

            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                //что делать при закрытии соединения
                Log.i("SERVER", "Connection closed");

            }

            @Override
            public void onError(Exception ex) {
                //что делать при ошибке
                Log.i("SERVER", "ERROR occurred: " + ex.getMessage());

            }
        };
        //выполняет подключение к серверу "ws://35.214.3.133:8881"
        client.connect();

    }


    //метод displayIncomingMessage отображает сообщение MESSAGE на экране
    //получаем само сообщение String json
    private void displayIncomingMessage (String json){
        //сообщение String json надо распаковать
        Protocol.Message m = Protocol.unpackMessage(json);
        //Взять имя пользователя из карты имен
        String name = names.get(m.getSender());
        //Если имя пустое, то
        if (name == null){
            //дать имя по умолчанию "Безымянный"
            name = "Безымянный";
        }
        String text = m.getEncodedText();
        try {
            text = Crypto.decrypt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //нужно узнать текст этого сообщения (это теперь в Consumer)
//        m.getEncodedText();
        //нужно узнать кто отправитель сообщения (это теперь в Consumer)
//        m.getSender();
        //вызвать Consumer<Pair<String, String>>() из MainActivity
        onMessageReceived.accept(
                new Pair<String, String>(name, text)  //имя, сообщение
        );


    }

    private void userStatusChanged(String json){
        //распаковать статус пользователя из json
        Protocol.UserStatus s = Protocol.unpackStatus(json);
        //если подключился
        if (s.isConnected()){
            //добавить имя в карту имен (полусить ID, получить имя)
            names.put(s.getUser().getId(), s.getUser().getName());
            //иначе, если отключился
        } else {
            //удалить пользователя из карты имен (по ID)
            names.remove(s.getUser().getId());
        }

    }

    public void sendMessage (String message){
        //если клиент пустой = ноль или клиент не подключен, то
        if (client == null || !client.isOpen()){
            //ничего не делаем
            return;
        }
        //try-catch означает - не получилось зашифровать, отправляй незашифрованным
        try {
            //зашифровка сообщения при отправке
            message = Crypto.encrypt(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //попробуем отправить на серевер тестовое сообщение
        //создадим сообщение в виде Объекта new Protocol.Message(message) и сохраним в переменную m
        Protocol.Message m  = new Protocol.Message(message);
        //нужно запаковать сообщение, для этого укажем кто получатель этого сообщения
        //мы создали сообщение с текстом "Всем приветы" и собираемся его отправить в Групповой чат GROUP_CHAT
        m.setReceiver(Protocol.GROUP_CHAT);
        // Запаковываем наше сообщение "Всем приветы"
        String packedMessage = Protocol.packMessage(m);
        //вывод сообщения в Log для отладки
        Log.i("SERVER", "Sending message: " + packedMessage);
        //отправка сообщения на Сервер
        client.send(packedMessage);
    }

    //метод public void sendUserName принимает строку String name
    public void sendUserName (String name){
        //создаем объект UserName
        //эта строка выводит "3{ name: \"Мишаня\" }";
        String myName = Protocol.packName(new Protocol.UserName(name));
        Log.i("SERVER", "Sending my name to server: " + myName);
        client.send(myName);
    }
}
