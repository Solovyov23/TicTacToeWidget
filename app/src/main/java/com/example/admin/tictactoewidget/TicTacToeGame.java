package com.example.admin.tictactoewidget;

import android.util.Log;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

// keeps data about a game session and manages logic
public class TicTacToeGame implements Serializable
{
    private Player turn;
    private GameState gameState = GameState.InTheProcess;

    private int fieldWidth = 3;
    private int fieldHeight = 3;
    private Player[][] gameField = new Player[fieldWidth][fieldHeight];
    private int availableCells = 0;

    private TicTacToeGameListener listener;

    public TicTacToeGame()
    {
        startTheGame();
    }

    // starts or restarts a new game
    public void startTheGame()
    {
        for(int i = 0; i < fieldWidth; i++)
            Arrays.fill(gameField[i], Player.Emptiness);
        this.turn = Player.X;
        availableCells = fieldWidth * fieldHeight;

        gameState = GameState.InTheProcess;
        if(listener != null)
            listener.onGameStateChanged(gameState, null);
    }

    public void updateTurn()
    {
        if(turn == Player.X) turn = Player.O;
        else turn = Player.X;
    }

    public boolean makeMove(int position)
    {
        // return if it's out of range
        if(availableCells == 0 || position < 0 || position > fieldHeight * fieldWidth) return false;

        int x = position % fieldWidth;
        int y = position / fieldWidth;

        //Log.e("Game", "X: " + x + ", Y: " + y);

        // or the cell is not empty
        if(gameField[x][y] != Player.Emptiness) return false;

        // place current player's sign on the board
        gameField[x][y] = turn;
        availableCells--;
        updateTurn();

        if(listener != null)
            listener.onMoveHasBeenMade();

        checkGameField();

        return true;
    }

    // check whether someone has won anything or not
    public void checkGameField()
    {
        for(int i = 0; i < fieldWidth; i++)
        {
            // vertical check
            if (gameField[i][0] == gameField[i][1] && gameField[i][1] == gameField[i][2])
            {
                if(checkWinner(gameField[i][0]))
                    return;
            }
            // horizontal check
            else if (gameField[0][i] == gameField[1][i] && gameField[1][i] == gameField[2][i])
            {
                if(checkWinner(gameField[0][i]))
                    return;
            }
            // diagonal checks
            else if ((gameField[0][0] == gameField[1][1] && gameField[1][1] == gameField[2][2])
                    || (gameField[0][2] == gameField[1][1] && gameField[1][1] == gameField[2][0]))
            {
                if(checkWinner(gameField[1][1]))
                    return;
            }
        }

        // it's a tie
        if(availableCells == 0)
        {
            gameState = GameState.Ended;
            if(listener != null)
                listener.onGameStateChanged(gameState, Player.Emptiness);
        }
    }

    private boolean checkWinner(Player winner)
    {
        if(winner == Player.Emptiness) return false;

        gameState = GameState.Ended;
        if(listener != null)
            listener.onGameStateChanged(gameState, winner);

        return true;
    }

    public Player getTurn()
    {
        return turn;
    }

    public GameState getGameState()
    {
        return gameState;
    }

    public Player[][] getGameField()
    {
        return gameField;
    }

    public int getFieldWidth()
    {
        return fieldWidth;
    }

    public int getFieldHeight()
    {
        return fieldHeight;
    }


    public TicTacToeGameListener getListener()
    {
        return listener;
    }

    public void setListener(TicTacToeGameListener listener)
    {
        this.listener = listener;
    }

    // listener interface for the game
    public interface TicTacToeGameListener
    {
        void onGameStateChanged(GameState newState, Object object);
        void onMoveHasBeenMade();
    }

    public enum Player
    {
        X,
        O,
        Emptiness
    }

    public enum GameState
    {
        InTheProcess,
        Ended
    }
}