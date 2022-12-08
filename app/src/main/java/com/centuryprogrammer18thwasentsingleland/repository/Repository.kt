package com.centuryprogrammer18thwasentsingleland.repository

import android.content.SharedPreferences
import android.util.Log
import com.centuryprogrammer18thwasentsingleland.data.*
import com.centuryprogrammer18thwasentsingleland.utils.invoiceOrderConvertSale
import com.centuryprogrammer18thwasentsingleland.utils.invoiceOrderToBrief
import com.centuryprogrammer18thwasentsingleland.utils.makeFirebaseSafeEmail
import com.centuryprogrammer18thwasentsingleland.utils.numberStrIntoPhoneNumStr
import com.google.firebase.FirebaseError
import com.google.firebase.database.*


object Repository {
    private val TAG = Repository::class.java.simpleName

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private var ownerEmail: String? = null
    private var emailForFirebaseChild: String? = null
    private var cleanerName: String? = null

    fun saveCleanerInitSetting(
        cleanerInitSetting: CleanerInitSetting,
        callback: AddCleanerInitSettingCallbacks
    ){
        firebaseDatabase.getReference("cleaners").child("cleaners_initsettings").child(
            makeFirebaseSafeEmail(cleanerInitSetting.ownerEmail!!)).child(cleanerInitSetting.cleanerName!!).setValue(cleanerInitSetting)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onAddCleanerInitSettingCall(true, cleanerInitSetting)
                } else {
                    callback.onAddCleanerInitSettingCall(false, cleanerInitSetting)
                }
            }
    }

//old version before using transaction
//
//    //ref) callbacke in callback https://stackoverflow.com/a/51595202/3151712
//    //add new customer into firebase real time database
//    fun addCustomer(
//        sharedPrefs: SharedPreferences,
//        customer: Customer,
//        callback: AddCustomerCallbacks
//    ) {
//
//        val customerId = getCleanerRef(sharedPrefs).child("customers").push().key
//        val customerWithId = customer
//        customerWithId.id = customerId
//
//        getCleanerRef(sharedPrefs).child("customers").child(customerId!!).setValue(customerWithId)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    callback.onAddCustomerCall(true, customerWithId)
//                } else {
//                    callback.onAddCustomerCall(false, customerWithId)
//                }
//            }
//    }

    fun addCustomer(
        sharedPrefs: SharedPreferences,
        customer: Customer,
        callback: AddCustomerCallbacks
    ) {
//      transaction firebase serial id key ref) https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions
//      transaction firebase serial id key ref) https://stackoverflow.com/a/28915836/3151712

        getCleanerRef(sharedPrefs).child("customer_id").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentCustomerId: MutableData): Transaction.Result {
                Log.d(TAG, "===== doTransaction :currentInvoiceId : '${currentCustomerId.toString()}' =====")
                Log.d(
                    TAG,
                    "===== doTransaction :currentInvoiceId.value : '${currentCustomerId.value.toString()}' ====="
                )

                if (currentCustomerId.value == null) {
                    currentCustomerId.value = 1000
                } else {

                    // firesbase stored invoice_id in Long

                    val newCustomerId = (currentCustomerId.value as Long) + 1
                    Log.d(
                        TAG,
                        "===== doTransaction : newVal : '${newCustomerId.toInt().toString()}' ====="
                    )

                    currentCustomerId.value = newCustomerId

                    val customerWithId = customer
                    customerWithId.id = newCustomerId.toString()

                    getCleanerRef(sharedPrefs).child("customers").child(newCustomerId.toString()).setValue(customerWithId)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                callback.onAddCustomerCall(true, customerWithId)
                            } else {
                                callback.onAddCustomerCall(false, customerWithId)
                            }
                        }
                }
                return Transaction.success(currentCustomerId)
            }

            override fun onComplete(
                firebaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (firebaseError != null) {
                    Log.e(TAG, "There is problem adding to customer_id and save")
                } else {
                    Log.d(TAG, "customer_id increased by 1 Successfully")
                }
            }
        })
    }






    fun editCustomer(
        sharedPrefs:SharedPreferences ,
        customer:Customer,
        callback: AddCustomerCallbacks){

        getCleanerRef(sharedPrefs).child("customers").child(customer.id!!).setValue(customer)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onAddCustomerCall(true, customer)
                } else {
                    callback.onAddCustomerCall(false, customer)
                }
            }

    }


    // this can do add, update
    fun updateAllItems(
        sharedPrefs: SharedPreferences,
        items: MutableMap<String, Item>,
        baseItems: MutableMap<String, BaseItem>,
        partialBaseItems: MutableMap<String, PartialBaseItem>,
        callback: UpdateAllItemsCallback
    ) {
        Log.d(TAG, "===== update Items BaseItems PartialBaseItems=====")

        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/item" to items,
            "/base_item" to baseItems,
            "/partial_base_item" to partialBaseItems
        )

        getCleanerRef(sharedPrefs).child("items").updateChildren(childUpdate)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onUpdateAllItemsCall(true)
                } else {
                    callback.onUpdateAllItemsCall(false)
                }
            }

    }

    fun updateDetailItem(
        sharedPrefs: SharedPreferences,
        items: MutableMap<String, DetailItem>,
        callback: UpdateDetailItemCallback
    ) {
        Log.d(TAG, "===== update Detail item=====")

        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/detail_item" to items
        )

        getCleanerRef(sharedPrefs).child("items").updateChildren(childUpdate)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onUpdateDetailItemCall(true)
                } else {
                    callback.onUpdateDetailItemCall(false)
                }
            }

    }

    fun deleteItem(sharedPrefs: SharedPreferences, name: String, callback: DeleteItemCallback) {
        val childUpdate: HashMap<String, Any?> = hashMapOf(
            "/item/${name}" to null,
            "/base_item/${name + "_dryclean_press"}" to null,
            "/base_item/${name + "_wetclean_press"}" to null,
            "/base_item/${name + "_alteration_only"}" to null,
            "/partial_base_item/${name + "_clean_only"}" to null,
            "/partial_base_item/${name + "_press_only"}" to null
        )

        getCleanerRef(sharedPrefs).child("items").updateChildren(childUpdate)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onDeleteItemCallback(true)
                } else {
                    callback.onDeleteItemCallback(false)
                }
            }
    }

    fun deleteDetailItem(
        sharedPrefs: SharedPreferences,
        name: String,
        callback: DeleteDetailItemCallback
    ) {
        val childUpdate: HashMap<String, Any?> = hashMapOf(
            "/detail_item/${name}" to null
        )

        getCleanerRef(sharedPrefs).child("items").updateChildren(childUpdate)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onDeleteDetailItemCallback(true)
                } else {
                    callback.onDeleteDetailItemCallback(false)
                }
            }
    }


    fun updateBaseItemsPartialBaseItems(
        sharedPrefs: SharedPreferences,
        baseItems: MutableMap<String, BaseItem>,
        partialBaseItems: MutableMap<String, PartialBaseItem>,
        callback: UpdateBaseItemPartialBaseItemCallback
    ) {
        Log.d(TAG, "===== update ALL BaseItems PartialBaseItems=====")

        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/base_item" to baseItems,
            "/partial_base_item" to partialBaseItems
        )

        getCleanerRef(sharedPrefs).child("items").updateChildren(childUpdate)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onUpdateBaseItemPartialBaseItemCallback(true)
                } else {
                    callback.onUpdateBaseItemPartialBaseItemCallback(false)
                }
            }

    }

    fun updatePartialBaseItems(
        sharedPrefs: SharedPreferences,
        partialBaseItems: MutableMap<String, PartialBaseItem>,
        callback: UpdatePartialBaseItemsCallback
    ) {
        Log.d(TAG, "===== update PartialBaseItems=====")

        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/partial_base_item" to partialBaseItems
        )

        getCleanerRef(sharedPrefs).child("items").updateChildren(childUpdate)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onUpdatePartialBaseItemsCallback(true)
                } else {
                    callback.onUpdatePartialBaseItemsCallback(false)
                }
            }

    }


    fun addDetailBaseItem(
        sharedPrefs: SharedPreferences,
        newDetailBaseItem: DetailBaseItem,
        callback: AddDetailBaseItemCallback
    ) {
        getCleanerRef(sharedPrefs).child("items").child("detail_base_item")
            .child(newDetailBaseItem.name + "_" + newDetailBaseItem.baseItemProcess)
            .setValue(newDetailBaseItem).addOnCompleteListener {
            if (it.isSuccessful) {
                callback.onAddDetailBaseItemCallback(true)
            } else {
                callback.onAddDetailBaseItemCallback(false)
            }
        }
    }

    fun deleteDetailBaseItem(
        sharedPrefs: SharedPreferences,
        pushKey: String,
        callback: DeleteDetailBaseItemCallback
    ) {
        getCleanerRef(sharedPrefs).child("items").child("detail_base_item")
            .child(pushKey)
            .setValue(null).addOnCompleteListener {
                if (it.isSuccessful) {
                    callback.onDeleteDetailBaseItemCallback(true)
                } else {
                    callback.onDeleteDetailBaseItemCallback(false)
                }
            }
    }


    fun deleteBaseItem(
        sharedPrefs: SharedPreferences,
        baseItemName: String,
        callback: RemoveItemCallback
    ) {

        getCleanerRef(sharedPrefs).child("items").child("base_item").child("base_item_collection")
            .child(baseItemName).removeValue(object : DatabaseReference.CompletionListener {
            override fun onComplete(error: DatabaseError?, dataRef: DatabaseReference) {
                if (error == null) {
                    Log.d(TAG, "===== firebase says deleting BaseItem succeed =====")
                    callback.onDeleteItemCallback(true)
                } else {
                    // error happened
                    Log.d(
                        TAG,
                        "===== error while deleting BaseItem ===== error is '{${error.message}}'====="
                    )
                    callback.onDeleteItemCallback(false)
                }
            }
        })

    }


    fun addValueEventOnWholeItems(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnItemCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnItemCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnItemCallback(snapshot, null)
                }
            })

        return valueEventListener
    }


    fun addValueEventOnItem(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnItemCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items").child("item")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnItemCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnItemCallback(snapshot, null)
                }
            })

        return valueEventListener
    }


    fun addValeEventOnBaseItem(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnBaseItemCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items").child("base_item")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnBaseItemCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnBaseItemCallback(snapshot, null)
                }
            })

        return valueEventListener
    }

    fun addValeEventOnBaseItemByPushKey(
        pushKey:String,
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnBaseItemCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items").child("base_item").child(pushKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnBaseItemCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnBaseItemCallback(snapshot, null)
                }
            })

        return valueEventListener
    }

    fun addValeEventOnPartialBaseItem(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnPartialBaseItemCallback
    ): ValueEventListener {
        val valueEventListener =
            getCleanerRef(sharedPrefs).child("items").child("partial_base_item")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "===== valueEventListener onCancelled =====")
                        callback.onValueEventOnPartialBaseItemCallback(null, error)
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "===== valueEventListener onDataChange =====")
                        Log.d(
                            TAG,
                            "===== snapshot.exists() is '${snapshot.exists().toString()}' ====="
                        )
                        callback.onValueEventOnPartialBaseItemCallback(snapshot, null)
                    }
                })

        return valueEventListener
    }

    fun addValeEventOnPartialBaseItemByPushKey(
        pushKey: String,
        typeClean: String,
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnPartialBaseItemByPushKeyCallback
    ): ValueEventListener {
        val valueEventListener =
            getCleanerRef(sharedPrefs).child("items").child("partial_base_item").child(pushKey)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "===== valueEventListener onCancelled =====")
                        callback.onValueEventOnPartialBaseItemByPushKeyCallback(null, error, typeClean)
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "===== valueEventListener onDataChange =====")
                        Log.d(
                            TAG,
                            "===== snapshot.exists() is '${snapshot.exists().toString()}' ====="
                        )
                        callback.onValueEventOnPartialBaseItemByPushKeyCallback(snapshot, null, typeClean)
                    }
                })

        return valueEventListener
    }



    fun addValueEventOnItems(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnItemsCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnItemsCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnItemsCallback(snapshot, null)
                }
            })

        return valueEventListener
    }

    fun addValueEventOnDetailItem(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnDetailItemCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items").child("detail_item")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnDetailItemCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnDetailItemCallback(snapshot, null)
                }
            })

        return valueEventListener
    }


    fun addValueEventOnDetailBaseItems(
        sharedPrefs: SharedPreferences,
        callback: ValueEventOnItemCallback
    ): ValueEventListener {
        val valueEventListener = getCleanerRef(sharedPrefs).child("items").child("detail_base_item")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "===== valueEventListener onCancelled =====")
                    callback.onValueEventOnItemCallback(null, error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "===== valueEventListener onDataChange =====")
                    Log.d(TAG, "===== snapshot.exists() is '${snapshot.exists().toString()}' =====")
                    callback.onValueEventOnItemCallback(snapshot, null)
                }
            })

        return valueEventListener
    }


    fun removeValueEventOnWholeItems(
        sharedPrefs: SharedPreferences,
        valueEventListener: ValueEventListener
    ) {
        Log.d(TAG, "===== Value Event Listener was removed  =====")
        getCleanerRef(sharedPrefs).child("items").removeEventListener(valueEventListener)
    }

    fun removeValueEventOnItem(
        sharedPrefs: SharedPreferences,
        valueEventListener: ValueEventListener
    ) {
        Log.d(TAG, "===== Value Event Listener was removed  =====")
        getCleanerRef(sharedPrefs).child("items").child("item")
            .removeEventListener(valueEventListener)
    }

    fun removeValueEventOnBaseItem(
        sharedPrefs: SharedPreferences,
        valueEventListener: ValueEventListener
    ) {
        Log.d(TAG, "===== Value Event Listener was removed  =====")
        getCleanerRef(sharedPrefs).child("items").child("base_item")
            .removeEventListener(valueEventListener)
    }

    fun removeValueEventOnItems(
        sharedPrefs: SharedPreferences,
        valueEventListener: ValueEventListener
    ) {
        Log.d(TAG, "===== Value Event Listener was removed  =====")
        getCleanerRef(sharedPrefs).child("items").removeEventListener(valueEventListener)
    }

    fun removeValueEventOnPartialBaseItem(
        sharedPrefs: SharedPreferences,
        valueEventListener: ValueEventListener
    ) {
        Log.d(TAG, "===== Value Event Listener was removed  =====")
        getCleanerRef(sharedPrefs).child("items").child("partial_base_item")
            .removeEventListener(valueEventListener)
    }

    fun removeValueEventOnDetailItem(
        sharedPrefs: SharedPreferences,
        valueEventListener: ValueEventListener
    ) {
        Log.d(TAG, "===== Value Event Listener was removed  =====")
        getCleanerRef(sharedPrefs).child("items").child("detail_item")
            .removeEventListener(valueEventListener)
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Invoice

    fun addInvoiceOrder(
        sharedPrefs: SharedPreferences,
        invoice: InvoiceOrder,
        invoiceBrief: InvoiceOrderBrief,
        callback: AddInvoiceOrderCallback
    ) {

//      transaction firebase serial id key ref) https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions
//      transaction firebase serial id key ref) https://stackoverflow.com/a/28915836/3151712

        getCleanerRef(sharedPrefs).child("invoice_id").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentInvoiceId: MutableData): Transaction.Result {
                Log.d(TAG, "===== doTransaction :currentInvoiceId : '${currentInvoiceId.toString()}' =====")
                Log.d(
                    TAG,
                    "===== doTransaction :currentInvoiceId.value : '${currentInvoiceId.value.toString()}' ====="
                )

                if (currentInvoiceId.value == null) {
                    currentInvoiceId.value = 100000
                } else {

                    // firesbase stored invoice_id in Long

                    val newInvoiceId = (currentInvoiceId.value as Long) + 1
                    Log.d(
                        TAG,
                        "===== doTransaction : newVal : '${newInvoiceId.toInt().toString()}' ====="
                    )

                    currentInvoiceId.value = newInvoiceId

                    // enter id number
                    invoice.id = newInvoiceId
                    invoiceBrief.invoiceOrderId = newInvoiceId


                    val childUpdate: HashMap<String, Any> = hashMapOf(
                        "/invoices/" + newInvoiceId.toString() to invoice,
                        "/invoices_briefs/" + newInvoiceId.toString() to invoiceBrief,
                        "/inventory_invoices/" + newInvoiceId.toString() to invoice,
                        "/customers_invoices/" +invoice.customer!!.id+"/inventory/" + newInvoiceId.toString() to invoice,
                        "/customers_invoices/" +invoice.customer!!.id+"/all/" + newInvoiceId.toString() to invoice,
                        "/invoice_sales/" + newInvoiceId.toString() to invoiceOrderConvertSale(invoice)
                    )

                    getCleanerRef(sharedPrefs).updateChildren(childUpdate).addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback.onAddInvoiceOrderCallback(true, newInvoiceId)
                        } else {
                            callback.onAddInvoiceOrderCallback(false)
                        }
                    }
                }
                return Transaction.success(currentInvoiceId)
            }

            override fun onComplete(
                firebaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (firebaseError != null) {
                    Log.e(TAG, "There is problem adding to invoice_id and save")
                } else {
                    Log.d(TAG, "invoice_id increased by 1 Successfully")
                }
            }
        })

    }

    fun editAndAddInvoiceOrder(
        sharedPrefs:SharedPreferences,
        oldInvoice: InvoiceOrder,
        oldInvoiceBrief :InvoiceOrderBrief,
        newInvoice :InvoiceOrder,
        newInvoiceBrief :InvoiceOrderBrief,
        callback: AddInvoiceOrderCallback)
    {
//      transaction firebase serial id key ref) https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions
//      transaction firebase serial id key ref) https://stackoverflow.com/a/28915836/3151712

        getCleanerRef(sharedPrefs).child("invoice_id").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentInvoiceId: MutableData): Transaction.Result {
                Log.d(TAG, "===== doTransaction :currentInvoiceId : '${currentInvoiceId.toString()}' =====")
                Log.d(
                    TAG,
                    "===== doTransaction :currentInvoiceId.value : '${currentInvoiceId.value.toString()}' ====="
                )

                if (currentInvoiceId.value == null) {
                    currentInvoiceId.value = 100000
                } else {

                    // firesbase stored invoice_id in Long

                    val newInvoiceId = (currentInvoiceId.value as Long) + 1
                    Log.d(
                        TAG,
                        "===== doTransaction : newVal : '${newInvoiceId.toInt().toString()}' ====="
                    )

                    currentInvoiceId.value = newInvoiceId

                    // enter id number
                    newInvoice.id = newInvoiceId
                    newInvoiceBrief.invoiceOrderId = newInvoiceId


                    val childUpdate: HashMap<String, Any> = hashMapOf(
                        "/invoices/" + oldInvoice.id.toString() to oldInvoice,
                        "/invoices_briefs/" + oldInvoiceBrief.invoiceOrderId.toString() to oldInvoiceBrief,
                        "/inventory_invoices/" + oldInvoice.id.toString() to InvoiceOrder(),
                        "/customers_invoices/" +oldInvoice.customer!!.id+"/inventory/" + oldInvoice.id.toString() to InvoiceOrder(),
                        "/customers_invoices/" +oldInvoice.customer!!.id+"/all/" + oldInvoice.id.toString() to oldInvoice,
                        "/invoice_sales/" + oldInvoice.id.toString() to InvoiceOrderSale(),

                        "/invoices/" + newInvoiceId.toString() to newInvoice,
                        "/invoices_briefs/" + newInvoiceId.toString() to newInvoiceBrief,
                        "/inventory_invoices/" + newInvoiceId.toString() to newInvoice,
                        "/customers_invoices/" +newInvoice.customer!!.id+"/inventory/" + newInvoiceId.toString() to newInvoice,
                        "/customers_invoices/" +newInvoice.customer!!.id+"/all/" + newInvoiceId.toString() to newInvoice,
                        "/invoice_sales/" + newInvoiceId.toString() to invoiceOrderConvertSale(newInvoice)
                    )

                    getCleanerRef(sharedPrefs).updateChildren(childUpdate).addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback.onAddInvoiceOrderCallback(true, newInvoiceId)
                        } else {
                            callback.onAddInvoiceOrderCallback(false)
                        }
                    }
                }
                return Transaction.success(currentInvoiceId)
            }

            override fun onComplete(
                firebaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (firebaseError != null) {
                    Log.e(TAG, "There is problem adding to invoice_id and save")
                } else {
                    Log.d(TAG, "invoice_id increased by 1 Successfully")
                }
            }
        })
    }


    fun getInvoiceOrdersForCustomer(
        sharedPrefs: SharedPreferences,
        customer :Customer,
        callback: InvoiceOrdersForCustomerCallback
    ){
        Log.d(TAG,"===== getInvoiceOrdersForCustomer =====")

        getCleanerRef(sharedPrefs).child("customers_invoices").child(customer.id!!).child("inventory")
            .orderByChild("createdAtTimestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    callback.onInvoiceOrdersForCustomerCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    callback.onInvoiceOrdersForCustomerCallback(snapshot,null)
                }
            })
    }

    fun getLastPickupHistoryForCustomer(
        sharedPrefs: SharedPreferences,
        customer :Customer,
        callback: LastPickupHistoryForCustomerCallback
    ){
        getCleanerRef(sharedPrefs).child("customer_pickups_history").child(customer.id!!)
            .orderByChild("pickUpAtTimestamp")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    callback.onLastPickupHistoryForCustomerCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    callback.onLastPickupHistoryForCustomerCallback(snapshot,null)
                }
            })
    }

    fun getInvoiceOrdersById(
        sharedPrefs: SharedPreferences,
        invoiceId :String,
        callback: InvoiceOrdersByIdCallback
    ){
        Log.d(TAG,"===== getInvoiceOrdersById =====")

        getCleanerRef(sharedPrefs).child("inventory_invoices").child(invoiceId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersByIdCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersByIdCallback(snapshot,null)
                }
            })
    }

    fun getInvoiceOrderByIdFromAll(
        sharedPrefs: SharedPreferences,
        invoiceId :String,
        callback: InvoiceOrdersByIdCallback
    ){
        Log.d(TAG,"===== getInvoiceOrderByIdFromAll =====")

        getCleanerRef(sharedPrefs).child("invoices").child(invoiceId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersByIdCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersByIdCallback(snapshot,null)
                }
            })
    }

    fun rackInvoiceOrder(
        sharedPrefs: SharedPreferences,
        invoice: InvoiceOrder,
        invoiceBrief: InvoiceOrderBrief,
        callback: RackInvoiceOrderCallback
    ) {
        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/invoices/" + invoice.id to invoice,
            "/invoices_briefs/" + invoice.id to invoiceBrief,
            "/inventory_invoices/" + invoice.id to invoice,
            "/customers_invoices/" +invoice.customer!!.id+"/inventory/" + invoice.id to invoice,
            "/customers_invoices/" +invoice.customer!!.id+"/all/" + invoice.id to invoice,
            "/invoice_sales/" + invoice.id to invoiceOrderConvertSale(invoice)
        )

        getCleanerRef(sharedPrefs).updateChildren(childUpdate).addOnCompleteListener {
            if (it.isSuccessful) {
                callback.onRackInvoiceOrderCallback(true)
            } else {
                callback.onRackInvoiceOrderCallback(false)
            }
        }
    }

    fun getUserInvoiceHistory(sharedPrefs: SharedPreferences, customer:Customer, startTimestamp:Long, endTimestamp:Long, callback:InvoiceOrdersCallback){
        Log.d(TAG,"===== getUserInvoiceHistory =====")


        Log.d(TAG,"===== customer : '${customer.id.toString()}' =====")
        Log.d(TAG,"===== startTimestamp : '${startTimestamp.toString()}' =====")
        Log.d(TAG,"===== endTimestamp : '${endTimestamp.toString()}' =====")


        getCleanerRef(sharedPrefs).child("customers_invoices").child(customer.id.toString()).child("all")
            .orderByChild("createdAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersCallback(snapshot,null)
                }
            })
    }

    fun removeInvoiceOrder(sharedPrefs : SharedPreferences,customer: Customer,invoiceOrder: InvoiceOrder, callback : RemoveInvoiceOrderCallback){
        Log.d(TAG,"===== removeInvoiceOrder =====")

        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/invoices/" + invoiceOrder.id.toString() to invoiceOrder,
            "/invoices_briefs/" + invoiceOrder.id.toString() to invoiceOrderToBrief(invoiceOrder),
            "/inventory_invoices/" + invoiceOrder.id.toString() to InvoiceOrder(),
            "/customers_invoices/" +invoiceOrder.customer!!.id+"/inventory/" + invoiceOrder.id.toString() to InvoiceOrder(),
            "/customers_invoices/" +invoiceOrder.customer!!.id+"/all/" + invoiceOrder.id.toString() to invoiceOrder,
            "/invoice_sales/" + invoiceOrder.id.toString() to InvoiceOrderSale()
        )

        getCleanerRef(sharedPrefs).updateChildren(childUpdate).addOnCompleteListener {
            if (it.isSuccessful) {
                callback.onRemoveInvoiceOrderCallback(true)
            } else {
                callback.onRemoveInvoiceOrderCallback(false)
            }
        }
    }

    fun getAllInventoryInvoices(sharedPrefs: SharedPreferences, callback:InvoiceOrdersCallback){
        Log.d(TAG,"===== getAllInventoryInvoices =====")
        Log.d(TAG,"===== sharedPrefs : '${sharedPrefs.toString()}' =====")


        getCleanerRef(sharedPrefs).child("inventory_invoices")
            .orderByChild("createdAtTimestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersCallback(snapshot,null)
                }
            })
    }

    fun getInvoiceOrdersByConveyor(sharedPrefs: SharedPreferences, rackLocation:String, callback:InvoiceOrdersCallback){
        Log.d(TAG,"===== getInvoiceOrdersByConveyor =====")
        Log.d(TAG,"===== sharedPrefs : '${sharedPrefs.toString()}' =====")


        getCleanerRef(sharedPrefs).child("invoices")
            .orderByChild("rackLocation")
            .equalTo(rackLocation)
            .limitToFirst(3000)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersCallback(snapshot,null)
                }
            })
    }

    fun getVoidedInvoicesBetween(sharedPrefs: SharedPreferences, startTimestamp:Long, endTimestamp:Long, callback:InvoiceOrdersCallback){
        Log.d(TAG,"===== getVoidedInvoicesBetween =====")

        Log.d(TAG,"===== startTimestamp : '${startTimestamp.toString()}' =====")
        Log.d(TAG,"===== endTimestamp : '${endTimestamp.toString()}' =====")


        getCleanerRef(sharedPrefs).child("invoices")
            .orderByChild("voidAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersCallback(snapshot,null)
                }
            })
    }

    fun getInvoicesBetween(sharedPrefs: SharedPreferences, startTimestamp:Long, endTimestamp:Long, callback:InvoiceOrdersCallback){
        Log.d(TAG,"===== getInvoicesBetween =====")

        Log.d(TAG,"===== startTimestamp : '${startTimestamp.toString()}' =====")
        Log.d(TAG,"===== endTimestamp : '${endTimestamp.toString()}' =====")


        getCleanerRef(sharedPrefs).child("invoices")
            .orderByChild("createdAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrdersCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrdersCallback(snapshot,null)
                }
            })
    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//InvoiceOrderBrief


    fun getInvoiceOrderBriefsBetween(sharedPrefs:SharedPreferences, startTimestamp:Long, endTimestamp:Long, callback: InvoiceOrderBriefGetBetweenCallback){
        Log.d(TAG,"===== getInvoiceOrderBriefsBetween =====")

        getCleanerRef(sharedPrefs).child("invoices_briefs")
            .orderByChild("createdAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onInvoiceOrderBriefsCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onInvoiceOrderBriefsCallback(snapshot,null)
                }
            })
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//pickup


    fun makePickups(
        sharedPrefs: SharedPreferences,
        customer: Customer,
        payments: MutableList<Payment>,
        pickup: Pickup,
        pickupHistory: PickupHistory,
        selectedInvoiceWithPayments : MutableList<InvoiceWithPayment>,
        callback: PickupProcess
    ){
        //      transaction firebase serial id key ref) https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions
        //      transaction firebase serial id key ref) https://stackoverflow.com/a/28915836/3151712

        getCleanerRef(sharedPrefs).child("pickup_id").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentPickupId: MutableData): Transaction.Result {
                Log.d(TAG, "===== doTransaction :currentPickupId : '${currentPickupId.toString()}' =====")
                Log.d(
                    TAG,
                    "===== doTransaction :currentInvoiceId.value : '${currentPickupId.value.toString()}' ====="
                )

                if (currentPickupId.value == null) {
                    currentPickupId.value = 100000
                } else {

                    // firesbase stored pickup_id in Long

                    val newPickupId = (currentPickupId.value as Long) + 1
                    Log.d(TAG, "===== doTransaction : newVal : '${newPickupId.toInt().toString()}' =====")

                    currentPickupId.value = newPickupId

                    // enter id number
                    pickup.id = newPickupId
                    pickupHistory.pickupId = newPickupId


                    var childUpdate = HashMap<String,Any>()
                    childUpdate.putAll(makePaymentChildUpdate(newPickupId,sharedPrefs,payments))
                    childUpdate.putAll(makeInvoiceChildUpdate(customer,selectedInvoiceWithPayments))
                    childUpdate.putAll(makePickupChildUpdate(newPickupId,customer,pickup,pickupHistory))

                    Log.d(TAG,"===== childUpdate : ${childUpdate.toString()} =====")

                    getCleanerRef(sharedPrefs).updateChildren(childUpdate).addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback.onPickupProcess(true)
                        } else {
                            callback.onPickupProcess(false)
                        }
                    }

                }
                return Transaction.success(currentPickupId)
            }

            override fun onComplete(
                firebaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (firebaseError != null) {
                    Log.e(TAG, "There is problem adding to invoice_id and save")
                } else {
                    Log.d(TAG, "pickup_id increased by 1 Successfully")
                }
            }
        })
    }

    fun makePaymentChildUpdate(newPickupId:Long,sharedPrefs: SharedPreferences,payments: MutableList<Payment>):HashMap<String,Any>{
        val temp = hashMapOf<String,Any>()

        for (payment in payments){
            val newKey = getCleanerRef(sharedPrefs).child("payments").push().key
            payment.pickupId = newPickupId
            temp.put("/payments/"+newKey, payment)
        }
        return temp
    }

    // make HashMap for invoices work child update while pick up process
    fun makeInvoiceChildUpdate(
        customer:Customer,
        selectedInvoiceWithPayments: MutableList<InvoiceWithPayment>
    ):HashMap<String,Any>{
        val temp = hashMapOf<String,Any>()

        for(invoiceWithPayment in selectedInvoiceWithPayments){
            when(invoiceWithPayment.typePayment){
                "fullpaid","partialpaid"->{
                    temp.put("/invoices/"+invoiceWithPayment.invoiceOrderId, invoiceWithPayment.invoiceOrder!!)
                    temp.put("/invoices_briefs/"+invoiceWithPayment.invoiceOrderId, invoiceOrderToBrief(invoiceWithPayment.invoiceOrder!!))
                    temp.put("/inventory_invoices/"+invoiceWithPayment.invoiceOrderId, InvoiceOrder())
                    temp.put("/customers_invoices/"+customer.id + "/all/" + invoiceWithPayment.invoiceOrderId, invoiceWithPayment.invoiceOrder!!)
                    temp.put("/customers_invoices/"+customer.id + "/inventory/" + invoiceWithPayment.invoiceOrderId, InvoiceOrder())
                }
                "prepaid"->{
                    temp.put("/invoices/"+invoiceWithPayment.invoiceOrderId, invoiceWithPayment.invoiceOrder!!)
                    temp.put("/invoices_briefs/"+invoiceWithPayment.invoiceOrderId, invoiceOrderToBrief(invoiceWithPayment.invoiceOrder!!))
                    temp.put("/inventory_invoices/"+invoiceWithPayment.invoiceOrderId, invoiceWithPayment.invoiceOrder!!)
                    temp.put("/customers_invoices/"+customer.id + "/all/" + invoiceWithPayment.invoiceOrderId, invoiceWithPayment.invoiceOrder!!)
                    temp.put("/customers_invoices/"+customer.id + "/inventory/" + invoiceWithPayment.invoiceOrderId, invoiceWithPayment.invoiceOrder!!)
                }
            }
        }
        return temp
    }

    // make HashMap for pickup work child update while pick up process
    fun makePickupChildUpdate(
        newPickupId:Long,
        customer:Customer,
        pickup: Pickup,
        pickupHistory: PickupHistory
    ):HashMap<String,Any>{
        return  hashMapOf(
            "/customer_pickups/" + customer.id + "/" + newPickupId.toString() to pickup,
            "/customer_pickups_history/" + customer.id + "/" + newPickupId.toString() to pickupHistory,
            "/pickups/" + newPickupId.toString() to pickup,
            "/pickups_history/" + newPickupId.toString() to pickupHistory
        )
    }

    fun getPickupById(
        sharedPrefs: SharedPreferences,
        pickupId: String,
        callback: PickupByIdCallback
    ){
        Log.d(TAG,"===== getPickupById =====")

        getCleanerRef(sharedPrefs).child("pickups").child(pickupId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onPickupByIdCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onPickupByIdCallback(snapshot,null)
                }
            })
    }

    fun getPickupsBetween(
        sharedPrefs:SharedPreferences,
        startTimestamp:Long,
        endTimestamp:Long,
        callback: PickupGetBetweenCallback)
    {
        Log.d(TAG,"===== getPickupsBetween =====")

        getCleanerRef(sharedPrefs).child("pickups")
            .orderByChild("pickUpAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onPickupsCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onPickupsCallback(snapshot,null)
                }
            })
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Payment

    fun getPaymentsBetween(sharedPrefs:SharedPreferences, startTimestamp:Long, endTimestamp:Long, callback: PaymentGetBetweenCallback){
        Log.d(TAG,"===== getPaymentsBetween =====")

        getCleanerRef(sharedPrefs).child("payments")
            .orderByChild("paymentMaidAtAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onPaymentsCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onPaymentsCallback(snapshot,null)
                }
            })
    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Dropoff

    fun addDropoff(
        sharedPrefs: SharedPreferences,
        dropoff: Dropoff,
        callback: AddDropoffCallback
    ) {
        val childUpdate: HashMap<String, Any> = hashMapOf(
            "/dropoffs/" + dropoff.createdAtTimestamp to dropoff,
            "/customers_dropoffs/"+dropoff.customer!!.id+"/" + dropoff.createdAtTimestamp to dropoff
        )

        getCleanerRef(sharedPrefs).updateChildren(childUpdate).addOnCompleteListener {
            if (it.isSuccessful) {
                callback.onAddDropoffCallback(true)
            } else {
                callback.onAddDropoffCallback(false)
            }
        }
    }

    fun getDropoffsBetween(
        sharedPrefs:SharedPreferences,
        startTimestamp:Long,
        endTimestamp:Long,
        callback: DropoffGetBetweenCallback){

        getCleanerRef(sharedPrefs).child("dropoffs")
            .orderByChild("createdAtTimestamp")
            .startAt(startTimestamp.toDouble())
            .endAt(endTimestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    callback.onDropoffsCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    callback.onDropoffsCallback(snapshot,null)
                }
            })

    }













////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//search
    fun searchLastNamePhoneInvoice(
        sharedPrefs: SharedPreferences,
        category:String,
        searchWord: String,
        searchPhoneCallback:SearchPhoneCallback,
        searchInvoiceCallback:SearchInvoiceCallback,
        searchLastNameCallback:SearchLastNameCallback
    ) {
            when(category){
                "phoneNum" -> {
                    Log.d(TAG,"===== numberStrIntoPhoneNumStr(searchWord) : '${numberStrIntoPhoneNumStr(searchWord)}' =====")
                    getCleanerRef(sharedPrefs).child("customers")
                        .orderByChild("phoneNum")
                        .equalTo(numberStrIntoPhoneNumStr(searchWord))
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {
                                searchPhoneCallback.onSearchPhoneCallback(null,error)
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                searchPhoneCallback.onSearchPhoneCallback(snapshot,null)
                            }
                        })

                }
                "invoiceNum" -> {
                    Log.d(TAG,"===== invoiceNum searching =====")
                    getCleanerRef(sharedPrefs).child("invoices")
                        .child(searchWord)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {
                                Log.d(TAG,"===== invoiceNum searching cancelled=====")
                                searchInvoiceCallback.onSearchInvoiceCallback(null,error)
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.d(TAG,"===== invoiceNum searching succeed=====")
                                searchInvoiceCallback.onSearchInvoiceCallback(snapshot,null)
                            }
                        })
                }
                "lastName" -> {
                    Log.d(TAG,"===== lastName searching =====")
                    Log.d(TAG,"===== searchWord ${searchWord} =====")
                    getCleanerRef(sharedPrefs).child("customers")
                        .orderByChild("lastName")
                        .startAt(searchWord)
                        .endAt(searchWord+"z")
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {
                                searchLastNameCallback.onSearchLastNameCallback(null,error)
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                searchLastNameCallback.onSearchLastNameCallback(snapshot,null)
                            }
                        })
                }
            }


    }

    fun searchByLastName(
        sharedPrefs: SharedPreferences,
        lastName: String,
        searchLastNameCallback:SearchLastNameCallback
    ) {
        Log.d(TAG,"===== lastName searching =====")
        Log.d(TAG,"===== lastName ${lastName} =====")
        getCleanerRef(sharedPrefs).child("customers")
            .orderByChild("lastName")
            .startAt(lastName)
            .endAt(lastName+"z")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    searchLastNameCallback.onSearchLastNameCallback(null,error)
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    searchLastNameCallback.onSearchLastNameCallback(snapshot,null)
                }
            })
    }

    fun searchByFirstName(
        sharedPrefs: SharedPreferences,
        firstName: String,
        searchFirstNameCallback:SearchFirstNameCallback
    ) {
        Log.d(TAG,"===== lastName searching =====")
        Log.d(TAG,"===== firstName ${firstName} =====")
        getCleanerRef(sharedPrefs).child("customers")
            .orderByChild("firstName")
            .startAt(firstName)
            .endAt(firstName+"z")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    searchFirstNameCallback.onSearchFirstNameCallback(null,error)
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    searchFirstNameCallback.onSearchFirstNameCallback(snapshot,null)
                }
            })
    }

    fun searchByInvoiceId(
        sharedPrefs: SharedPreferences,
        invoiceId: String,
        searchInvoiceCallback:SearchInvoiceCallback
    ) {
        Log.d(TAG,"===== invoiceId searching =====")
        Log.d(TAG,"===== invoiceId ${invoiceId} =====")

        getCleanerRef(sharedPrefs).child("invoices").child(invoiceId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG,"===== onCancelled =====")
                    searchInvoiceCallback.onSearchInvoiceCallback(null,error)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"===== onDataChange =====")
                    Log.d(TAG,"===== snapshot : '${snapshot.toString()}' =====")

                    searchInvoiceCallback.onSearchInvoiceCallback(snapshot,null)
                }
            })
    }

    fun getCleanerRef(sharedPrefs: SharedPreferences):DatabaseReference{
        // getting owner email and cleaner name from SharedPreferences
        ownerEmail = sharedPrefs.getString("owner_email","")
        Log.d(TAG,"===== owner_email : '${ownerEmail}' =====")

        // firebase not allow to use . in the child name
        // i change . to ,
        emailForFirebaseChild = makeFirebaseSafeEmail(ownerEmail!!)
        cleanerName = sharedPrefs.getString("cleaner_name","")

        Log.d(TAG,"===== cleanerName : '${cleanerName}' =====")

        val ownerRef = firebaseDatabase.getReference("cleaners_db").child(emailForFirebaseChild!!)
        return ownerRef.child(cleanerName!!)
    }


}