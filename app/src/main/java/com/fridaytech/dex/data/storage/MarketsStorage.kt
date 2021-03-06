package com.fridaytech.dex.data.storage

import com.fridaytech.dex.core.model.Market
import com.fridaytech.dex.data.storage.dao.MarketsDao
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.concurrent.Executors

class MarketsStorage(
    private val marketsDao: MarketsDao
) : IMarketsStorage {
    private val executor = Executors.newSingleThreadExecutor()

    override fun getAllMarkets(): Single<List<Market>> = Maybe.create<List<Market>> { emitter ->
        val rates = marketsDao.getMarkets()

        if (rates.isEmpty()) {
            emitter.onComplete()
        } else {
            emitter.onSuccess(rates)
        }
    }.toSingle()

    override fun getMarket(coinCode: String): Single<Market> = marketsDao.getMarket(coinCode)

    override fun save(vararg markets: Market) = executor.execute { marketsDao.insert(*markets) }

    override fun deleteAll() = executor.execute { marketsDao.deleteAll() }
}
