package com.centuryprogrammer18thwasentsingleland.utils


import android.R
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.data.*
import com.google.firebase.database.DataSnapshot
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KMutableProperty1


// change . to , in string
fun makeFirebaseSafeEmail(email:String):String{
    return email.replace(".",",")
}

fun isNumberStr(str:String):Boolean{
    var numFlag = true

    try {
        val a = java.lang.Double.parseDouble(str)
    } catch (e: NumberFormatException) {
        numFlag = false
    }
    return numFlag
}

// livedata observer which has collection don't react to adding element.
// livedata observer collection add ref) https://stackoverflow.com/a/52075248/3151712
fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}


inline fun <reified DATA_TYPE> getCollectionItemFromSnapshot(snapshot: DataSnapshot): kotlin.collections.MutableMap<String, DATA_TYPE> {
    val container = mutableMapOf<String,DATA_TYPE>()

    for (data in snapshot.children){
        container.put(data.key!!, data.getValue(DATA_TYPE::class.java)!!)
    }

    return container

}

// this function is similar to one above, but this is collect only certain type
inline fun <reified DATA_TYPE> getCollectionItemFromSnapshot(snapshot: DataSnapshot,process:String): kotlin.collections.MutableMap<String, DATA_TYPE> {
    val container = mutableMapOf<String,DATA_TYPE>()

    for (data in snapshot.children){
        val a =  data.getValue(DATA_TYPE::class.java)!! as BaseItem

        if (a.process == process){
            container.put(data.key!!, data.getValue(DATA_TYPE::class.java)!!)
        }
    }
    return container
}

// this function is similar to one above, but this is collect only certain type
inline fun <reified DATA_TYPE> getCollectionPartialItemFromSnapshot(snapshot: DataSnapshot,process:String): kotlin.collections.MutableMap<String, DATA_TYPE> {
    val container = mutableMapOf<String,DATA_TYPE>()

    for (data in snapshot.children){
        val a =  data.getValue(DATA_TYPE::class.java)!! as PartialBaseItem

        if (a.process == process){

            container.put(data.key!!, data.getValue(DATA_TYPE::class.java)!!)
        }
    }
    return container
}



fun getCurrencyString(amount: Double):String{

    var format: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    var currencyString: String = format.format(amount)
    return currencyString
}


// ref) double decimal point string format https://stackoverflow.com/a/3973886/3151712
fun makeTwoPointsDecialString(numInDouble: Double):String{
    val decimalFormat = DecimalFormat("0.00")
    val twoDecimalString = decimalFormat.format(numInDouble)

//    Log.d("etc.kt","###### twoDecimalString is ${twoDecimalString} ######")
    return twoDecimalString
}

// ref) double decimal point string format https://stackoverflow.com/a/3973886/3151712
fun makeTwoPointsDecialStringWithDollar(numInDouble: Double):String{
    val decimalFormat = DecimalFormat("0.00")
    val twoDecimalString = decimalFormat.format(numInDouble)

//    Log.d("etc.kt","###### twoDecimalString is ${twoDecimalString} ######")
    return "$"+twoDecimalString
}

// 10characters number incoming and outcome (000) 000-0000 string
fun numberStrIntoPhoneNumStr(numStr : String):String{
    val firstPart = numStr.substring(0,3)
    val secondPart = numStr.substring(3,6)
    val thirdPart = numStr.substring(6,10)

    val phoneNumStr = StringBuilder()
    phoneNumStr.append("(")
    phoneNumStr.append(firstPart)
    phoneNumStr.append(")")
    phoneNumStr.append(" ")
    phoneNumStr.append(secondPart)
    phoneNumStr.append("-")
    phoneNumStr.append(thirdPart)

    return phoneNumStr.toString()
}

// return current time string "yyyy-MM-dd HH:mm:ss" in format
fun getCurrentTimeString():String{
    //            date time format ref) https://stackoverflow.com/a/38220579/3151712
    //            date time format ref) https://stackoverflow.com/a/41507429/3151712
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Format date

    // get current datatime string
    val nowString: String = dateFormat.format(Calendar.getInstance().getTime())

    return nowString
}

// return current time string "yyyy-MM-dd" in format
fun getCurrentDateString():String{
    //            date time format ref) https://stackoverflow.com/a/38220579/3151712
    //            date time format ref) https://stackoverflow.com/a/41507429/3151712
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd") // Format date

    // get current datatime string
    val nowString: String = dateFormat.format(Calendar.getInstance().getTime())

    return nowString
}


fun getCurrentTimestamp():Long{
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Format date

    // get current datatime string
    val nowString: String = dateFormat.format(Calendar.getInstance().getTime())

    // kotlin time string timestamp ref) https://stackoverflow.com/a/6993420/3151712
    return dateFormat.parse(nowString).time
}

fun getTimestamp(timeMap: MutableMap<String,Int>): Long{

    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd") // Format date

    val timeStr =
        java.lang.StringBuilder()
            .append(timeMap["year"])
            .append("-")
            .append(timeMap["month"])
            .append("-")
            .append(timeMap["day"])
            .toString()

    // kotlin time string timestamp ref) https://stackoverflow.com/a/6993420/3151712
    return dateFormat.parse(timeStr).time
}


// check string is valid email format
// ref) https://stackoverflow.com/a/44260895/3151712
fun validEmailAddress(emailStr:String):Boolean{
    return android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr.trim()).matches()
}

// must be alpha numeric,must contain at least one symbol
// android password validation ref) https://stackoverflow.com/a/36574313/3151712
fun isValidPassword(password: String?): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
    pattern = Pattern.compile(PASSWORD_PATTERN)
    matcher = pattern.matcher(password)
    return matcher.matches()
}

fun invoiceOrderToBrief(invoiceOrder : InvoiceOrder): InvoiceOrderBrief{
    return InvoiceOrderBrief(
        invoiceOrder.id,
        invoiceOrder.createdAt,
        invoiceOrder.createdBy,
        invoiceOrder.createdAtTimestamp,
        invoiceOrder.dueDateTime,
        invoiceOrder.dueTimestamp,
        invoiceOrder.pickedUpAt,
        invoiceOrder.pickedUpBy,
        invoiceOrder.pickedAtTimestamp,
        invoiceOrder.rackLocation,
        invoiceOrder.rackedAt,
        invoiceOrder.rackedBy,
        invoiceOrder.rackedAtTimestamp,
        invoiceOrder.note,
        invoiceOrder.isVoid,
        invoiceOrder.voidAt,
        invoiceOrder.voidBy,
        invoiceOrder.voidAtTimestamp,
        invoiceOrder.priceStatement,
        invoiceOrder.qtyTable,
        invoiceOrder.orgInvoiceOrderId,
        invoiceOrder.adjustedBy,
        invoiceOrder.customer!!.id,
        invoiceOrder.customer!!.firstName,
        invoiceOrder.customer!!.lastName,
        invoiceOrder.customer!!.phoneNum,
        invoiceOrder.customer!!.email
    )
}

// make InvoiceOrderSale from InvoiceOrder
fun invoiceOrderConvertSale(invoiceOrder : InvoiceOrder): InvoiceOrderSale{
    return InvoiceOrderSale(
        invoiceOrder.id,
        invoiceOrder.createdAtTimestamp,
        invoiceOrder.rackedAtTimestamp,
        invoiceOrder.pickedAtTimestamp,
        invoiceOrder.qtyTable,
        invoiceOrder.priceStatement
    )
}

fun showDatePicker(context: Context, listener :  DatePickerDialog.OnDateSetListener, callback: DatePickerCallback? = null){
    Log.d("showDatePicker","????? showDatePicker ?????")

    // get today date
    val cal: Calendar = Calendar.getInstance()
    val year: Int = cal.get(Calendar.YEAR)
    val month: Int = cal.get(Calendar.MONTH)
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
//            val hour: Int = cal.get(Calendar.HOUR)
//            val minutes: Int = cal.get(Calendar.HOUR)
//            val ampm: Int = cal.get(Calendar.AM_PM)

// date picker for setting up due date
// date picker ref) https://stackoverflow.com/a/61865742/3151712
// select date event will be handled by viewModel
    val dialog = DatePickerDialog(context,listener, year, month, day)

// disable past dates
// disable date picker past ref) https://stackoverflow.com/a/61865742/3151712
    dialog.datePicker.minDate = cal.timeInMillis


    dialog.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        App.resourses!!.getString(R.string.cancel)
    ) { dialog, which ->
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            // cancle button clicked

            callback?.onCancelBtnClicked(true)
        }
    }
    dialog.show()
}

fun Toast.showCustomToast(context: Context, msg:String){

    this.duration = Toast.LENGTH_LONG

    val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(com.centuryprogrammer18thwasentsingleland.R.layout.custom_toast, null)
    view.findViewById<TextView>(com.centuryprogrammer18thwasentsingleland.R.id.tvCustomToast).setText(msg)
    this.view = view
    this.show()
}

fun isTeamSignedIn(sharedPrefs: SharedPreferences):Boolean{
    val signedInTeamId = sharedPrefs.getString("logged_team_id", null)

    Log.d("etc.kt","===== signedInTeamId : '${signedInTeamId}'  =====")

    return signedInTeamId != null
}

fun isManagerSignedIn(sharedPrefs: SharedPreferences):Boolean{
    val signedInTeamId = sharedPrefs.getString("logged_team_id", null)

    if(signedInTeamId != null){
        val managerId = sharedPrefs.getString("manager_id",null)
        Log.d("etc.kt","===== managerId : '${managerId}'  =====")

        managerId?.let {
            return signedInTeamId == managerId
        }
    }
    return false
}

// month 0 -11
fun someMonthsAgo(some:Int):MutableMap<String,Int> {
    val calendar = Calendar.getInstance()

    // add month , minus month ref) https://www.tutorialspoint.com/add-months-to-current-date-using-calendar-add-method-in-java
    calendar.add(Calendar.MONTH,-some)
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DATE)

    val someMonsAgo = mutableMapOf<String,Int>(
        "year" to year,
        "month" to month,
        "day" to day
    )

    Log.d("etc.kt","----- ${some.toString()} month ago ----- year : ${year.toString()} , month : ${month.toString()}, day : ${day.toString()} -----")

    return someMonsAgo
}

// month 0 - 11
fun makeStartDateTimestamp(year:Int, month: Int, day:Int):Long{

    val format = SimpleDateFormat("yyyy-MM-dd")

    // month start from 0 , so i add 1
    val startDateStr =
        java.lang.StringBuilder(year.toString())
            .append("-")
            .append(month.plus(1).toString())
            .append("-")
            .append(day.toString())
            .toString()

    val startTimestamp = format.parse(startDateStr).time

    return startTimestamp
}

// month 0 - 11
fun makeEndDateTimestamp(year:Int, month: Int, day:Int):Long{

    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    // month start from 0 , so i add 1
    // i need time stamp - day 23:59 59 , so i will add one day and minus 1
    val endDateStr =
        java.lang.StringBuilder(year.toString())
            .append("-")
            .append(month.plus(1).toString())
            .append("-")
            .append(day.toString())
            .append(" 23:59:59")
            .toString()

    val endTimestamp = format.parse(endDateStr).time

    return endTimestamp
}


//Get list of some property values of Object from list of Object
// ref) https://medium.com/@hayi/kotlin-get-list-of-some-property-values-of-object-from-list-of-object-8da9419c2e77
inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>):MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}


// firebase not allow some special characters for key
// . $ [ ] # / not allowed
// this func checks if there is not allowed character in string
fun isValidKeyCharaters(key: String):Boolean{
    Log.d("etc.kt","===== entered key string | ${key} =====")
    Log.d("etc.kt","===== key validation result | ${(!key.contains("[\\.\\$\\[\\]\\#\\/]".toRegex())).toString()} =====")

//    kotlin regex  http://zetcode.com/kotlin/regularexpressions
    return !key.contains("[\\.\\$\\[\\]\\#\\/]".toRegex())
}