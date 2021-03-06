package com.fridaytech.dex.presentation.transactions

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.fridaytech.dex.R
import com.fridaytech.dex.core.ui.CoreFragment
import com.fridaytech.dex.presentation.transactions.info.TransactionInfoDialog
import com.fridaytech.dex.presentation.transactions.recycler.TransactionViewHolder
import com.fridaytech.dex.presentation.transactions.recycler.TransactionsAdapter
import com.fridaytech.dex.presentation.widgets.MainToolbar
import com.fridaytech.dex.utils.ui.ToastHelper
import com.fridaytech.dex.utils.visible
import kotlinx.android.synthetic.main.fragment_transactions.*

class TransactionsFragment : CoreFragment(R.layout.fragment_transactions),
        TransactionViewHolder.OnClickListener, TransactionsAdapter.ILoadNextListener {
    private lateinit var viewModel: TransactionsViewModel
    private lateinit var adapter: TransactionsAdapter
    private var coinCode: String? = null

    //region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter =
            TransactionsAdapter(this, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)
        viewModel.init(coinCode)

        viewModel.errorEvent.observe(this, Observer { ToastHelper.showErrorMessage(it) })
        viewModel.messageEvent.observe(this, Observer { ToastHelper.showInfoMessage(it) })
        viewModel.finishEvent.observe(this, Observer { activity?.finish() })

        viewModel.transactions.observe(this, Observer {
            adapter.setTransactions(it)
        })

        viewModel.showTransactionInfoEvent.observe(this, Observer {
            TransactionInfoDialog.show(childFragmentManager, it)
        })

        viewModel.syncTransactions.observe(this, Observer {
            adapter.syncTransactions(it)
        })

        viewModel.coinName.observe(this, Observer {
            if (it != null) toolbar?.title = "Transactions"
        })

        viewModel.balance.observe(this, Observer {
            if (it != null) transactions_total_balance.update(it)
        })

        viewModel.isEmpty.observe(this, Observer { emptyTransactions ->
            transactions_recycler?.visible = !emptyTransactions
            empty_view?.visible = emptyTransactions
        })

        viewModel.isSyncing.observe(this, Observer {
            transactions_total_balance?.progressVisible = it

            empty_view?.title = getString(if (it) {
                R.string.message_syncing
            } else {
                R.string.message_empty_transactions
            })
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactions_recycler?.adapter = adapter
        transactions_recycler?.layoutManager = LinearLayoutManager(context)

        toolbar?.bind(MainToolbar.getBackAction { viewModel.onBackClick() })
    }

    //endregion

    override fun loadNext() {
        viewModel.loadNext()
    }

    override fun onClick(position: Int) {
        viewModel.onTransactionClick(position)
    }

    companion object {
        fun newInstance(coinCode: String) = TransactionsFragment().apply {
            this.coinCode = coinCode
        }
    }
}
