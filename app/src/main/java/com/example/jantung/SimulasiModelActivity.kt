package com.example.jantung

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiModelActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "jantung.tflite"

    private lateinit var resultText: TextView
    private lateinit var age: EditText
    private lateinit var sex: EditText
    private lateinit var cp: EditText
    private lateinit var trestbps: EditText
    private lateinit var chol: EditText
    private lateinit var fbs: EditText
    private lateinit var restecg: EditText
    private lateinit var oldpeak: EditText
    private lateinit var checkButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi_model)

        resultText = findViewById(R.id.txtResult)
        age = findViewById(R.id.age)
        sex = findViewById(R.id.sex)
        cp = findViewById(R.id.cp)
        trestbps = findViewById(R.id.trestbps)
        chol = findViewById(R.id.chol)
        fbs = findViewById(R.id.fbs)
        restecg = findViewById(R.id.restecg)
        oldpeak = findViewById(R.id.oldpeak)
        checkButton = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            var result = doInference(
                age.text.toString(),
                sex.text.toString(),
                cp.text.toString(),
                trestbps.text.toString(),
                chol.text.toString(),
                fbs.text.toString(),
                restecg.text.toString(),
                oldpeak.text.toString())
            runOnUiThread {
                if (result == 0) {
                    resultText.text = "Terkena Penyakit Jantung"
                }else if (result == 1){
                    resultText.text = "Tidak Terkena Penyakit Jantung"
                }
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = org.tensorflow.lite.Interpreter.Options()
        options.setNumThreads(9)
        options.setUseNNAPI(true)
        interpreter = org.tensorflow.lite.Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(input1: String, input2: String, input3: String, input4: String, input5: String, input6: String, input7: String, input8: String): Int{
        val inputVal = FloatArray(8)
        inputVal[0] = input1.toFloat()
        inputVal[1] = input2.toFloat()
        inputVal[2] = input3.toFloat()
        inputVal[3] = input4.toFloat()
        inputVal[4] = input5.toFloat()
        inputVal[5] = input6.toFloat()
        inputVal[6] = input7.toFloat()
        inputVal[7] = input8.toFloat()
        val output = Array(1) { FloatArray(2) }
        interpreter.run(inputVal, output)

        Log.e("result", (output[0].toList()+" ").toString())

        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}