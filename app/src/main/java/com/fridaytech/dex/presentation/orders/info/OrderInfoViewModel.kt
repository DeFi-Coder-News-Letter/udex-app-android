package com.fridaytech.dex.presentation.orders.info

import androidx.lifecycle.MutableLiveData
import com.fridaytech.dex.App
import com.fridaytech.dex.R
import com.fridaytech.dex.core.ui.CoreViewModel
import com.fridaytech.dex.core.ui.SingleLiveEvent
import com.fridaytech.dex.data.manager.duration.ETransactionType
import com.fridaytech.dex.data.zrx.IRelayerAdapter
import com.fridaytech.dex.data.zrx.model.SimpleOrder
import com.fridaytech.dex.presentation.orders.model.CancelOrderInfo
import com.fridaytech.dex.presentation.orders.model.OrderInfoConfig
import com.fridaytech.dex.utils.rx.uiSubscribe
import java.math.BigDecimal

class OrderInfoViewModel : CoreViewModel() {
    private val adapterManager = App.adapterManager
    private val ratesConverter = App.ratesConverter
    private val processingTimeProvider = App.processingDurationProvider
    private val relayerAdapter: IRelayerAdapter?
        get() = App.relayerAdapterManager.mainRelayer
    private var order: OrderInfoConfig? = null

    val orderInfo = MutableLiveData<SimpleOrder>()

    val dismissEvent = SingleLiveEvent<Unit>()
    val transactionSentEvent = SingleLiveEvent<String>()
    val showCancelConfirmEvent =
        SingleLiveEvent<CancelOrderInfo>()

    fun init(orderInfo: OrderInfoConfig?) {
        this.order = orderInfo

        order?.let {
            this.orderInfo.value = SimpleOrder.fromOrder(
                ratesConverter,
                it.orderRecord,
                it.side,
                orderInfo = it.info,
                isMine = true
            )
        } ?: dismissEvent.call()
    }

    private fun onCancelConfirm() {
        order?.let {
            messageEvent.postValue(R.string.message_cancel_started)
            relayerAdapter?.cancelOrder(it.orderRecord.order)
                ?.uiSubscribe(disposables, {
                    transactionSentEvent.postValue(it)
                    dismissEvent.call()
                }, {
                    errorEvent.postValue(R.string.error_cancel_order)
                })
        }
    }

    fun onCancelClick() {
        orderInfo.value?.let { uiOrder ->
            val adapter = adapterManager.adapters
                .firstOrNull { it.coin.code == uiOrder.makerCoin.code } ?: return

            showCancelConfirmEvent.value =
                CancelOrderInfo(
                    1,
                    BigDecimal.ZERO,
                    adapter.feeCoinCode,
                    processingTimeProvider.getEstimatedDuration(
                        adapter.coin,
                        ETransactionType.CANCEL
                    )
                ) { onCancelConfirm() }
        } ?: dismissEvent.call()
    }
}
