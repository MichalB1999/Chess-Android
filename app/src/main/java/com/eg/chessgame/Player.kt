package com.eg.chessgame

import kotlin.math.sign

class Player(var color: Int) {
    private val initialRowPos = if (color == 1) 0 else 7
    val pieces: MutableMap<Int, Pair<String, Pair<Int, Int>>> = mutableMapOf(
        1 * color to Pair("King", Pair(initialRowPos, 3)), //Król
        2 * color to Pair("Queen", Pair(initialRowPos, 4)), //Hetman
        3 * color to Pair("Rook", Pair(initialRowPos, 0)), //Wieża nr 1
        4 * color to Pair("Rook", Pair(initialRowPos, 7)), //Wieża nr 2
        5 * color to Pair("Knight", Pair(initialRowPos, 1)), //Konik nr 1
        6 * color to Pair("Knight", Pair(initialRowPos, 6)), //Konik nr 2
        7 * color to Pair("Bishop", Pair(initialRowPos, 2)), //Laufer nr 1
        8 * color to Pair("Bishop", Pair(initialRowPos, 5)) //Laufer nr 2.
    )
    var availableMoves = mutableMapOf<Int, List<Pair<Int, Int>>>()
    init {
        for (i in 0..7) {
            pieces[(i + 9) * color] = Pair("Pawn", Pair(initialRowPos + color, i)) //Pionki.
        }
    } //Przypisuje wartości liczbowe dla figurek.

    fun updateAvailableMoves(board: Array<IntArray>): Unit { //Funkcja dla aktualizacja dostępnych ruchów dla gracza.
        availableMoves = mutableMapOf() //Kasuje przestarzałą informację o dostępnych ruchach.
        fun checkForObstacle(currentPos: Pair<Int, Int>, nextPos: Pair<Int, Int>): Boolean {
            val currentFig = board[currentPos.first][currentPos.second]
            val nextFig = board[nextPos.first][nextPos.second]
            return (currentFig == 0 || currentFig.sign == color) && (nextFig == 0 || nextFig.sign == -color) //Sprawdza, czy można wykonać ruch, czy są figurki na danym polu, czy ich nie ma.
        }
        fun checkIfOnBoard(pos: Pair<Int, Int>): Boolean = (0..7).contains(pos.first) && (0..7).contains(pos.second)
        fun fKing(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            val rows = ((pos.first - 1)..(pos.first + 1)).toList().filter{(0..7).contains(it)}
            val cols = ((pos.second - 1)..(pos.second + 1)).toList().filter{(0..7).contains(it)}
            rows.forEach{row ->
                cols.forEach{col ->
                    if (board[row][col] == 0) positions += Pair(row, col)
            }}
            return positions
        } //Sprawdza pozycje króla, programuje jego ruchy, sprawdza, czy nie ma przeszkód uniemożliwiających ruch oraz czy nie będzie szachowany, nadaje mu możliwość poruszania się o 1 pole od jego pozycji.

        fun fRook(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            arrayOf(0, 7).forEach{endPoint ->
                run {
                    val order = if (endPoint == 0) -1 else 1
                    var row = pos.first
                    while ((row * order < endPoint) &&
                            checkForObstacle(Pair(row, pos.second), Pair(row+order, pos.second))) {
                        row += order
                        positions += Pair(row, pos.second)
                    }
                }}
            arrayOf(0, 7).forEach { endPoint ->
                run {
                    val order = if (endPoint == 0) -1 else 1
                    var col = pos.second

                    while ((col * order < endPoint) &&
                            checkForObstacle(Pair(pos.first, col), Pair(pos.first, col+order))){
                        col += order
                        positions += Pair(pos.first, col)
                    }
                }
            }
            return positions
        } //Sprawdza pozycje wieży, programuje jego ruchy, sprawdza, czy nie ma przeszkód uniemożliwiających ruch, nadaje mu możliwość poruszania się do 7 pól od jego pozycji, poruszając się po prostej.

        fun fBishop(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            fun scanDiag1(): Unit {
            arrayOf(0, 7).forEach{endPoint ->
                run {
                    val order = if (endPoint == 0) -1 else 1
                    var row = pos.first
                    var col = pos.second
                    while ((row * order < endPoint) &&
                            (col * order < endPoint) &&
                            checkForObstacle(Pair(row, col), Pair(row+order, col+order))) {
                        row += order
                        col += order
                        positions += Pair(row, col)
                    }
                }
            }
        }
            fun scanDiag2(): Unit {
            arrayOf(Pair(0, 7), Pair(7, 0)).forEach{endPoints ->
                run {
                    val order = if (endPoints.first == 0) -1 else 1
                    var row = pos.first
                    var col = pos.second

                    while ((row*order < endPoints.first) &&
                           (col*(-1)*order < endPoints.second) &&
                            checkForObstacle(Pair(row, col), Pair(row + order, col - order))) {
                        row += order
                        col -= order
                        positions += Pair(row, col)
                    }
                }
            }
        }
            scanDiag1()
            scanDiag2()
            return positions
        } //Sprawdza pozycje laufra, programuje jego ruchy, sprawdza, czy nie ma przeszkód uniemożliwiających ruch, nadaje mu możliwość poruszania się do 7 pól od jego pozycji, poruszając się po ukosie.

        fun fQueen(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val diagonalPositions = fBishop(pos)
            val linesPositions = fRook(pos)
            return diagonalPositions.union(linesPositions).toMutableList()
        } //Dodaje do siebie ruchu wieży i laufra, by hetman poruszał się, jak oni obaj równocześnie.

        fun fKnight(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val positions = mutableListOf<Pair<Int, Int>>()
            arrayOf(Pair(1,1), Pair(-1,-1), Pair(1, -1), Pair(-1,1)).forEach{signs ->
                run {
                    positions += Pair(pos.first + 2 * signs.first, pos.second + 1 * signs.second)
                    positions += Pair(pos.first + 1 * signs.first, pos.second + 2 * signs.second)
                }
            }
            fun checkIfObstacle(pos: Pair<Int, Int>): Boolean = (board[pos.first][pos.second]).sign != color
            return positions.filter{(checkIfOnBoard(it) && checkIfObstacle(it))}.toMutableList()
        } //Sprawdza pozycje konika, programuje jego ruchy, sprawdza, czy nie ma przeszkód uniemożliwiających ruch (ale tylko na polu docelowym), nadaje mu możliwość poruszania się po "L" na planszy.

        fun fPawn(pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            val movePositions = mutableListOf<Pair<Int, Int>>()
            val nextRow = pos.first + color
            fun checkIfEnemy(pos: Pair<Int, Int>): Boolean = board[pos.first][pos.second].sign == -color
            fun checkPawnForObstacle(currentPos: Pair<Int, Int>, nextPos: Pair<Int, Int>): Boolean {
                val currentFig = board[currentPos.first][currentPos.second]
                val nextFig = board[nextPos.first][nextPos.second]
                return (currentFig == 0 || currentFig.sign == color) && (nextFig == 0)
            }
            if (pos.first == initialRowPos + color) {
                var row = pos.first
                while (checkPawnForObstacle(Pair(row, pos.second), Pair(row+color, pos.second)) &&
                        row != initialRowPos + 3*color) {
                    row += color
                    movePositions += Pair(row, pos.second)

                }
            }
            else if ((0..7).contains(nextRow) && board[nextRow][pos.second] == 0) movePositions += Pair(nextRow, pos.second)
            val attackPositions = mutableListOf(Pair(nextRow, pos.second - 1), Pair(nextRow, pos.second + 1)).filter {
                    move -> (checkIfOnBoard(move) && checkIfEnemy(move))
            }
            return movePositions.union(attackPositions).toMutableList()
        } //Sprawdza pozycje pionks, programuje jego ruchy, sprawdza, czy nie ma przeszkód uniemożliwiających ruch, nadaje mu możliwość poruszania się do 1 pole do przodu, lub 2 na starcie, oraz na jedno do przodu na ukos podczas ataku.

        fun applyFunction(name: String, pos: Pair<Int, Int>): MutableList<Pair<Int, Int>> {
            return when (name) {
                "King" -> fKing(pos)
                "Queen" -> fQueen(pos)
                "Rook" -> fRook(pos)
                "Knight" -> fKnight(pos)
                "Bishop" -> fBishop(pos)
                "Pawn" -> fPawn(pos)
                else -> mutableListOf()
            }
        } //Przypisuje zestaw ruchów do zestawu figurek.
        for ((pieceNum, piece) in pieces) {
            val pieceName = piece.first
            val piecePos = piece.second
            availableMoves[pieceNum] = applyFunction(pieceName, piecePos) //Aktualizuje możliwe ruchy.
        }
    }
}