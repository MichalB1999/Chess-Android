package com.eg.chessgame

class Game {
    val gameUtils = GameUtils() //Umożliwia działanie funkcji z innej klasy.
    private val chessObjects = gameUtils.initGame() //Wczytuje grę.
    val capturedPiecesQueue: capturedQueue = mutableListOf()

    val playerBlack = chessObjects.first //Przypisuje pierwszy zestaw figurek dla białego gracza.
    val playerWhite = chessObjects.second //Przypisuje drugi zestaw figurek dla czarnego gracza.
    val board = chessObjects.third //Wyodrębnia szachownice.

    val players: Map<Int, Player> = mapOf(-1 to playerWhite, 1 to playerBlack) //Przypisuje wartości, by wyodrębnić gracza.

    var isEnd = 0 //Pat.
    val isCheck = mutableMapOf<Int, Boolean>(-1 to false, 1 to false)
    var currentPlayerColor = -1  //Ustawia pierwszego gracza, jako białego.

    private var lastMoveCurrentPos: Pair<Int, Int>? = null //Dla cofania.
    private var lastMovePreviousPos: Pair<Int, Int>? = null

    init {
        gameUtils.updateAllAvailableMoves(players, board) //Sprawdza wszystkie możliwe ruchy.
    }

    fun cancelMove() {
        if (lastMovePreviousPos != null && lastMoveCurrentPos != null) {
            println("previoues pos: $lastMovePreviousPos, current pos: $lastMoveCurrentPos") //Zamiana starej pozycji na nową.
            currentPlayerColor *= -1 //Zmiana gracza przy cofaniu ruchu.
            gameUtils.cancelMove(players, currentPlayerColor, board, lastMoveCurrentPos as Pair, lastMovePreviousPos as Pair, capturedPiecesQueue) //Cofa wszystkie zmiany zwiazane z dokonaniem ruchu.
            gameUtils.updateAllAvailableMoves(players, board) //Sprawdza wszystkie możliwe ruchy.
            isEnd = gameUtils.checkEnd(players)
            lastMoveCurrentPos = null
            lastMovePreviousPos = null
        }
    }

    fun makeMove(piecePos: Pair<Int, Int>, movePos: Pair<Int, Int>) {
        gameUtils.makeMove(players, currentPlayerColor, board, piecePos, movePos, capturedPiecesQueue) //Przeprowadza wszystkie zmiany wykonane z wykonaniem ruchu.
        gameUtils.updateAllAvailableMoves(players, board) //Sprawdza wszystkie możliwe ruchy.

        for (p in players[-1*currentPlayerColor]!!.availableMoves) {
            println("$p")
        }

        isCheck[currentPlayerColor] = gameUtils.isCheck(players[currentPlayerColor]!!.pieces[currentPlayerColor]!!.second, players[-1*currentPlayerColor] as Player)
        isCheck[-1*currentPlayerColor] = gameUtils.isCheck(players[-1*currentPlayerColor]!!.pieces[-1*currentPlayerColor]!!.second, players[currentPlayerColor] as Player) //Sprawdza szachowanie.

        for (k in isCheck) {
            println("$k") //Informacja o szachowaniu.
        }

        if (isCheck[currentPlayerColor] == true) {
            gameUtils.cancelMove(players, currentPlayerColor, board, movePos, piecePos, capturedPiecesQueue)
            gameUtils.updateAllAvailableMoves(players, board) //Sprawdzanie, czy król wykonał prawidłowy ruch w trakcie szachu.
        }
        else {
            lastMoveCurrentPos = movePos
            lastMovePreviousPos = piecePos
            currentPlayerColor *= -1 //Zmiana gracza.
            isEnd = gameUtils.checkEnd(players) //Mat.
        }
    }
}
