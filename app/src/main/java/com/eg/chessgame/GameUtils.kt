package com.eg.chessgame

class GameUtils {

    /*
    * Helper class containing methods for initialization of game objects, updating their sates and
    * checking a state of the game
    */

    // Board represented as a 2d array, white figs down, black up.
    // -1 = white, 1 = black
    private fun initBoard(players: Array<Player>): Array<IntArray> {
        val board = Array(8) { IntArray(8) } //Tworzy szachownicę.
        for (player in players) {
            player.pieces.forEach { (pieceNum, piece) ->
                run {
                    val pos = piece.second
                    board[pos.first][pos.second] = pieceNum //Zmienia pozycję figurki na szachownicy.
                }
            }
        }
        return board
    }

    fun updateAllAvailableMoves(players: Map<Int, Player>, board: Array<IntArray>): Unit {
        for (player in players.values) player.updateAvailableMoves(board) //Aktualizuje możliwe do wykonania ruchy na szachownicy.
    }

    fun getAvailableMovesForPiece(pieceNum: Int, currentPlayer: Player?): List<Pair<Int, Int>> {
        return currentPlayer!!.availableMoves[pieceNum]!! //Aktualizuje możliwe ruchy dla figurki.
    }

    fun makeMove(players: Map<Int, Player>, currentPlayer: Int, board: Array<IntArray>, currentPos: Pair<Int, Int>, movePos: Pair<Int, Int>, capturedPiecesQueue: capturedQueue): Unit {
        val otherPlayer = players[currentPlayer * -1] as Player //Zmienia gracza.
        val pieceNum = board[currentPos.first][currentPos.second] // Wskazuje numer wybranej figurki (ponieważ każda ma przypisany dla siebie).
        val pieceName = players[currentPlayer]?.pieces?.get(pieceNum)!!.first
        val pieceOnMovePosition = board[movePos.first][movePos.second] //Wykonuje ruch.
        if (pieceOnMovePosition != 0) {
            val capturedPieceInfo = otherPlayer.pieces[pieceOnMovePosition]
            capturedPiecesQueue.add(Triple(pieceOnMovePosition, capturedPieceInfo!!.first, capturedPieceInfo.second))
            otherPlayer.pieces.remove(pieceOnMovePosition) //Jeśli wroga figurka zajmuje pozycje i można ja zbić, to kasuję ją z planszy.
        }
        board[movePos.first][movePos.second] = pieceNum
        board[currentPos.first][currentPos.second] = 0 //Przemieszcza figurkę gracza po planszy.
        players[currentPlayer]?.pieces!![pieceNum] = Pair(pieceName, movePos) //Aktualizuje informacje o pozycjach wigurek.
    }

    fun cancelMove(players: Map<Int, Player>, currentPlayer: Int, board: Array<IntArray>, currentPos: Pair<Int, Int>, previousPos: Pair<Int, Int>, capturedPiecesQueue: capturedQueue): Unit {
        val pieceNum = board[currentPos.first][currentPos.second]
        println("piece num to cancel: $pieceNum")//Pokazuje informacje, jaką figurkę cofnąć.
        val pieceName = players[currentPlayer]?.pieces?.get(pieceNum)!!.first
        board[previousPos.first][previousPos.second] = pieceNum
        board[currentPos.first][currentPos.second] =
            if (capturedPiecesQueue.isNotEmpty() && capturedPiecesQueue.last().third == currentPos) {
                val capturedPiece = capturedPiecesQueue.last()
                players[-1*currentPlayer]?.pieces?.set(capturedPiece.first, Pair(capturedPiece.second, capturedPiece.third)
                )
                capturedPiecesQueue.removeAt(capturedPiecesQueue.lastIndex)
                capturedPiece.first //Jeśli figurka była bita, kładzie ją spowrotem.
            }
            else 0
        players[currentPlayer]?.pieces!![pieceNum] = Pair(pieceName, previousPos) //Aktualizuje pozycje figurki.
    }
    fun isCheck(kingPos: Pair<Int, Int>, attacker: Player): Boolean {
        val attackerPossibleMoves = attacker.availableMoves
        return (attackerPossibleMoves.values.any { list -> list.contains(kingPos) }) //Sprawdza możliwe ruchy atakującego przy szachu.
    }

    fun isCheckmate(defender: Player, attacker: Player): Boolean {
        val allPossibleKingMoves = defender.availableMoves[defender.color]
        val currentKingPos = defender.pieces[defender.color]!!.second
        return (allPossibleKingMoves!! + currentKingPos).all { pos -> isCheck(pos, attacker) //Sprawdza, czy jest mat i czy król nie posiada żadnego możliwego ruchu.
        }
    }

    fun checkEnd(players: Map<Int, Player>): Int {
        return when {
            isCheckmate(players[1] as Player, players[-1] as Player) -> -1
            isCheckmate(players[-1] as Player, players[1] as Player) -> 1
            else -> 0 //Maty dla wartości -1 i 1 i pat dla 0.
        }
    }

    fun initGame(): Triple<Player, Player, Array<IntArray>> {
        val playerBlack = Player(1)
        val playerWhite = Player(-1)
        val board = initBoard(arrayOf(playerWhite, playerBlack))
        return Triple(playerBlack, playerWhite, board) //Do implementacji cofania.
    }
}
