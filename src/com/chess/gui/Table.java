package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.*;
import static javax.swing.SwingUtilities.*;

public class Table {

    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private Board chessBoard;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;

    private static String defaultPieceImagesPath = "art/pieces/plain/";
    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600,600);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);

    private final Color LightTileColor = Color.decode("#FFFACD");
    private final Color darkTileColor = Color.decode("#593E1A");

    public Table()
    {
        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessBoard = Board.createsStandardBoard();
        this.boardPanel = new BoardPanel();
        this.boardDirection = BoardDirection.NORMAL;
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.setVisible(true);
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("open up that pgn file!");
            }
        });
        fileMenu.add(openPGN);
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        return fileMenu;
    }

    public enum BoardDirection
    {
        NORMAL {
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    private JMenu createPreferencesMenu()
    {
        final JMenu preferencesMenu = new JMenu("preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);
        return preferencesMenu;
    }

    private class BoardPanel extends JPanel
    {
        final List<TilePanel> boardTiles;

        BoardPanel()
        {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for(int i = 0; i < BoardUtils.NUM_TILES; i++)
            {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board)
        {
            removeAll();
            for(final TilePanel tilePanel : boardDirection.traverse(boardTiles))
            {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    private class TilePanel extends JPanel
    {
        private final int tileID;

        TilePanel(final BoardPanel boardPanel,
                  final int tileID)
        {
            super(new GridBagLayout());
            this.tileID = tileID;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if(isRightMouseButton(e))
                    {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    }
                    else if(isLeftMouseButton(e))
                    {
                        if(sourceTile == null)
                        {
                            sourceTile = chessBoard.getTile(tileID);
                            humanMovedPiece = sourceTile.getPiece();
                            if(humanMovedPiece == null)
                            {
                                sourceTile = null;
                            }
                        }
                        else
                        {
                            destinationTile = chessBoard.getTile(tileID);
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate());
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if(transition.getMoveStatus().isDone())
                            {
                                chessBoard = transition.getTransitionBoard();
                            }
                            sourceTile = null;
                            humanMovedPiece = null;
                        }
                    }
                }

                @Override
                public void mousePressed(final MouseEvent e) {

                }

                @Override
                public void mouseReleased(final MouseEvent e) {

                }

                @Override
                public void mouseEntered(final MouseEvent e) {

                }

                @Override
                public void mouseExited(final MouseEvent e) {

                }
            });
            validate();
        }

        public void drawTile(final Board board)
        {
            assignTileColor();
            assignTilePieceIcon(board);
            validate();
            repaint();
        }

        private void assignTilePieceIcon(final Board board)
        {
            this.removeAll();
            if(board.getTile(this.tileID).isTileOccupied())
            {
                try {
                    final BufferedImage image =
                            ImageIO.read(new File(defaultPieceImagesPath + board.getTile(this.tileID).getPiece().getPieceAlliance().toString().substring(0,1) +
                            board.getTile(this.tileID).getPiece().toString() + ".gif"));
                    add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void assignTileColor() {
            if (BoardUtils.EIGHTH_RANK[this.tileID] ||
                BoardUtils.SIXTH_RANK[this.tileID] ||
                BoardUtils.FOURTH_RANK[this.tileID] ||
                BoardUtils.SECOND_RANK[this.tileID])
            {
                setBackground(this.tileID % 2 == 0 ? LightTileColor : darkTileColor);
            } else if (BoardUtils.SEVENTH_RANK[this.tileID] ||
                       BoardUtils.FIFTH_RANK[this.tileID] ||
                       BoardUtils.THIRD_RANK[this.tileID] ||
                       BoardUtils.FIRST_RANK[this.tileID])
            {
                setBackground(this.tileID % 2 != 0 ? LightTileColor : darkTileColor);
            }
        }
    }
}
