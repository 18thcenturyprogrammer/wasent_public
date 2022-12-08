package com.centuryprogrammer18thwasentsingleland.data

data class PrintingJob(
    // print, feed_line, half_cut, full_cut
    var type : String? = null,
    var pszString : String? = null,

    // 0-GBK  1-UTF8  3-BIG5  4-SHIFT-JIS  5-EUC-KR
    var nLan : Int? = null,
    var nOrgx : Int? = null,

    // 0 or 1
    var nWidthTimes : Int? = null,

    // 0 or 1
    var nHeightTimes : Int? = null,
    var nFontType : Int? = null,
    var nFontStyle : Int? = null
){}