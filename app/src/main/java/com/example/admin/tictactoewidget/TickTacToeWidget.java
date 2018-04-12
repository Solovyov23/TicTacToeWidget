package com.example.admin.tictactoewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Implementation of App Widget functionality.
 */
public class TickTacToeWidget extends AppWidgetProvider
{
    // when user makes a move
    final static String ACTION_CLICK = "super_action";
    final static String CELL_NUM = "cell_number";
    private static final String LOG_TAG = "Tick Tac Toe Widget";

    // keeps status of every game
    private static HashMap<Integer, TicTacToeGame> gameStats = new HashMap<>();

    private static final int[] button_ids = new int[]{ R.id.first, R.id.second, R.id.third, R.id.fourth, R.id.fifth, R.id.sixth, R.id.seventh, R.id.eight, R.id.ninth};

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, final int appWidgetId)
    {
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tick_tac_toe_widget);

        final TicTacToeGame game = new TicTacToeGame();
        // set listener
        game.setListener(new TicTacToeGame.TicTacToeGameListener()
        {
            @Override
            public void onGameStateChanged(TicTacToeGame.GameState newState, Object object)
            {
                if(newState == TicTacToeGame.GameState.InTheProcess)
                {
                    showHideTextView(false, "");
                    // refresh status bar
                    updateStatusViewText(context.getResources().getString(R.string.ticTacToe) + ": " + game.getTurn() );
                    drawGameField();
                }
                else if(newState == TicTacToeGame.GameState.Ended)
                {
                    updateStatusViewText(context.getResources().getString(R.string.ticTacToe));
                    showHideTextView(true, object + " is the winner!");
                    Log.d(LOG_TAG, "Game ended!!!");
                }
            }

            @Override
            public void onMoveHasBeenMade()
            {
                // refresh status bar
                updateStatusViewText(context.getResources().getString(R.string.ticTacToe) + ": " + game.getTurn() );
                drawGameField();
            }

            void showHideTextView(boolean show, String text)
            {
                views.setViewVisibility(R.id.mainTextView, show? View.VISIBLE: View.GONE);

                if(show) views.setTextViewText(R.id.mainTextView, text);

                // update views
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            void updateStatusViewText(String text)
            {
                views.setTextViewText(R.id.statusView, text);

                // update views
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            void drawGameField()
            {
                TicTacToeGame.Player[][] gameField = game.getGameField();
                int fieldWidth = game.getFieldWidth();
                int fieldHeight = game.getFieldHeight();

                // redraw the board
                for(int x = 0; x < fieldWidth; x++)
                {
                    for(int y = 0; y < fieldHeight; y++)
                    {
                        // cell image resource
                        int imgResource = -1;
                        if (gameField[x][y] == TicTacToeGame.Player.X) imgResource = R.drawable.cross;
                        else if (gameField[x][y] == TicTacToeGame.Player.O) imgResource = R.drawable.naught;
                        else imgResource = 0;

                        if (imgResource != -1)
                            views.setImageViewResource(button_ids[x + fieldWidth * y], imgResource);
                    }
                }

                // update views
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
        gameStats.put(appWidgetId, game);

        // loop through the buttons and set click intents
        for(int i = 0; i < 9; i++)
        {
            int id = button_ids[i];

            Intent clickIntent = new Intent(context, TickTacToeWidget.class);

            clickIntent.setAction(id+"");
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            clickIntent.putExtra(CELL_NUM, i);

            PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
            views.setOnClickPendingIntent(id, pIntent);
        }

        Intent clickIntent = new Intent(context, TickTacToeWidget.class);

        clickIntent.setAction(ACTION_CLICK);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
        views.setOnClickPendingIntent(R.id.mainTextView, pIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            gameStats.remove(appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context){}

    @Override
    public void onDisabled(Context context)
    {
        Log.e(LOG_TAG, "Disabled!!!");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        // remove game stats from the list
        for(int id: appWidgetIds)
        {
            gameStats.remove(id);
            Log.d(LOG_TAG, id + " Deleted!");
        }
    }

    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        int cellId = -1;
        for(int id: button_ids)
        {
            if (intent.getAction().equalsIgnoreCase(id+""))
            {
                cellId = id;
            }
        }

        // a button has been clicked
        if(cellId != -1)
        {
            Bundle extras = intent.getExtras();

            if (extras == null) return;

            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            int cellPos = extras.getInt(CELL_NUM, -1);

            //Log.e(LOG_TAG, "Cell: " + cellPos);

            TicTacToeGame game = gameStats.get(appWidgetId);

            // make a move
            game.makeMove(cellPos);

            //updateAppWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
        }
        else if(intent.getAction().equalsIgnoreCase(ACTION_CLICK))
        {
            Bundle extras = intent.getExtras();
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            TicTacToeGame game = gameStats.get(appWidgetId);
            game.startTheGame();

            //Log.e(LOG_TAG, "Text Click");
        }
    }
}

