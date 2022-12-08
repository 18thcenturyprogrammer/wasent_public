package com.centuryprogrammer18thwasentsingleland.utils

import android.util.Log
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.lvrenyang.io.Pos
import kotlin.experimental.and

class GenstarPrint() {
//    private val TAG = GenstarPrint::class.java.simpleName

    companion object {
        fun printTicket(
            pos: Pos,
            nPrintWidth: Int,
            bCutter: Boolean,
            bDrawer: Boolean,
            bBeeper: Boolean,
            nCount: Int,
            printingJobs : MutableList<PrintingJob>
        ): Int {
            val TAG = "GenstarPrint companion"

            Log.d(TAG,"+++++ printTicket() is started +++++")
            Log.d(TAG,"+++++ printingJobs : ${printingJobs.toString()} +++++")


            var bPrintResult = -8

            var status = ByteArray(1)

            // check doc POS_RTQueryStatus() table 3
            // checking printer status (hardware condition)
            if (pos.POS_RTQueryStatus(status, 3, 1000, 2)) {

                Log.d(TAG, "+++++ OS_RTQueryStatus() table 3 +++++")

                //判断切刀是否异常  Determine whether the cutter is abnormal
                if (status[0] and 0x08 == 0x08.toByte()) {

                    // cutter has a problem

                    Log.d(TAG, "+++++  cutter has a problem +++++")

                    return -2.also { bPrintResult = it }
                }


                if (status[0] and 0x40 == 0x40.toByte()) {
                    // head temperature problem or voltage is out of range

                    Log.d(TAG, "+++++  head temperature problem or voltage is out of range +++++")

                    return -3.also { bPrintResult = it }
                }

                Log.d(TAG, "+++++ before OS_RTQueryStatus() table 2 +++++")

                // check doc POS_RTQueryStatus() table 2
                // checking printer status (hardware condition)
                if (pos.POS_RTQueryStatus(status, 2, 1000, 2)) {

                    Log.d(TAG, "+++++ OS_RTQueryStatus() table 2 +++++")

                    if (status[0] and 0x04 == 0x04.toByte()) {
                        // upper cover problem (probably upper cover is opened)

                        Log.d(
                            TAG,
                            "+++++ upper cover problem (probably upper cover is opened) +++++"
                        )
                        return -6.also { bPrintResult = it }
                    }

                    if (status[0] and 0x20 == 0x04.toByte()) {
                        // printer is getting out of paper

                        Log.d(TAG, "+++++ printer is getting out of paper +++++")

                        return -5.also { bPrintResult = it }
                    } else {

                        if (pos.GetIO().IsOpened()) {
                            pos.POS_Reset()

                            for (item in printingJobs){
                                when(item.type){
                                    "print" -> {
                                        pos.POS_TextOut(
                                            item.pszString,
                                            item.nLan!!,
                                            item.nOrgx!!,
                                            item.nWidthTimes!!,
                                            item.nHeightTimes!!,
                                            item.nFontType!!,
                                            item.nFontStyle!!
                                        )
                                    }
                                    "feed_line" -> {
                                        pos.POS_FeedLine()
                                    }
                                    "half_cut" -> {
                                        pos.POS_HalfCutPaper()
                                    }
                                    "full_cut" -> {
                                        pos.POS_FullCutPaper()
                                    }
                                    "center" -> {
                                        pos.POS_S_Align(1)
                                    }
                                    "left" -> {
                                        pos.POS_S_Align(0)
                                    }
                                    "right" -> {
                                        pos.POS_S_Align(2)
                                    }
                                }
                            }
                        }
                        return 0
                    }

                }else{
                    // query failed

                    Log.d(TAG,"+++++ query failed - POS_RTQueryStatus() table 2 +++++")

                    return -11.also { bPrintResult = it }
                }
            } else {
                // query failed

                Log.d(TAG,"+++++ query failed - POS_RTQueryStatus() table 3 +++++")

                return -11.also { bPrintResult = it }
            }
            return 0

        }

        fun makeSmallPrintingJob(content:String): PrintingJob{

            return PrintingJob("print", content, 1, 0, 0, 0, 0, 0)
        }

        fun makeLargePrintingJob(content:String):PrintingJob{
            return PrintingJob("print", content, 1, 0, 1, 1, 0, 0)
        }

        fun makeSmallBoldPrintingJob(content:String): PrintingJob{

            return PrintingJob("print", content, 1, 0, 0, 0, 0, 8)
        }

        fun makeLargeBoldPrintingJob(content:String):PrintingJob{
            return PrintingJob("print", content, 1, 0, 1, 1, 0, 8)
        }

        fun makeFeedLinePrintingJob():PrintingJob{
            return PrintingJob("feed_line")
        }

        fun makeHalfCutPrintingJob():PrintingJob{
            return PrintingJob("half_cut")
        }

        fun makeFullCutPrintingJob():PrintingJob{
            return PrintingJob("full_cut")
        }

        fun makeCenterAlignPrintingJob():PrintingJob{
            return PrintingJob("center")
        }

        fun makeLeftAlignPrintingJob():PrintingJob{
            return PrintingJob("left")
        }

        fun makeRightAlignPrintingJob():PrintingJob{
            return PrintingJob("right")
        }




    }
}

// check if priter is out of paper or not

// this will be needed before second printing job
//        // check if priter is out of paper or not
//        if ((status.get(0) and 0x20) == 0x20) //打印过程缺纸
//            return -9.also( { bPrintResult = it })