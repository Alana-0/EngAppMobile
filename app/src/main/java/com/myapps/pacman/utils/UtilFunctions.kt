package com.myapps.pacman.utils

import com.myapps.pacman.utils.matrix.Matrix

fun transformIntoCharMatrix(
    strings: List<String>,
    rows: Int,
    columns: Int
): Matrix<Char> = Matrix<Char>(rows, columns)
    .apply {
        for (i in strings.indices) {
            for (j in strings[i].indices) {
                insertElement(strings[i][j], i, j)
            }
        }
    }


fun getAmountOfFood(strings: List<String>): Int {
    var amountOfFood = 0
    strings.forEach { string ->
        for (i in string.indices) {
            amountOfFood = if (string[i] == '.' || string[i] == 'o') {
                amountOfFood + 1
            } else amountOfFood
        }
    }
    return amountOfFood
}

