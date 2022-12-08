//package com.centuryprogrammer18thwasentsingleland.drop_pickup.paging_not_used
//
//import android.content.SharedPreferences
//import android.util.Log
//import androidx.paging.ItemKeyedDataSource
//import com.centuryprogrammer18thwasentsingleland.data.Customer
//import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
//import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrderBriefsForCustomerCallback
//import com.centuryprogrammer18thwasentsingleland.repository.Repository
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.ValueEventListener
//
//// for bring invoice order for customer for initial page (paging library)
//// callback for firebase returning value
//// callbackDataSource for paging library
//fun getInvoiceOrderBriefsInitialForCustomer(
//    sharedPrefs: SharedPreferences,
//    customer : Customer,
//    loadSize:Int,
//    callback: InvoiceOrderBriefsForCustomerCallback,
//    callbackDataSource: ItemKeyedDataSource.LoadInitialCallback<InvoiceOrderBrief>
//){
//    Log.d(Repository.TAG,"===== getInvoiceOrderBriefsInitialForCustomer =====")
//
//    Repository.getCleanerRef(sharedPrefs).child("customers_invoices").child(customer.id!!)
//        .orderByChild("createdAtTimestamp")
//        .limitToFirst(loadSize)
//        .addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//                callback.onInvoiceOrderBriefsForCustomerCallback(null,error, null,null)
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                callback.onInvoiceOrderBriefsForCustomerCallback(snapshot,null, callbackDataSource, null)
//            }
//        })
//}
//
//// for bring invoice order for customer for before page (paging library)
//// callback for firebase returning value
//// callbackDataSource for paging library
//fun getInvoiceOrderBriefsBeforeForCustomer(
//    sharedPrefs: SharedPreferences,
//    customer : Customer,
//    endKey:Long,
//    loadSize:Int,
//    callback: InvoiceOrderBriefsForCustomerCallback,
//    callbackDataSource: ItemKeyedDataSource.LoadCallback<InvoiceOrderBrief>
//){
//    Log.d(Repository.TAG,"===== getInvoiceOrderBriefsBeforeForCustomer =====")
//    Log.d(Repository.TAG,"===== endKey : '${endKey.toString()}}' =====")
//    Log.d(Repository.TAG,"===== endKey.toDouble : '${endKey.toDouble().toString()}}' =====")
//
//    // endKey is Long type , but firebase not support long. firebase support Double
//    Repository.getCleanerRef(sharedPrefs).child("customers_invoices").child(customer.id!!)
//        .orderByChild("createdAtTimestamp")
//        .endAt(endKey.toDouble())
//        .limitToFirst(loadSize)
//        .addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//                callback.onInvoiceOrderBriefsForCustomerCallback(null,error, null,null)
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                callback.onInvoiceOrderBriefsForCustomerCallback(snapshot,null, null, callbackDataSource)
//            }
//        })
//}
//
//
//// for bring invoice order for customer for after page (paging library)
//// callback for firebase returning value
//// callbackDataSource for paging library
//fun getInvoiceOrderBriefsAfterForCustomer(
//    sharedPrefs: SharedPreferences,
//    customer : Customer,
//    startKey:Long,
//    loadSize:Int,
//    callback: InvoiceOrderBriefsForCustomerCallback,
//    callbackDataSource: ItemKeyedDataSource.LoadCallback<InvoiceOrderBrief>
//){
//    Log.d(Repository.TAG,"===== getInvoiceOrderBriefsAfterForCustomer =====")
//    Log.d(Repository.TAG,"===== startKey : '${startKey.toString()}}' =====")
//    Log.d(Repository.TAG,"===== startKey.toDouble : '${startKey.toDouble().toString()}}' =====")
//
//    // startKey is Long type , but firebase not support long. firebase support Double
//    Repository.getCleanerRef(sharedPrefs).child("customers_invoices").child(customer.id!!)
//        .orderByChild("createdAtTimestamp")
//        .startAt(startKey.toDouble())
//        .limitToFirst(loadSize)
//        .addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//                callback.onInvoiceOrderBriefsForCustomerCallback(null,error, null,null)
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                callback.onInvoiceOrderBriefsForCustomerCallback(snapshot,null, null, callbackDataSource)
//            }
//        })
//}