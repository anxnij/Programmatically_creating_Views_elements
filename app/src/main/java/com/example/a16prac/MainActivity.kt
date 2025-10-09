package com.example.a16prac

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private val expr = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#FFF5F8"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))  // <- без named args
        }
        scroll.addView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val allTextViews = mutableListOf<TextView>()
        val allButtons = mutableListOf<Button>()

        display = TextView(this).apply {
            text = "0"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = Gravity.END
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(bg = Color.WHITE)
            layoutParams = linParams(margins = dp(8))
        }
        root.addView(display)
        allTextViews += display

        val hello = TextView(this).apply {
            text = "Hello Programmed-View!"
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = linParams(margins = dp(8))
        }
        root.addView(hello)
        allTextViews += hello

        val tvContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = linParams(margins = dp(8))
        }
        for (i in 0..9) {
            val tv = TextView(this).apply {
                text = i.toString()
                textSize = 18f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                layoutParams = linParams(width = 0, weight = 1f, margins = dp(4))
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = rounded(bg = Color.parseColor("#EEEEEE"))
            }
            tvContainer.addView(tv)
            allTextViews += tv
        }
        root.addView(tvContainer)

        val btnContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = linParams(margins = dp(8))
        }
        repeat(10) { idx ->
            val b = Button(this).apply {
                text = "Btn $idx"
                layoutParams = linParams(width = 0, weight = 1f, margins = dp(4))
                setOnClickListener {
                    Toast.makeText(this@MainActivity, "Clicked: $text", Toast.LENGTH_SHORT).show()
                }
            }
            btnContainer.addView(b)
            allButtons += b
        }
        root.addView(btnContainer)

        val gridManualTitle = titleLabel("Калькулятор (вручную)")
        root.addView(gridManualTitle); allTextViews += gridManualTitle

        val gridManual = GridLayout(this).apply {
            rowCount = 4
            columnCount = 4
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = false
            layoutParams = linParams(
                width = ViewGroup.LayoutParams.MATCH_PARENT,
                margins = dp(8)
            )
        }

        fun addManual(label: String) {
            val btn = Button(this).apply {
                text = label
                layoutParams = gridParams(margins = dp(6))   // width=0 + вес колонки
                setOnClickListener(onKey(label))
            }
            gridManual.addView(btn)
            allButtons += btn
        }

        listOf(
            "7","8","9","/",
            "4","5","6","*",
            "1","2","3","-",
            "0",".","=","+"
        ).forEach { addManual(it) }
        root.addView(gridManual)

        val gridLoopTitle = titleLabel("Калькулятор (циклом)")
        root.addView(gridLoopTitle); allTextViews += gridLoopTitle

        val gridLoop = GridLayout(this).apply {
            rowCount = 5
            columnCount = 4
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = false
            layoutParams = linParams(
                width = ViewGroup.LayoutParams.MATCH_PARENT,
                margins = dp(8)
            )
        }

        val keys = listOf(
            "C","(",")","←",
            "7","8","9","/",
            "4","5","6","*",
            "1","2","3","-",
            "0",".","=","+"
        )

        keys.forEach { label ->
            val b = Button(this).apply {
                text = label
                layoutParams = gridParams(margins = dp(6))
                setOnClickListener(onKey(label))
            }
            gridLoop.addView(b)
            allButtons += b
        }
        root.addView(gridLoop)

        allTextViews.forEach { tv ->
            if (tv.background == null) tv.background = rounded(bg = Color.parseColor("#F7F7F7"))
            tv.setPadding(dp(10), dp(10), dp(10), dp(10))

        }
        allButtons.forEach { btn ->
            btn.textSize = 16f
            btn.isAllCaps = false
            btn.setTextColor(Color.BLACK)
            btn.background = rounded(
                bg = Color.WHITE,
                stroke = Color.parseColor("#DDDDDD"),
                radius = dp(12).toFloat()
            )
            btn.setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        setContentView(scroll)
    }

    private fun onKey(label: String) = View.OnClickListener {
        when (label) {
            "C" -> { expr.clear(); display.text = "0" }
            "←" -> {
                if (expr.isNotEmpty()) expr.deleteCharAt(expr.length - 1)
                display.text = if (expr.isEmpty()) "0" else expr.toString()
            }
            "=" -> {
                val input = expr.toString()
                runCatching { eval(input) }
                    .onSuccess { v ->
                        val out = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
                        display.text = out
                        expr.clear(); expr.append(out)
                    }
                    .onFailure { display.text = "Err" }
            }
            else -> { expr.append(label); display.text = expr.toString() }
        }
    }

    private fun eval(s: String): Double {
        val ops = ArrayDeque<Char>()
        val vals = ArrayDeque<Double>()

        fun prec(op: Char) = when (op) { '+','-' -> 1; '*','/' -> 2; else -> -1 }
        fun applyOp() {
            val b = vals.removeLast()
            val a = vals.removeLast()
            when (ops.removeLast()) {
                '+' -> vals.addLast(a + b)
                '-' -> vals.addLast(a - b)
                '*' -> vals.addLast(a * b)
                '/' -> vals.addLast(a / b)
            }
        }

        var i = 0
        while (i < s.length) {
            when (val c = s[i]) {
                ' ' -> i++
                '(' -> { ops.addLast('('); i++ }
                ')' -> { while (ops.isNotEmpty() && ops.last() != '(') applyOp(); ops.removeLast(); i++ }
                '+','-','*','/' -> {
                    if (c == '-' && (i == 0 || s[i-1] in "+-*/(")) { // унарный минус
                        var j = i + 1
                        while (j < s.length && (s[j].isDigit() || s[j]=='.')) j++
                        vals.addLast(s.substring(i, j).toDouble())
                        i = j
                    } else {
                        while (ops.isNotEmpty() && prec(ops.last()) >= prec(c)) applyOp()
                        ops.addLast(c); i++
                    }
                }
                else -> {
                    if (c.isDigit() || c == '.') {
                        var j = i
                        while (j < s.length && (s[j].isDigit() || s[j]=='.')) j++
                        vals.addLast(s.substring(i, j).toDouble())
                        i = j
                    } else throw IllegalArgumentException("Bad char: $c")
                }
            }
        }
        while (ops.isNotEmpty()) applyOp()
        return vals.single()
    }

    private fun dp(px: Int): Int = (px * resources.displayMetrics.density).toInt()

    private fun linParams(
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        weight: Float = 0f,
        margins: Int = 0
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(width, height, weight).apply {
            setMargins(margins, margins, margins, margins)
        }

    private fun gridParams(margins: Int = 0): GridLayout.LayoutParams =
        GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        ).apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setMargins(margins, margins, margins, margins)
        }

    private fun rounded(
        bg: Int = Color.WHITE,
        stroke: Int = Color.TRANSPARENT,
        radius: Float = dp(10).toFloat()
    ): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(bg)
            setStroke(dp(1), stroke)
        }

    private fun titleLabel(text: String) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(Color.BLACK)
        layoutParams = linParams(margins = dp(8))
    }
}
