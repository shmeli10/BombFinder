package com.example.os1.bombfinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by OS1 on 19.10.2016.
 */
public class Main_Activity extends Activity {

    private Context context;

    private LinearLayout gameContainer;
    private Button       startGameBtn;

    // количество уровней в игре
    private final int LEVELS_SUM = 10;

    // плотность точек на экране
    private float density;

    // тут будет храниться порядковый номер "заминированной" коробки
    private int bombedBoxNum = 0;

    // тут будет храниться номер уровня, на котором находится игрок
    private int currentLevel;

    private LinearLayout.LayoutParams strutLP;
    private LinearLayout.LayoutParams rowLP;
    private LinearLayout.LayoutParams boxLP;
    private LinearLayout.LayoutParams boxTextViewLP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        // получаем плотность экрана
        density = context.getResources().getDisplayMetrics().density;

        ///////////////////////////////////////////////////////////////////////////////////////////

        // определяем параметры для компоновщиков элементов
        strutLP         = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rowLP           = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int) (27 * density)), 0.0f);
        boxLP           = new LinearLayout.LayoutParams(((int) (27 * density)), ((int) (27 * density)), 0.0f);
        boxTextViewLP   = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);

        ///////////////////////////////////////////////////////////////////////////////////////////

        // определяем главный контейнер
        gameContainer = (LinearLayout) findViewById(R.id.Game_Container);

        // определяем кнопку "START NEW GAME"
        startGameBtn = (Button) findViewById(R.id.StartGame_BTN);

        // по-умолчанию кнопка "START NEW GAME" выключена
        startGameBtn.setEnabled(false);

        // определяем слушателя нажатия на кнопку "START NEW GAME"
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // выключаем кнопку "START NEW GAME"
                startGameBtn.setEnabled(false);

                // запускаем новую игру
                startGame();
            }
        });

        // запускаем игру
        startGame();
    }

    // метод для инициализации игрового процесса
    private void startGame() {

        // чистим главный контейнер, перед использованием
        gameContainer.removeAllViews();

        // задаем главному контейнеру белый фон с черной рамкой
        gameContainer.setBackgroundResource(R.drawable.game_container_bg_white);

        // задаем уровень пользователя как 1 (начало игры)
        currentLevel = 1;

        // создаем невидимый элемент "вертикальная распорка"
        View verticalStrut = new View(context);
        verticalStrut.setLayoutParams(strutLP);

        // добавляем "вертикальную распорку" в главный контейнер,
        // чтобы прижать к нижнему краю все последующие добавляемые элементы
        gameContainer.addView(verticalStrut);

        // в цикле добавляем "уровни" в главный контейнер
        for(int i=LEVELS_SUM; i>0; i--) {

            // определяем очередной "уровень", в котором будем накапливать коробки
            LinearLayout rowLL = new LinearLayout(context);
            rowLL.setOrientation(LinearLayout.HORIZONTAL);
            rowLL.setLayoutParams(rowLP);
            rowLL.setGravity(Gravity.CENTER_HORIZONTAL);
            rowLL.setTag("rowLL_"+i);

            // если это первый уровень
            if(i == 1)
                // наполняем его коробками
                addBoxesToRow(rowLL, i);

            // кладем сформированный "уровень" в главный контейнер
            gameContainer.addView(rowLL);
        }
    }

    // метод для наполнения заданного уровня коробками
    private void addBoxesToRow(final LinearLayout row, int rowNum) {

        // получаем необходимое количество коробок, которые надо добавить
        int boxesSum = (LEVELS_SUM + 1) - rowNum;

        // минируем случайную коробку на уровне
        bombingABox(boxesSum);

        // будем хранить сформированный тег для коробки
        StringBuilder boxNameSB = new StringBuilder("");

        // в цикле формируем коробки и добавляем их в строку уровня
        for(int b=0; b<boxesSum; b++) {

            // формируем тег для коробки
            boxNameSB.append("boxLL_"+(b+1));

            // определяем порядковый номер коробки
            final int boxNum = (b+1);

            // формируем коробку
            LinearLayout boxLL = new LinearLayout(context);
            boxLL.setOrientation(LinearLayout.HORIZONTAL);
            boxLL.setLayoutParams(boxLP);
            boxLL.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            boxLL.setTag(boxNameSB.toString());
            boxLL.setBackgroundResource(R.color.black);
            boxLL.setTag("boxLL_"+boxNum);
            setMargins(boxLP, 2,0,0,0);

            // формируем надпись на коробке
            TextView boxTV = new TextView(context);
            boxTV.setLayoutParams(boxTextViewLP);
            boxTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            boxTV.setTextColor(context.getResources().getColor(R.color.white));
            boxTV.setTextSize(8);
            boxTV.setText("Box " +boxNum);
            boxTV.setTag("boxTV_"+boxNum);

            // добавляем надпись на коробку
            boxLL.addView(boxTV);

            // определяем слушателя нажатия на коробку
            boxLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // вызываем метод проверки какая коробка выбрана ("заминированная" или нет)
                    checkSelectedBoxOnABomb(row, boxNum);
                }
            });

            // добавляем очередную коробку в строку уровня
            row.addView(boxLL);

            // чистим переменную хранящую тег коробки, перед следующим использованием
            boxNameSB.delete(0, boxNameSB.length());
        }
    }

    // метод для случайного минирования одной из коробок на уровне
    private void bombingABox(int boxesSum) {

        // получаем значение от 1 до значения в boxesSum
        bombedBoxNum = (int)(Math.random() * boxesSum);
    }

    // метод для задания отступов программным путем
    private void setMargins(LinearLayout.LayoutParams layout,int left, int top, int right, int bottom) {

        int marginLeft     = (int)(left * density);
        int marginTop      = (int)(top * density);
        int marginRight    = (int)(right * density);
        int marginBottom   = (int)(bottom * density);

        layout.setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    // метод для реакции приложения на выбор игроком одной из коробок
    private void checkSelectedBoxOnABomb(LinearLayout row, int selectedBoxNum) {

        // делаем коробки на заданном уровне недоступными для выбора
        setBoxesUnSelectable(row);

        // если выбрана "заминированная" коробка, останавливаем игру
        if((selectedBoxNum - 1) == bombedBoxNum) {

            // задаем главному контейнеру красный фон с черной рамкой
            gameContainer.setBackgroundResource(R.drawable.game_container_bg_red);

            // включаем кнопку "START NEW GAME"
            startGameBtn.setEnabled(true);
        }
        // выбрана не "заминированная" коробка и игра продолжается
        else {

            // переводим игрока на новый уровень
            currentLevel++;

            // находим в главном контейнере подходящий уровень
            LinearLayout nextRowLL = (LinearLayout) gameContainer.findViewWithTag("rowLL_"+currentLevel);

            // если уровень найден
            if(nextRowLL != null) {

                // наполняем его коробками
                addBoxesToRow(nextRowLL, currentLevel);

                // если достигнут последний уровень (с одной коробкой), останавливаем игру
                if(currentLevel == LEVELS_SUM) {

                    // задаем главному контейнеру зеленый фон с черной рамкой
                    gameContainer.setBackgroundResource(R.drawable.game_container_bg_green);

                    // включаем кнопку "START NEW GAME"
                    startGameBtn.setEnabled(true);

                    // делаем коробки на заданном уровне недоступными для выбора
                    setBoxesUnSelectable(nextRowLL);
                }
            }
        }
    }

    // метод для преобразования коробок на заданном уровне в недоступные для выбора
    private void setBoxesUnSelectable(LinearLayout row) {

        // получаем количество коробок в строке заданного уровня
        int boxesSum = row.getChildCount();

        // будем хранить сформированный тег для коробки
        StringBuilder boxNameSB = new StringBuilder("");

        // в цикле находим коробки в заданной строке уровня
        for(int b=0; b<boxesSum; b++){

            // формируем тег для коробки, которую будем искать
            boxNameSB.append("boxLL_"+(b+1));

            // поиск заданной коробки
            LinearLayout boxLL = (LinearLayout) row.findViewWithTag(boxNameSB.toString());

            // если коробка найдена
            if(boxLL != null) {

                // делаем ее недоступной для выбора
                boxLL.setClickable(false);

                // ищем надпись на коробке
                TextView boxTV = (TextView) boxLL.getChildAt(0);

                // если надпись найдена
                if(boxTV != null)
                    // очищаем ее от текста
                    boxTV.setText("");
            }

            // чистим переменную хранящую тег коробки, перед следующим использованием
            boxNameSB.delete(0, boxNameSB.length());
        }
    }
}