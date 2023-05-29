package minesweeper

import kotlin.random.Random
import java.util.Scanner

fun main() {
    println("How many mines do you want on the field?")
    val scanner = Scanner(System.`in`)
    val mines = scanner.nextInt()
    val minesField = MinesField(9, 9, mines)
    minesField.firstTimeDisplay()
    var firstTime = true
    var row: Int
    var col: Int
    var option: String
    minesField.addHints()
    while (true) {
        if (firstTime) {
            println("Set/unset mine marks or claim a cell as free:")
            col = scanner.nextInt() - 1
            row = scanner.nextInt() - 1
            option = scanner.next()
            when (option) {
                "free" -> {
                    while (minesField.field[row][col] == 'X') {
                        minesField.placeMines(mines)
                    }
                    minesField.free(row, col)
                    if (minesField.isWonByExploring()) {
                        minesField.display()
                        println("Congratulations! You found all the mines!")
                        break
                    }
                    minesField.display()
                    firstTime = false
                }
                "mine" -> {
                    if (minesField.mark(row, col)) {
                        if (minesField.isWonByMarking()) {
                            minesField.display()
                            println("Congratulations! You found all the mines!")
                            break
                        }
                    }
                    minesField.display()
                }
            }
        } else {
            println("Set/unset mine marks or claim a cell as free:")
            col = scanner.nextInt() - 1
            row = scanner.nextInt() - 1
            option = scanner.next()
            when (option) {
                "free" -> {
                    if (minesField.free(row, col).name == "BOMB") {
                        println("You stepped on a mine and failed!")
                        break
                    }
                    if (minesField.isWonByExploring()) {
                        minesField.display()
                        println("Congratulations! You found all the mines!")
                        break
                    }
                }
                "mine" -> {
                    if (minesField.mark(row, col)) {
                        if (minesField.isWonByMarking()) {
                            minesField.display()
                            println("Congratulations! You found all the mines!")
                            break
                        }
                    }
                }
            }
            minesField.display()
        }
    }
}

class MinesField(row: Int, col: Int, private val mines: Int) {
    private val fieldSize: Int = row * col
    val field = MutableList(9) {
        MutableList(9) { '.' }
    }
    private val colBombList: MutableList<Int> = arrayListOf()
    private val rowBombList: MutableList<Int> = arrayListOf()
    private val markedList: MutableList<String> = arrayListOf()
    private val displayList = MutableList(9) {
        MutableList(9) { '.' }
    }

    init {
        if (mines > fieldSize) throw IllegalArgumentException("Too many bombs for number of spaces.")
        placeMines(mines)
    }

    fun placeMines(mines: Int) {
        // Because we have 9x9 = 81 positions on the board.
        // Hence, generate a number less than the value of 81 "numberOfMines" times
        /********/
        /*
        Somehow, we can be caught in an infinite loop doing this (Uhh!)
        Because Kotlin default generator always get the same results
         */
        var numberOfMines = mines

        do {
            val defaultGenerator = Random.Default
            val col = defaultGenerator.nextInt(9)
            val row = Random.nextInt(9)
            if (field[row][col] == '.') {
                field[row][col] = 'X'
                rowBombList.add(row)
                colBombList.add(col)
                numberOfMines--
            }
        } while (numberOfMines != 0)
    }

    private fun addHint(row: Int, col: Int) {
        // board[row][col] is where the mine is placed
        val minRow: Int = if (row > 0) row - 1 else row
        val maxRow: Int = if (row < 8) row + 1 else row
        val minCol: Int = if (col > 0) col - 1 else col
        val maxCol: Int = if (col < 8) col + 1 else col
        for (i in minRow..maxRow) {
            for (j in minCol..maxCol) {
                when (field[i][j]) {
                    '.' -> field[i][j] = '1'
                    in '1'..'7' -> field[i][j]++
                }
            }
        }
    }

    fun addHints() {
        for (i in 0 until rowBombList.size) {
            addHint(rowBombList[i], colBombList[i])
        }
    }

    fun firstTimeDisplay() {
        println(
            """
                 │123456789│
                —│—————————│
                1│.........│
                2│.........│
                3│.........│
                4│.........│
                5│.........│
                6│.........│
                7│.........│
                8│.........│
                9│.........│
                —│—————————│
            """.trimIndent()
        )
    }

    fun display() {
        // Can not declare displayList = field because they both point to the same object

        // Making a copy of field

        // Displaying marked and freed position
        println(" │123456789│")
        println("—│—————————│")
        // Displaying marked cells
        if (displayList.isNotEmpty()) {
            for (i in markedList) {
                if (displayList[i.first().digitToInt()][i.last().digitToInt()] != '/') {
                    displayList[i.first().digitToInt()][i.last().digitToInt()] = '*'
                } else if (displayList[i.first().digitToInt()][i.last().digitToInt()] == '*') {
                    displayList[i.first().digitToInt()][i.last().digitToInt()] = '.'
                }
            }
        }
        for (i in 0 until displayList.size) {
            if (field[i].contains('/')) {
                for (j in kotlin.math.max(field[i].indexOf('/') - 1, 0)..kotlin.math.min(
                    field[i].lastIndexOf('/') + 1,
                    8
                )) {
                    if (field[i][j] != 'X')
                        displayList[i][j] = field[i][j]
                }
            }
        }
        for (i in 0 until displayList.size) {
            print("${i + 1}|")
            for (j in 0 until displayList[i].size) {
                print(displayList[i][j])
            }
            println('|')
        }
        println("—│—————————│")

    }

    fun mark(row: Int, col: Int): Boolean {
        return if (displayList[row][col] in '1'..'8') {
            println("There is a number here!")
            false
        } else {
            if (!markedList.remove("$row#$col")) {
                markedList.add("$row#$col")
            } else {
                displayList[row][col] = '.'
            }
            true
        }
    }


    // Started checking from the top left corner and skipping the middle cells
    fun free(row: Int, col: Int): CellStates {
        if (field[row][col] == '.' && displayList[row][col] != '/') {
            displayList[row][col] = '/'
            for (i in kotlin.math.max(row - 1, 0)..kotlin.math.min(row + 1, 8))
                for (j in kotlin.math.max(col - 1, 0)..kotlin.math.min(col + 1, 8)) {
                    if (i == row && j == col) {
                        continue
                    }
                    free(i, j)
                }
        } else if (field[row][col] in '1'..'8') {
            if (displayList[row][col] == '*') {
                markedList.remove("$row#$col")
            }
            displayList[row][col] = field[row][col]
        } else if (field[row][col] == 'X') {
            return CellStates.BOMB
        }
        return CellStates.EXPLORED
    }

    fun isWonByExploring(): Boolean {
        var count = 0
        for (i in 0..8) {
            for (j in 0..8) {
                if (displayList[i][j] == '.' || displayList[i][j] == '*')
                    count++
            }
        }
        return count == mines
    }

    fun isWonByMarking(): Boolean {
        if (markedList.size != mines) {
            return false
        } else {
            for (i in markedList) {
                if (field[i.toCharArray().first().digitToInt()][i.toCharArray().last().digitToInt()] != 'X') {
                    return false
                }
            }
            return true
        }
    }
}

enum class CellStates {
    BOMB, EXPLORED
}
