package com.fridaytech.dex.presentation.orders.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fridaytech.dex.R
import com.fridaytech.dex.presentation.common.TransactionSentDialog
import com.fridaytech.dex.presentation.dialogs.BaseBottomDialog
import com.fridaytech.dex.presentation.orders.CancelOrderConfirmDialog
import com.fridaytech.dex.presentation.orders.model.OrderInfoConfig
import com.fridaytech.dex.utils.ui.ToastHelper
import kotlinx.android.synthetic.main.dialog_order_info.*

class OrderInfoDialog : BaseBottomDialog(R.layout.dialog_order_info) {

    private lateinit var viewModel: OrderInfoViewModel
    private var orderInfo: OrderInfoConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(OrderInfoViewModel::class.java)
        viewModel.init(orderInfo)

        viewModel.dismissEvent.observe(this, Observer {
            dismiss()
        })

        viewModel.errorEvent.observe(this, Observer {
            ToastHelper.showErrorMessage(it)
        })

        viewModel.transactionSentEvent.observe(this, Observer { hash ->
            fragmentManager?.let {
                TransactionSentDialog.show(it, hash)
            }
        })

        viewModel.messageEvent.observe(this, Observer {
            ToastHelper.showInfoMessage(it)
        })

        viewModel.orderInfo.observe(this, Observer { order ->
            order_info_amount.setCoin(order.makerCoin.code, order.makerAmount)
            order_info_price.setCoin(order.takerCoin.code, order.price)
            order_info_receive_amount.setCoin(order.takerCoin.code, order.takerAmount)
            order_info_filled_amount.setCoin(order.takerCoin.code, order.filledAmount)
            order_info_expire_date.setRaw(order.expireDate)
        })

        viewModel.showCancelConfirmEvent.observe(this, Observer { cancelInfo ->
            fragmentManager?.let { CancelOrderConfirmDialog.show(it, cancelInfo) }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        order_cancel.setOnClickListener { viewModel.onCancelClick() }
    }

    companion object {
        fun show(fragmentManager: FragmentManager, orderInfo: OrderInfoConfig) {
            val fragment = OrderInfoDialog()

            fragment.orderInfo = orderInfo

            fragment.show(fragmentManager, "order_info")
        }
    }
}
