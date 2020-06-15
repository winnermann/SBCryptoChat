package com.example.sbcryptochat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.function.Consumer;

// MainActivity - ГлавноеОкно
public class MainActivity extends AppCompatActivity {


    // Объявлена переменная для кнопки "Отправить - sendButton"
    Button sendButton;
    // Объявлена переменная для поля ввода "Введите Ваше сообщение - userInput"
    EditText userInput;
    // Объявлена переменная для окна чата "Окно чата - chatWindow"
    RecyclerView chatWindow;
    // Объявлена переменная для контролера сообщений что бы сообщение положить в RecyclerView
    MessageController controller;
    // Объявлена переменная server, для вызова сервера
    Server server;
    //В эту переменную будет записываться имя пользователя из всплывающего диалогового окна
    String userName;

    //Метод для вызова всплывающего окошка, куда можно ввести свое имя
    public void getUserName(){
        //AlertDialog.Builder - специальный класс для сбора диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //this- указывает что диалоговое окно (builder) будет всплывать в текущем окне MainActivity
        //Указать что будет написано во всплавающем диалоговом окнке (Enter your name)
        builder.setTitle("Enter your name");
        //создать поле программным способом для введения текста (Именю пользователя)
        final EditText nameInput = new EditText(this); //this- указывает что диалоговое окно (nameInput) будет находиться в текущем окне MainActivity
        //сказать билдеру чтобы он отображал поле для ввода (nameInput)
        builder.setView(nameInput);
        //указать что будет происходить при нажатии на кнопку "Save". Будет срабатывать метод public void onClick
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //получить введенное пользователем имя в переменную userName из окна nameInput в виде текста в виде строки
                userName = nameInput.getText().toString();
                //отослать полученное имя пользователя на сервер
                server.sendUserName(userName);

            }
        });
        //показать диалог
        builder.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { //метод onCreate выполняется при создании окошка (activity), выполняестя код внутри метода, по умолчанию окно пустое, а мы в него закладываем (R.layout.activity_main)
        super.onCreate(savedInstanceState); //
        setContentView(R.layout.activity_main); // установить_как_выглядит_КонтентОкна(R-это_папка_ресурсы_res.layout-разметка.из_файла_activity_main);
        //найди элемент интерфейса (вьюшку кнопка sendButton) по его идентификатору элемента id sendButton
        sendButton = findViewById(R.id.sendButton);
        //найди элемент интерфейса (вьюшку поле ввода userInput) по его идентификатору элемента id userInput
        userInput = findViewById(R.id.messageText);
        //найди элемент интерфейса (вьюшку окно чата chatWindow) по его идентификатору элемента id chatWindow
        chatWindow = findViewById(R.id.chatWindow);

        //Создан объект MessageController()
        controller = new MessageController();
        //указать контроллеру как выглядит само сообщение
        //указать как выглядит входящее сообщение
        controller.setIncomingLayout(R.layout.message); //контролер.установитьВходящуюРазметку(папка_res.папака_layout.файл_message)
        //указать как выглядит исходящее сообщение
        controller.setOutgoingLayout(R.layout.outgoing_message); //контролер.установитьИсходящуюРазметку(папка_res.папака_layout.файл_outgoing_message)
        //установить контролеру куда выводить текст сообщения messageText по его идентификатору элемента id messageText
        controller.setMessageTextId(R.id.messageText);
        //установить контролеру куда выводить имя пользователя userName по его идентификатору элемента id userName
        controller.setUserNameId(R.id.userName);
        //установить контролеру куда выводить дату сообщения messageDate по его идентификатору элемента id messageDate
        controller.setMessageTimeId(R.id.messageDate);
        //указать контролеру куда(в элемент интерфейса вьюшку chatWindow) выводить messageText, userName, messageDate. this - означает отношение к окошку MainActivity
        //контроллер.прикрепитьсяК(окноЧата, Это_окно_MainActivity)
        controller.appendTo(chatWindow, this);
        //добавим в контролер тестовое сообщение
        controller.addMessage(
                new MessageController.Message("Всем здрасте, Вас приветствует Скиллбокс. Вы создали Ваше первое Андроид приложение", "Skillbox", false) // создадим новое сообщение: текст "Всем здрасти", имяПользователя "Skillbox", исходящее нет
        );

        //установить кнопке sendButton действие через ClickListener при нажатии на кнопку sendButton
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //вот что происходит при нажатии на кнопку sendButton:
                //получить текст из поля ввода userInput и положить то что пользователь ввел (это значение) в переменную text
                String text = userInput.getText().toString();


                //Вывести текст который пользователь ввел из переменной text в качестве сообщения
                controller.addMessage(
                        new MessageController.Message(text, userName, true) // создадим новое сообщение: текст из переменной text, имяПользователя "Мишаня", исходящее да
                );

                //вызвать метод public void sendMessage (String message){
                server.sendMessage(text);


                    //Очистить поле для ввода userInput после отправки сообщения
                userInput.setText("");

            }
        });
        //Создан объект Server
        //передадим Consumer в объект new Server
        //передадим код который возникает при появлении нового сообщения
        server = new Server(new Consumer<Pair<String, String>>() {
            @Override
            //Consumer внутри сервера
            public void accept(final Pair<String, String> p) { //Pair <имя, сообщение>
                //Выполнить Consumer в основном потоке, а не в фоновом (по умолчанию он в фоновом выполняется)
                //runOnUiThread(new Runnable() внутри Консьюмера
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //наш код ынутри Runnable()
                        controller.addMessage(
                                //(p.secon-текст сообщения, p.first-имя ползователя, false-входящее сообщение)
                                new MessageController.Message(p.second, p.first, false) //
                        );

                    }
                });
            }
        });
        //
        server.connect();
        getUserName();
    }
}
