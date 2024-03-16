package com.graduation.vitlog_android.util.number

object TimeStamp {
    fun formatTimeStamp(a: Double, b: Double): String {
        var formattedA = String.format("%.2f", a)
        var formattedB = String.format("%.2f", b)

        // 한 자리 수 앞에 0 추가
        if (formattedA.length == 4) {
            formattedA = "0$formattedA"
        }
        if (formattedB.length == 4) {
            formattedB = "0$formattedB"
        }

        return "$formattedA ~ $formattedB"
    }
}