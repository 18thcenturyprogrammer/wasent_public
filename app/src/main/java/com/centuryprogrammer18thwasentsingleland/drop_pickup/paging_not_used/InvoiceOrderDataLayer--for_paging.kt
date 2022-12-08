//package com.centuryprogrammer18thwasentsingleland.drop_pickup.paging_not_used
//
//import android.content.SharedPreferences
//import android.util.Log
//import androidx.lifecycle.MutableLiveData
//import androidx.paging.DataSource
//import androidx.paging.ItemKeyedDataSource
//import androidx.paging.PageKeyedDataSource
//import com.centuryprogrammer18thwasentsingleland.data.Customer
//import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
//import com.centuryprogrammer18thwasentsingleland.data.SharedPreferenceData
//import com.centuryprogrammer18thwasentsingleland.drop_pickup.TaskBoardFragmentVM
//import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrderBriefsForCustomerCallback
//import com.centuryprogrammer18thwasentsingleland.repository.Repository
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//
//
//
//// android paging datasource factory ref) https://developer.android.com/topic/libraries/architecture/paging
//// android paging datasource factory ref) https://www.zoftino.com/pagination-in-android-using-paging-library
//// android paging datasource factory ref) https://proandroiddev.com/8-steps-to-implement-paging-library-in-android-d02500f7fffe
//
//// datasource factory for bring InvoiceOrderBrief by customer from firebase
//class InvoiceOrderCustomerDataSourceFactory(
//    val sharedPrefs:SharedPreferences,
//    val customer:Customer,
//    val viewModel: TaskBoardFragmentVM
//): DataSource.Factory<Long,InvoiceOrderBrief>() {
//    private val TAG = InvoiceOrderCustomerDataSourceFactory::class.java.simpleName
//
//    override fun create(): DataSource<Long, InvoiceOrderBrief> {
//
//        // sharedPrefs, customer for firebase query
//        // viewModel for returning query value callback
//        return InvoiceOrderCustomerDataSource(sharedPrefs,customer,viewModel)
//    }
//}
//
//
//
//// android paging pagination ItemKeyedDataSource ref) https://www.zoftino.com/pagination-in-android-using-paging-library
//// i will user createdAtTimestamp as key
//class InvoiceOrderCustomerDataSource(
//    val sharedPrefs: SharedPreferences,
//    val customer: Customer,
//    val viewModel: TaskBoardFragmentVM
//): ItemKeyedDataSource<Long, InvoiceOrderBrief>(){
//    private val TAG = InvoiceOrderCustomerDataSource::class.java.simpleName
//
//    override fun loadInitial(
//        params: LoadInitialParams<Long>,
//        callback: LoadInitialCallback<InvoiceOrderBrief>
//    ) {
//        Log.d(TAG,"===== loadInitial =====")
//        Log.d(TAG,"===== params.requestedLoadSize : '${params.requestedLoadSize.toString()}' =====")
//
//        // INVOICE_ORDER_CUSTOMER_DATASOURCE_PAGE_SIZE=2
//        // INVOICE_ORDER_CUSTOMER_DATASOURCE_PRE_DISTANCE=1
//        //  params.requestedLoadSize is 3
//        Repository.getInvoiceOrderBriefsInitialForCustomer(sharedPrefs, customer, params.requestedLoadSize, viewModel,callback)
//
//    }
//
//    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<InvoiceOrderBrief>) {
//        Log.d(TAG,"===== loadBefore =====")
//        Log.d(TAG,"===== params.key : '${params.key.toString()}' =====")
//        Log.d(TAG,"===== params.requestedLoadSize : '${params.requestedLoadSize.toString()}' =====")
//
//        // i am using createdAtTimestamp , so createdAtTimestamp-1 will bring right before data from first item i have now
//        Repository.getInvoiceOrderBriefsBeforeForCustomer(sharedPrefs, customer, params.key-1, params.requestedLoadSize, viewModel, callback)
//    }
//
//    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<InvoiceOrderBrief>) {
//        Log.d(TAG,"===== loadAfter =====")
//        Log.d(TAG,"===== params.key : '${params.key.toString()}' =====")
//        Log.d(TAG,"===== params.requestedLoadSize : '${params.requestedLoadSize.toString()}' =====")
//
//        Repository.getInvoiceOrderBriefsAfterForCustomer(sharedPrefs, customer, params.key+1, params.requestedLoadSize, viewModel, callback)
//    }
//
//
//    override fun getKey(item: InvoiceOrderBrief): Long {
//        return item.createdAtTimestamp!!
//    }
//
//
//
//
//}