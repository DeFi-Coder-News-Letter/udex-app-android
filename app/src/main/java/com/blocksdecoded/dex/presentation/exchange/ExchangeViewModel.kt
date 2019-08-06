package com.blocksdecoded.dex.presentation.exchange

import androidx.lifecycle.MutableLiveData
import com.blocksdecoded.dex.App
import com.blocksdecoded.dex.core.manager.CoinManager
import com.blocksdecoded.dex.core.model.Coin
import com.blocksdecoded.dex.core.ui.CoreViewModel
import com.blocksdecoded.dex.presentation.exchange.ExchangeState.*
import com.blocksdecoded.dex.presentation.exchange.view.ExchangePairItem
import com.blocksdecoded.dex.presentation.exchange.view.ExchangeViewState
import java.math.BigDecimal

class ExchangeViewModel : CoreViewModel() {

    private val relayer = App.relayerAdapterManager.getMainAdapter()
    private val adapterManager = App.adapterManager
    private val exchangeWrapper = App.zrxKitManager.zrxKit().getExchangeInstance()
    
    private var exchangeableCoins: List<Coin> = listOf()
    private var coinPairsCodes: List<Pair<String, String>> = listOf()
    
    private var exchangeState = BID
    
    private var mSendCoins: List<ExchangePairItem> = listOf()
        set(value) {
            field = value
            sendCoins.value = value
        }
    
    private var mReceiveCoins: List<ExchangePairItem> = listOf()
        set(value) {
            field = value
            receiveCoins.value = value
        }

    val viewState = MutableLiveData<ExchangeViewState>()

    val sendCoins = MutableLiveData<List<ExchangePairItem>>()
    val receiveCoins = MutableLiveData<List<ExchangePairItem>>()
    
    init {
        relayer.availablePairsSubject
            .subscribe {
                coinPairsCodes = it
                
                exchangeableCoins = CoinManager.coins.filter { coin ->
                    coinPairsCodes.firstOrNull { pair ->
                        pair.first.equals(coin.code, true) ||
                            pair.second.equals(coin.code, true)
                    } != null
                }
                
                refreshPairs(null)
                
                initState(mSendCoins.first(), mReceiveCoins.first())
            }.let { disposables.add(it) }
    }
    
    //region Private
    
    private fun initState(sendItem: ExchangePairItem, receiveItem: ExchangePairItem) {
        viewState.value = ExchangeViewState(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            sendItem,
            receiveItem
        )
    }
    
    private fun getAvailableSendCoins(): List<ExchangePairItem> {
        // Send only available pair exchangeableCoins
        return exchangeableCoins
            .filter { coin -> coinPairsCodes
                .firstOrNull {
                    when(this.exchangeState) {
                        BID -> it.first == coin.code
                        
                        ASK -> it.second == coin.code
                    }
                } != null
            }
            .map { ExchangePairItem(it.code, it.title, 0.toBigDecimal(), 0.toBigDecimal()) }
    }
    
    private fun getAvailableReceiveCoins(baseCoinCode: String): List<ExchangePairItem> {
        // Receive available send coin pairs
        return exchangeableCoins
            .filter { coin ->
                coinPairsCodes.firstOrNull {
                    when(this.exchangeState) {
                        BID -> it.first.equals(baseCoinCode, true) &&
                            it.second.equals(coin.code, true)
                        
                        ASK -> it.second.equals(baseCoinCode, true) &&
                            it.first.equals(coin.code, true)
                    }
                } != null
            }
            .map { ExchangePairItem(it.code, it.title, 0.toBigDecimal(), 0.toBigDecimal()) }
    }
    
    private fun refreshPairs(state: ExchangeViewState?, refreshSendCoins: Boolean = true) {
        if (refreshSendCoins) {
            mSendCoins = getAvailableSendCoins()
        }
    
        val sendCoin = state?.sendPair?.code ?: mSendCoins.first().code
        mReceiveCoins = getAvailableReceiveCoins(sendCoin)
    }
    
    //endregion

    //region Public
    
    fun onReceiveCoinPick(position: Int) {
        viewState.value?.receivePair = mReceiveCoins[position]
    }
    
    fun onSendCoinPick(position: Int) {
        viewState.value?.sendPair = mSendCoins[position]
        refreshPairs(viewState.value, false)
    }

    fun onSendAmountChange(amount: BigDecimal) {
        viewState.value?.sendAmount = amount
        viewState.value = viewState.value
    }

    fun onReceiveAmountChange(amount: BigDecimal) {
    
    }

    fun onMaxClick() {
        val adapter = adapterManager.adapters.firstOrNull { it.coin.code == viewState.value?.sendPair?.code }
        if (adapter != null) {
        
        }
    }

    fun onExchangeClick() {

    }

    fun onSwitchClick() {
        exchangeState = when(exchangeState) {
            BID -> ASK
            ASK -> BID
        }
        
        val newState = ExchangeViewState(
            sendAmount = viewState.value?.receiveAmount ?: BigDecimal.ZERO,
            receiveAmount = viewState.value?.sendAmount ?: BigDecimal.ZERO,
            sendPair = viewState.value?.receivePair!!,
            receivePair = viewState.value?.sendPair!!
        )
        
        refreshPairs(newState)
        
        viewState.value = newState
    }
    
    //endregion

}
