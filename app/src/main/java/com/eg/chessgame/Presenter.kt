package com.eg.chessgame

import kotlin.math.sign

class Presenter(private val view: ChessboardInterface) {
    
    private var game = Game()

    // Variable of check state
    // 0: no check
    // 1: white has to move his king
    // -1: black has to move his king
    private var isCheck = 0 //Sprawdza, czy nie ma szacha.

    private var lastAvailableMoves: List<Pair<Int, Int>> = listOf() //Sprawdza poprzednie mozliwe ruchy.

    fun cancelMove() {
        game.cancelMove()
        view.redrawPieces(game.playerWhite.pieces, game.playerBlack.pieces)
    } //Cofa ruch i przywraca stan figurek sprzed ruchu.

    fun restartGame() {
        game = Game()
        view.redrawPieces(game.playerWhite.pieces, game.playerBlack.pieces)
    } //Resetuje grę i ustawia figurki na nowo.

    fun handleInput(currentPosition: Pair<Int, Int>?, previousPosition: Pair<Int, Int>?) {

        var lastSelection = 0
        if (previousPosition != null) {
            lastSelection = game.board[previousPosition.first][previousPosition.second]
        }
        val pieceNum = game.board[currentPosition!!.first][currentPosition.second]
        val currentPlayerNum = game.currentPlayerColor //Pokazuje dostępne ruchy.
        when {
            (pieceNum.sign == currentPlayerNum) -> selectPieceToMove(pieceNum, currentPlayerNum)
            (lastAvailableMoves.contains(currentPosition)
                    && lastSelection.sign == currentPlayerNum) -> movePiece(previousPosition!!, currentPosition)
            else -> view.clearSelection() //Umożliwia zmianę figurki, jeśli chcemy wykoać ruch inną.
        }
    }

    private fun selectPieceToMove(pieceNum: Int, currentPlayerNum: Int) {
        lastAvailableMoves = game.gameUtils.getAvailableMovesForPiece(pieceNum, game.players[currentPlayerNum]) //Uzyskuje mozliwe do wykonania ruchy.
        view.displayAvailableMoves(lastAvailableMoves) //Pokazuje mozliwe ruchy przy wybraniu figurki.
    }

    private fun movePiece(piecePos: Pair<Int, Int>, movePos: Pair<Int, Int>) {
        if (game.isEnd != 0) {
            view.displayWinner(game.isEnd) //Sprawdza, czy jest mat, jeśli jest, pokazuje zwycięzce.
        } else {
            game.makeMove(piecePos, movePos) //Umożliwia ruch dla gracza.
            lastAvailableMoves = listOf() //Czyści stare mozliwe ruchy.
            view.clearSelection() //Pokazuje mozliwe ruchy na planszy.
            view.redrawPieces(game.playerWhite.pieces, game.playerBlack.pieces) //Aktualizuje pozycje figurek.

            // If king of any player is in check- display a message
            if (game.isCheck[-1] == true) {
                view.displayCheck(-1) //Sprawdza, czy biały jest szachowany.
            }
            if (game.isCheck[1] == true) {
                view.displayCheck(1) //Sprawdza, czy czarny jest szachowany.
            }
            if (game.isEnd != 0) {
                view.displayWinner(game.isEnd) //Sprawdza, czy nie ma pata.
            }
        }
    }

    interface ChessboardInterface {
        fun displayAvailableMoves(movesCoordinates: List<Pair<Int, Int>>) //Funkcja pokazująca ruchy.
        fun sendInputToPresenter(currentPosition: Pair<Int, Int>?, previousPosition: Pair<Int, Int>?) //Funkcja dająca możliwośc ruchu graczowi.
        fun clearSelection() //Funkcja czyszcząca wybór.
        fun redrawPieces(whitePieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>, blackPieces: MutableMap<Int, Pair<String, Pair<Int, Int>>>) //Funkcja układająca pozycje figurek po ruchu.
        fun displayWinner(player: Int) //Funkcja pokazująca zwyciezce.
        fun displayCheck(player: Int) //Funkcja pokazująca szach.
    }
}