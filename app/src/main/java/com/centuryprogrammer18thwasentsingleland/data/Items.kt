package com.centuryprogrammer18thwasentsingleland.data

import com.centuryprogrammer18thwasentsingleland.singletons.SeletedFabricare
import java.io.Serializable


// firebase required consturctor with default values
// ref) https://stackoverflow.com/a/51134246/3151712
data class Item(
    var name: String ="",
    var numPiece: Int =0
){}

data class Items(
    var items: MutableMap<String, Item> = mutableMapOf()
){}


//process can be on of [dryclean_press , wetclean_press, alteration_only]]
data class BaseItem(
    var name: String ="",
    var numPiece: Int = 0,
    var process: String ="",
    var price : Double = 0.0
): Serializable {}

data class BaseItems(
  var baseItems : MutableMap<String,BaseItem> = mutableMapOf()
){}



// process can be on of [clean_only, press_only]
data class PartialBaseItem(
    var name: String ="",
    var numPiece: Int = 0,
    var process: String ="",
    var rate: Float = 0.0f,
    var amount: Double = 0.0,
    var drycleanPressPrice : Double = 0.0,
    var wetcleanPressPrice : Double = 0.0
){}

data class PartialBaseItems(
    var partialBaseItems : MutableMap<String,PartialBaseItem> = mutableMapOf()
){}


data class DetailItem(
    var name: String ="",

    // could be alteration, general
    var category: String =""
){}


data class DetailBaseItem(
    var name: String ="",

//    {basename}_dry_clean_press
//    {basename}_wet_clean_press
//    {basename}_dry_clean
//    {basename}_wet_clean
//    {basename}_dry_press
//    {basename}_wet_press
//    {basename}_alter
    var baseItemProcess: String ="",
    var rate: Float = 0f,
    var amount: Double = 0.0
): Serializable {}


// this is used for MakeInvoiceActivity , DetailMakeInvoiceFrg
// this is shown to user to choose detail item
data class MergedDetailItem(
    var name: String="",

    // alteration , general
    var category: String ="",

    //    {basename}_dry_clean_press
//    {basename}_wet_clean_press
//    {basename}름_dry_clean
//    {basename}_wet_clean
//    {basename}_dry_press
//    {basename}_wet_press
//    {basename}_alter
    var baseItemProcess: String?= null,
    var rate: Float? = null,
    var amount: Double? = null,
    var selectedFabricare : SeletedFabricare?= null
): Serializable {}


data class ColorItem(
    var name : String ="",
    var colorNumber : Int = 0

){}

// process example) pant_dry_clean_press
data class Fabricare(

    // if two fabricares are displayed in one row, numFabricare would be 2
    // if three fabricares are displayed in one row, numFabricare would be 3
    var numFabricare : Int = 1,
    var name : String = "",
    var numPiece : Int = 1,
    var process : String = "",
    var basePrice : Double = 0.0,
    var subTotalPrice : Double = 0.0,
    var dryPrice : Double = 0.0,
    var wetPrice : Double = 0.0,
    var alterationPrice : Double = 0.0,
    var comment : String = "",
    var fabricareDetails : MutableList<FabricareDetail> = mutableListOf()
): Serializable {}

// category can be 'general' , 'alteration'
data class FabricareDetail(
    var name : String = "",
    var category: String ="",
    var price : Double = 0.0,
    var rate : Float = 0f,
    var amount : Double = 0.0
): Serializable {}


// dry_clean_press means 'dryclean and press'
// wet_clean_press means 'wetclean and press'
// dry_clean means 'dryclean clean only'
// dry_press means 'dryclean press only'
enum class FabricareProcess{
    dry_clean_press,
    wet_clean_press,
    dry_clean,
    wet_clean,
    dry_press,
    wet_press,
    alter;

    override fun toString(): String {

        val s = "_"+super.toString()

        return s
    }
}



