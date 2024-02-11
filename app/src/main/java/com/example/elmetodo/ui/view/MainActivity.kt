package com.example.elmetodo.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import com.example.elmetodo.R
import com.example.elmetodo.domain.model.StatisticCount
import com.example.elmetodo.core.interfaces.StatisticDialog
import com.example.elmetodo.databinding.ActivityMainBinding
import com.example.elmetodo.databinding.DistributeLayoutBinding
import com.example.elmetodo.databinding.StatisticsLayoutBinding
import com.example.elmetodo.core.interfaces.DistributeInterface
import com.example.elmetodo.domain.model.BetSerie
import com.example.elmetodo.ui.viewModel.ViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.RoundingMode

class MainActivity : AppCompatActivity(), StatisticDialog, DistributeInterface {
    private val viewModel: ViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: StatisticsLayoutBinding
    private lateinit var distributeBinding: DistributeLayoutBinding

    private var serie1 = mutableListOf(0.2)
    private var serie2 = mutableListOf(0.2)
    private var serie3 = mutableListOf(0.2)
    private var serie4 = mutableListOf(0.2)
    private var serie5 = mutableListOf(0.2)
    private var statistics = StatisticCount(0, 0, 0, 0.0, 0)
    private lateinit var generalStatistics: StatisticCount
    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initClickListeners()
        initTimer()
        suscribe()
    }

    private fun initClickListeners() {
        binding.apply {
            setClickListener(btnLose1, 1, false)
            setClickListener(btnWin1, 1, true)
            tieButton(btnTie1)

            setClickListener(btnLose2, 2, false)
            setClickListener(btnWin2, 2, true)
            tieButton(btnTie2)

            setClickListener(btnLose3, 3, false)
            setClickListener(btnWin3, 3, true)
            tieButton(btnTie3)

            setClickListener(btnLose4, 4, false)
            setClickListener(btnWin4, 4, true)
            tieButton(btnTie4)

            setClickListener(btnLose5, 5, false)
            setClickListener(btnWin5, 5, true)
            tieButton(btnTie5)
        }
    }

    private fun initTimer() {
        CoroutineScope(Dispatchers.IO).launch {
            Thread.sleep(1000)
            isSaved = false
            while (!isSaved) {
                Thread.sleep(1000)
                statistics.time++
                runOnUiThread {
                    binding.tvTimer.text = toHourMinuteSeconds(statistics.time)
                }
            }
        }

    }

    private fun suscribe() {
        viewModel.generalStatistics.observe(this) {
            generalStatistics = it
        }
        viewModel.serieRefreshed.observe(this) { betSerie ->
            when (betSerie.position) {
                1 -> serie1 = betSerie.serie
                2 -> serie2 = betSerie.serie
                3 -> serie3 = betSerie.serie
                4 -> serie4 = betSerie.serie
                5 -> serie5 = betSerie.serie
            }
        }
    }

    private fun setClickListener(view: View, position: Int, isWinBet: Boolean) {
        view.apply {
            setOnClickListener {
                var serie = selectSerie(position)
                serie = setBalance(serie, position, isWinBet, 1)
                viewModel.refreshSerie(BetSerie(serie, position))
            }
            setOnLongClickListener {
                var serie = selectSerie(position)
                serie = setBalance(serie, position, isWinBet, 2)
                viewModel.refreshSerie(BetSerie(serie, position))
                true
            }
        }
    }

    private fun setBalance(
        serie: MutableList<Double>,
        position: Int,
        isWinBet: Boolean,
        valueOfWin: Int
    ): MutableList<Double> {
        if (isWinBet) {
            statistics.balance += valueOfWin * calculateBet(serie).toDouble()
        } else {
            statistics.balance -= valueOfWin * calculateBet(serie).toDouble()
        }
        val newSerie = setSerie(serie, position, isWinBet)
        return newSerie
    }

    private fun setSerie(
        serie: MutableList<Double>,
        position: Int,
        isWinBet: Boolean
    ): MutableList<Double> {
        var newSerie = serie
        if (isWinBet) {
            if (newSerie.size > 2) {
                newSerie.apply {
                    removeLast()
                    removeFirst()
                }
            } else {
                newSerie = mutableListOf(0.2)
            }
            statistics.victories += 1
        } else {
            serie.add(
                if (serie.size > 1) {
                    round(serie.first() + serie.last())
                } else {
                    round(serie.first())
                }
            )
            statistics.defeats += 1
        }
        showBet(position, newSerie)
        return newSerie
    }

    private fun tieButton(view: View) {
        view.setOnClickListener { statistics.draws += 1 }
    }

    private fun selectSerie(position: Int): MutableList<Double> {
        return when (position) {
            1 -> serie1
            2 -> serie2
            3 -> serie3
            4 -> serie4
            5 -> serie5
            else -> mutableListOf(0.2)
        }
    }

    private fun calculateBet(serie: MutableList<Double>): String {
        return if (serie.size > 1) {
            round(serie.first() + serie.last()).toString()
        } else {
            round(serie.first()).toString()
        }
    }

    private fun showBet(position: Int, serie: MutableList<Double>) {
        when (position) {
            1 -> binding.tv1.text = calculateBet(serie)
            2 -> binding.tv2.text = calculateBet(serie)
            3 -> binding.tv3.text = calculateBet(serie)
            4 -> binding.tv4.text = calculateBet(serie)
            5 -> binding.tv5.text = calculateBet(serie)
        }
    }

    private fun round(number: Double): Double {
        return number.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private fun clearSeries() {
        serie1 = mutableListOf()
        serie2 = mutableListOf()
        serie3 = mutableListOf()
        serie4 = mutableListOf()
        serie5 = mutableListOf()
    }

    fun distributeBets(view: View) {
        distributeBinding = DistributeLayoutBinding.inflate(layoutInflater)
        val dialog =
            distributeSeries(serie1, serie2, serie3, serie4, serie5, this, distributeBinding)
        distributeBinding.apply {
            btnClose.setOnClickListener { dialog.dismiss() }
            btnAccept.setOnClickListener {
                val etSerie = distributeBinding.etSerie.text.toString()
                val finalSerie = etSerie
                    .substring(1, etSerie.length - 1)
                    .split(", ")
                    .map { it.toDouble() }
                    .toMutableList()
                clearSeries()
                for (i in 0 until finalSerie.size) {
                    serie1.add(finalSerie[i])
                    serie2.add(finalSerie[i])
                    serie3.add(finalSerie[i])
                    serie4.add(finalSerie[i])
                    serie5.add(finalSerie[i])
                }
                for (i in 1..5) {
                    showBet(i, finalSerie)
                }
                dialog.dismiss()
            }
        }
    }

    fun showPopUp(view: View) {
        viewModel.getStatistics()
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.statistics_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.statistics -> {
                    dialogBinding = StatisticsLayoutBinding.inflate(layoutInflater)
                    val dialog = createDialog(this, dialogBinding, statistics, generalStatistics, binding.tvTimer.text)
                    CoroutineScope(Dispatchers.IO).launch {
                        while (!isSaved) {
                            Thread.sleep(1000)
                            runOnUiThread {
                                val dialogTime = "Time: ${toHourMinuteSeconds(statistics.time)}"
                                dialogBinding.tvTimeInGame.text = dialogTime
                            }
                        }
                    }
                    dialogBinding.btnSave.setOnClickListener {
                        isSaved = true
                        dialog.dismiss()
                        viewModel.saveStatistics(statistics, generalStatistics)
                        statistics = StatisticCount(0, 0, 0, 0.0, 0)
                        initTimer()
                    }
                    dialogBinding.btnClose.setOnClickListener {
                        dialog.dismiss()
                    }
                    true
                }
                R.id.show_series -> {
                    if (binding.tv1.text == serie1.toString()) {
                        binding.apply {
                            tv1.text = calculateBet(serie1)
                            tv2.text = calculateBet(serie2)
                            tv3.text = calculateBet(serie3)
                            tv4.text = calculateBet(serie4)
                            tv5.text = calculateBet(serie5)
                        }
                    } else {
                        binding.apply {
                            tv1.text = serie1.toString()
                            tv2.text = serie2.toString()
                            tv3.text = serie3.toString()
                            tv4.text = serie4.toString()
                            tv5.text = serie5.toString()
                        }
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArray(
            "statistics",
            arrayOf(
                statistics.victories.toString(),
                statistics.defeats.toString(),
                statistics.draws.toString(),
                statistics.balance.toString(),
                statistics.time.toString()
            )
        )

        for (i in 1..5) {
            outState.putDoubleArray("serie$i", selectSerie(i).toDoubleArray())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val statisticsRecovered = savedInstanceState.getStringArray("statistics")
        statistics = StatisticCount(
            statisticsRecovered?.get(0)?.toInt() ?: 0,
            statisticsRecovered?.get(1)?.toInt() ?: 0,
            statisticsRecovered?.get(2)?.toInt() ?: 0,
            statisticsRecovered?.get(3)?.toDouble() ?: 0.0,
            statisticsRecovered?.get(4)?.toLong() ?: 0
        )
        for (i in 1..5) {
            val serieRecovered =
                savedInstanceState.getDoubleArray("serie$i")?.toMutableList() ?: mutableListOf(0.2)
            when (i) {
                1 -> serie1 = serieRecovered
                2 -> serie2 = serieRecovered
                3 -> serie3 = serieRecovered
                4 -> serie4 = serieRecovered
                5 -> serie5 = serieRecovered
            }
            showBet(i, serieRecovered)
        }
    }
}