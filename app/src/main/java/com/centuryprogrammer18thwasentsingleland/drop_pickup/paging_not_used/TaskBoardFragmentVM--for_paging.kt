//package com.centuryprogrammer18thwasentsingleland.drop_pickup.paging_not_used
//
//import android.content.SharedPreferences
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.ViewModel
//import androidx.paging.ItemKeyedDataSource
//import androidx.paging.LivePagedListBuilder
//import androidx.paging.PagedList
//import com.centuryprogrammer18thwasentsingleland.Constants
//import com.centuryprogrammer18thwasentsingleland.data.Customer
//import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
//import com.centuryprogrammer18thwasentsingleland.data_layer.InvoiceOrderCustomerDataSourceFactory
//import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrderBriefsForCustomerCallback
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//
//
//class TaskBoardFragmentVM : ViewModel() , InvoiceOrderBriefsForCustomerCallback {
//    private val TAG = TaskBoardFragmentVM::class.java.simpleName
//
//    private lateinit var sharedPrefs : SharedPreferences
//
//    private lateinit var customer : Customer
//
//
//// android paging datasource factory ref) https://developer.android.com/topic/libraries/architecture/paging
//// android paging datasource factory ref) https://www.zoftino.com/pagination-in-android-using-paging-library
//// android paging datasource factory ref) https://proandroiddev.com/8-steps-to-implement-paging-library-in-android-d02500f7fffe
//
//    //PagedList for data loading using data source
//    lateinit var invoiceOrderBriefs :LiveData<PagedList<InvoiceOrderBrief>>
//
//    fun setSharedPrefs(receivedShared : SharedPreferences){
//        sharedPrefs = receivedShared
//    }
//
//    fun setCustomer(receivedCustomer : Customer){
//        customer = receivedCustomer
//    }
//
//    fun initDataSource(){
//        Log.d(TAG,"----- initDataSource sharedPrefs : '${sharedPrefs.toString()}' -----")
//        Log.d(TAG,"----- initDataSource customer : '${customer.toString()}' -----")
//
//        val dataSourceFactory = InvoiceOrderCustomerDataSourceFactory(sharedPrefs,customer,this)
//
//        // PagedList config page ref) https://developer.android.com/topic/libraries/architecture/paging/data#define-paging-configs
//        val config = PagedList.Config.Builder()
//            .setPageSize(Constants.INVOICE_ORDER_CUSTOMER_DATASOURCE_PAGE_SIZE)
//            .setPrefetchDistance(Constants.INVOICE_ORDER_CUSTOMER_DATASOURCE_PRE_DISTANCE)
//            .build()
//
//        invoiceOrderBriefs = LivePagedListBuilder<Long,InvoiceOrderBrief>(dataSourceFactory,config).build()
//    }
//
//    // this is called when we got paged result from firebase
//    // this is called when data source call loadInitial , loadBefore, loadAfter
//    // i can figure where this is from check null callbackInitialDataSource, callbackDataSource
//    // if this is called from loadInitial then i will have non-null callbackInitialDataSource
//    // if this is called from loadBefore, loadAfter then i will have non-null callbackDataSource
//    override fun onInvoiceOrderBriefsForCustomerCallback(
//        snapshot: DataSnapshot?,
//        error: DatabaseError?,
//        callbackInitialDataSource:ItemKeyedDataSource.LoadInitialCallback<InvoiceOrderBrief>?,
//        callbackDataSource: ItemKeyedDataSource.LoadCallback<InvoiceOrderBrief>?
//    ) {
//        error?.let {
//            Log.e(TAG, "----- there was problem to bring up InvoiceOrderBriefs by customer : error is '${error.toString()}' -----")
//        }
//
//        snapshot?.let{
////            Log.d(TAG,"----- onInvoiceOrderBriefsForCustomerCallback : snapshot '${it.toString()}' -----")
//            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")
//
//            // android pagination paging firebase ref) https://stackoverflow.com/a/50673779/3151712
//            // android pagination paging firebase ref) https://stackoverflow.com/a/51998355/3151712
//            // we need to get last snapshot and use it get next page items
//
//            val items = mutableListOf<InvoiceOrderBrief>()
//
//            for (item in it.children){
//                Log.d(TAG,"----- Timestamp '${item.getValue(InvoiceOrderBrief::class.java)!!.createdAtTimestamp!!.toString()}' -----")
//                items.add(item.getValue(InvoiceOrderBrief::class.java)!!)
//            }
//
////            Log.d(TAG,"----- items '${items.toString()}' -----")
////            Log.d(TAG,"----- callbackInitialDataSource '${callbackInitialDataSource.toString()}' -----")
////            Log.d(TAG,"----- callbackDataSource '${callbackDataSource.toString()}' -----")
//
//            callbackInitialDataSource?.let {
//                // the result is for data source loadInitial, so use LoadInitialCallback
//                it.onResult(items)
//            }
//
//            callbackDataSource?.let {
//                // the result is for data source loadBefor, loadAfter, so use LoadCallback
//
//                Log.d(TAG,"----- before or after snapshot : '${items.toString()}' -----")
//                it.onResult(items)
//            }
//
////            Log.d(TAG,"----- invoiceOrderBriefs '${invoiceOrderBriefs.value.toString()}' -----")
//        }
//    }
//}