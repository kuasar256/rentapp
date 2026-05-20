package com.example.rentapp.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.rentapp.data.local.dao.MonthlyEarning
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object CsvHelper {

    fun exportAnnualDataToCsv(
        context: Context,
        year: Int,
        monthlyEarnings: List<MonthlyEarning>,
        totalCollected: Double
    ) {
        val fileName = "Reporte_RentApp_$year.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        val sb = StringBuilder()
        sb.append("Mes,Recaudado\n")
        
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        
        for (i in 1..12) {
            val earning = monthlyEarnings.find { it.month == i }?.total ?: 0.0
            sb.append("${months[i-1]},$earning\n")
        }
        
        sb.append("\nTOTAL ANUAL,$totalCollected\n")

        try {
            FileOutputStream(file).use { out ->
                out.write(sb.toString().toByteArray())
            }
            Toast.makeText(context, "CSV Guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
