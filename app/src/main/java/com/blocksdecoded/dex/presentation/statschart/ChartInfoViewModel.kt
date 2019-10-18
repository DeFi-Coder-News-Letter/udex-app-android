package com.blocksdecoded.dex.presentation.statschart

import androidx.lifecycle.MutableLiveData
import com.blocksdecoded.dex.App
import com.blocksdecoded.dex.core.manager.rates.model.StatsData
import com.blocksdecoded.dex.core.model.ChartType
import com.blocksdecoded.dex.core.model.Coin
import com.blocksdecoded.dex.core.model.Market
import com.blocksdecoded.dex.core.model.Rate
import com.blocksdecoded.dex.core.ui.CoreViewModel
import com.blocksdecoded.dex.utils.Logger
import com.blocksdecoded.dex.utils.uiObserve
import com.blocksdecoded.dex.utils.uiSubscribe

class ChartInfoViewModel : CoreViewModel() {
    private val coinManager = App.coinManager
    private val ratesManager = App.ratesManager
    private val ratesStatsManager = App.ratesStatsManager

    private var chartType = ChartType.DAILY
    private var latestRate: Rate? = null
    private var statsData: StatsData? = null

    val coin = MutableLiveData<Coin>()
    val market = MutableLiveData<Market>()
    val chartData = MutableLiveData<ChartViewItem>()
    val currentPeriod = MutableLiveData<Int>()

    fun init(coinCode: String) {
        chartType = ChartType.DAILY
        market.value = ratesManager.getMarket(coinCode)
        coin.value = coinManager.getCoin(coinCode)

        ratesStatsManager.statsFlowable
            .uiSubscribe(disposables, {
                if (it is StatsData) {
                    statsData = it
                    showChart()
                }
            })

        ratesManager.getLatestRate(coinManager.cleanCoinCode(coinCode))
            .uiObserve()
            .subscribe({
                latestRate = it
                showChart()
            }, { Logger.e(it) }
            ).let { disposables.add(it) }

        ratesStatsManager.syncStats(coinManager.cleanCoinCode(coinCode))
        currentPeriod.value = chartType.ordinal
    }

    private fun showChart() {
        if (statsData != null && latestRate != null) {
            chartData.value = RateChartViewFactory().createViewItem(chartType, statsData!!, latestRate)
        }
    }

    fun onPeriodSelect(position: Int) {
        val newType = ChartType.fromInt(position)
        if (chartType != newType) {
            chartType = newType
            showChart()
            currentPeriod.value = newType.ordinal
        }
    }
}