package com.graduation.vitlog_android.util.number

object TimeUtil {
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

    fun Double.toMillis(): Int {
        val seconds = this.toInt() // 초 부분을 추출합니다.
        val milliseconds = ((this - seconds) * 1000).toInt() // 소수 부분을 밀리초로 변환합니다.
        return (seconds * 1000) + milliseconds // 전체 시간을 밀리초로 변환하여 반환합니다.
    }


}