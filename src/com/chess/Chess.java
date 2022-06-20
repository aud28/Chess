package com.chess;

import com.chess.engine.board.Board;
import com.chess.gui.Table;

import javax.swing.*;

public class Chess {
    public static void main(String[] args)
    {
        Board board = Board.createsStandardBoard();

        System.out.println(board);

        Table table = new Table();
    }
}
