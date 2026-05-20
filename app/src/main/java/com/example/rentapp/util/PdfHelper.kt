package com.example.rentapp.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.rentapp.data.local.dao.MonthlyEarning
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object PdfHelper {

    fun generateAnnualReport(
        context: Context,
        year: Int,
        totalCollected: Double,
        paidCount: Int,
        pendingCount: Int,
        delayedCount: Int,
        monthlyEarnings: List<MonthlyEarning>,
        currency: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        val nf = NumberFormat.getCurrencyInstance().apply {
            try {
                this.currency = java.util.Currency.getInstance(currency)
            } catch (e: Exception) {}
        }

        // --- Header Section with Gradient Effect ---
        paint.color = Color.rgb(10, 25, 49) // Very Dark Blue
        canvas.drawRect(0f, 0f, 595f, 150f, paint)
        
        paint.color = Color.rgb(0, 184, 212) // Accent Cyan
        canvas.drawRect(0f, 145f, 595f, 150f, paint)

        // Logo text or Icon
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 36f
        canvas.drawText("RentApp", 50f, 70f, paint)
        
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(200, 200, 200)
        canvas.drawText("SOLUCIONES INMOBILIARIAS DIGITALES", 50f, 95f, paint)

        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Reporte Anual $year", 320f, 80f, paint)

        // --- Summary Section ---
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        val df = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("es", "ES"))
        canvas.drawText("Emitido el: ${df.format(java.util.Date())}", 50f, 175f, paint)

        // Cards
        drawSummaryCard(canvas, 50f, 195f, 150f, 80f, "Pagados", paidCount.toString(), Color.rgb(232, 245, 233), Color.rgb(46, 125, 50))
        drawSummaryCard(canvas, 222f, 195f, 150f, 80f, "Pendientes", pendingCount.toString(), Color.rgb(255, 243, 224), Color.rgb(239, 108, 0))
        drawSummaryCard(canvas, 395f, 195f, 150f, 80f, "Atrasados", delayedCount.toString(), Color.rgb(255, 235, 238), Color.rgb(198, 40, 40))

        // Total Collected
        paint.color = Color.rgb(248, 249, 250)
        canvas.drawRoundRect(50f, 290f, 545f, 360f, 10f, 10f, paint)
        
        paint.color = Color.rgb(10, 25, 49)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL RECAUDADO EN EL PERIODO", 70f, 330f, paint)
        
        paint.textSize = 24f
        paint.color = Color.rgb(0, 105, 92)
        val totalStr = nf.format(totalCollected)
        val textWidth = paint.measureText(totalStr)
        canvas.drawText(totalStr, 545f - textWidth - 25f, 332f, paint)

        // --- Mini Chart Section ---
        drawMiniChart(canvas, 50f, 380f, 495f, 100f, monthlyEarnings, Color.rgb(10, 25, 49))

        // --- Table Section ---
        var yPos = 530f
        paint.color = Color.rgb(10, 25, 49)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText("Desglose de Ingresos Mensuales", 50f, yPos, paint)
        
        yPos += 20f
        // Table Header
        paint.color = Color.rgb(10, 25, 49)
        canvas.drawRoundRect(50f, yPos, 545f, yPos + 30f, 5f, 5f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MES", 75f, yPos + 20f, paint)
        canvas.drawText("MONTO RECAUDADO", 250f, yPos + 20f, paint)
        canvas.drawText("RENDIMIENTO", 450f, yPos + 20f, paint)

        // Table Content
        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        yPos += 50f
        val maxEarning = monthlyEarnings.maxOfOrNull { it.total } ?: 1.0
        
        for (i in 1..12) {
            val earning = monthlyEarnings.find { it.month == i }?.total ?: 0.0
            
            if (i % 2 == 0) {
                paint.color = Color.rgb(245, 245, 245)
                canvas.drawRect(50f, yPos - 18f, 545f, yPos + 10f, paint)
            }
            
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(months[i - 1], 75f, yPos, paint)
            canvas.drawText(nf.format(earning), 250f, yPos, paint)
            
            // Percentage indicator
            val percent = (earning / maxEarning * 100).toInt()
            paint.color = if(earning > 0) Color.rgb(0, 150, 136) else Color.GRAY
            canvas.drawText("$percent%", 450f, yPos, paint)
            
            yPos += 22f
        }

        // --- Footer ---
        paint.color = Color.LTGRAY
        canvas.drawLine(50f, 800f, 545f, 800f, paint)
        
        paint.textSize = 9f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Este documento es un reporte oficial generado por el sistema RentApp.", 50f, 815f, paint)
        canvas.drawText("Página 1 de 1", 510f, 815f, paint)

        pdfDocument.finishPage(page)

        val fileName = savePdfToDownloads(context, pdfDocument, "Reporte_Anual_${year}")
        pdfDocument.close()
    }

    private fun drawSummaryCard(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, label: String, value: String, bgColor: Int, textColor: Int) {
        val paint = Paint()
        // Background
        paint.color = bgColor
        canvas.drawRoundRect(x, y, x + w, y + h, 12f, 12f, paint)
        
        // Label
        paint.color = Color.DKGRAY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label.uppercase(), x + 15f, y + 30f, paint)
        
        // Value
        paint.color = textColor
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 15f, y + 65f, paint)
    }

    private fun drawMiniChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, data: List<MonthlyEarning>, color: Int) {
        val paint = Paint()
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawRect(x, y, x + w, y + h, paint)
        
        val maxVal = data.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: 1.0
        val barWidth = (w / 12f) * 0.7f
        val spacing = (w / 12f) * 0.3f
        
        paint.color = color
        for (i in 1..12) {
            val valAmt = data.find { it.month == i }?.total ?: 0.0
            val barHeight = (valAmt / maxVal * h).toFloat()
            val barX = x + (i - 1) * (barWidth + spacing) + spacing/2
            
            canvas.drawRect(barX, y + h - barHeight, barX + barWidth, y + h, paint)
            
            // Month Initials
            val paintText = Paint().apply {
                textSize = 8f
                textAlign = Paint.Align.CENTER
                this.color = Color.GRAY
            }
            val months = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
            canvas.drawText(months[i-1], barX + barWidth/2, y + h + 12f, paintText)
        }
    }


    fun generateContractReceipt(
        context: Context,
        contract: com.example.rentapp.data.local.entity.Contract,
        property: com.example.rentapp.data.local.entity.Property?,
        tenant: com.example.rentapp.data.local.entity.Tenant?,
        currency: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        val nf = NumberFormat.getCurrencyInstance().apply {
            try {
                this.currency = java.util.Currency.getInstance(currency)
            } catch (e: Exception) {}
        }
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // --- Header Section ---
        paint.color = Color.rgb(0, 74, 173)
        canvas.drawRect(0f, 0f, 595f, 120f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 28f
        canvas.drawText("RentApp", 50f, 55f, paint)
        
        paint.textSize = 20f
        canvas.drawText("Certificado de Contrato Digital", 50f, 85f, paint)

        // --- Document Info ---
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText("ID DE CONTRATO: ${contract.id}", 380f, 150f, paint)
        canvas.drawText("FECHA DE EMISIÓN: ${df.format(java.util.Date())}", 380f, 165f, paint)

        // --- Sections ---
        var yPos = 190f
        
        // 1. Parties Information
        drawSectionHeader(canvas, 50f, yPos, "INFORMACIÓN DE LAS PARTES")
        yPos += 40f
        
        drawInfoRow(canvas, 70f, yPos, "Propiedad:", property?.name ?: "N/A", paint)
        yPos += 20f
        drawInfoRow(canvas, 70f, yPos, "Dirección:", property?.address ?: "N/A", paint)
        yPos += 25f
        drawInfoRow(canvas, 70f, yPos, "Inquilino:", "${tenant?.firstName ?: ""} ${tenant?.lastName ?: ""}", paint)
        yPos += 20f
        drawInfoRow(canvas, 70f, yPos, "Identificación:", tenant?.documentId ?: "N/A", paint)
        yPos += 20f
        drawInfoRow(canvas, 70f, yPos, "Email:", tenant?.email ?: "N/A", paint)

        // 2. Financial Conditions
        yPos += 45f
        drawSectionHeader(canvas, 50f, yPos, "CONDICIONES ECONÓMICAS")
        yPos += 40f
        
        // Highlighted box for rent
        paint.color = Color.rgb(240, 247, 255)
        canvas.drawRect(50f, yPos - 15f, 545f, yPos + 60f, paint)
        
        paint.color = Color.rgb(0, 74, 173)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ALQUILER MENSUAL:", 70f, yPos + 10f, paint)
        paint.textSize = 18f
        canvas.drawText(nf.format(contract.monthlyRent), 250f, yPos + 10f, paint)
        
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.BLACK
        canvas.drawText("Depósito en garantía:", 70f, yPos + 35f, paint)
        canvas.drawText(nf.format(contract.deposit), 250f, yPos + 35f, paint)
        
        canvas.drawText("Día de pago:", 70f, yPos + 55f, paint)
        canvas.drawText("Día ${contract.paymentDueDay} de cada mes", 250f, yPos + 55f, paint)

        // 3. Validity and Clauses
        yPos += 100f
        drawSectionHeader(canvas, 50f, yPos, "VIGENCIA Y CLÁUSULAS")
        yPos += 40f
        
        drawInfoRow(canvas, 70f, yPos, "Fecha de Inicio:", df.format(contract.startDate), paint)
        yPos += 20f
        val endDateStr = if (contract.endDate == 0L) "Indefinido" else df.format(contract.endDate)
        drawInfoRow(canvas, 70f, yPos, "Fecha de Fin:", endDateStr, paint)
        yPos += 20f
        drawInfoRow(canvas, 70f, yPos, "Penalidad Mora:", nf.format(contract.lateFeePenalty), paint)
        yPos += 20f
        drawInfoRow(canvas, 70f, yPos, "Cláusula Desalojo:", if (contract.hasEvictionClause) "SÍ" else "NO", paint)

        // --- Signature Area ---
        yPos = 700f
        paint.color = Color.BLACK
        canvas.drawLine(70f, yPos, 250f, yPos, paint)
        canvas.drawLine(345f, yPos, 525f, yPos, paint)
        
        paint.textSize = 10f
        canvas.drawText("Firma del Arrendador", 100f, yPos + 20f, paint)
        canvas.drawText("Firma del Inquilino", 385f, yPos + 20f, paint)

        // --- Footer ---
        paint.textSize = 9f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Este documento tiene validez como resumen informativo de contrato.", 50f, 800f, paint)
        canvas.drawText("RentApp - Gestión Digital", 50f, 815f, paint)

        pdfDocument.finishPage(page)

        val fileName = savePdfToDownloads(context, pdfDocument, "Contrato_${contract.id}")
        pdfDocument.close()
    }

    fun generatePaymentReceipt(
        context: Context,
        payment: com.example.rentapp.data.local.entity.Payment,
        contract: com.example.rentapp.data.local.entity.Contract?,
        property: com.example.rentapp.data.local.entity.Property?,
        tenant: com.example.rentapp.data.local.entity.Tenant?,
        landlordName: String,
        currency: String
    ): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create() // Smaller size for receipt
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        val nf = NumberFormat.getCurrencyInstance().apply {
            try {
                this.currency = java.util.Currency.getInstance(currency)
            } catch (e: Exception) {}
        }
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val monthNames = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")

        // --- Header Section ---
        paint.color = Color.rgb(0, 74, 173)
        canvas.drawRect(0f, 0f, 400f, 100f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("RentApp", 30f, 45f, paint)
        
        paint.textSize = 16f
        canvas.drawText("Recibo de Alquiler", 30f, 75f, paint)

        // --- Receipt Info ---
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText("RECIBO №: ${if(payment.receiptNumber.isNotBlank()) payment.receiptNumber else payment.id}", 30f, 130f, paint)
        canvas.drawText("FECHA: ${df.format(payment.paidDate ?: payment.updatedAt)}", 250f, 130f, paint)

        // --- Body ---
        var yPos = 160f
        
        // Landlord & Tenant
        drawInfoRowReceipt(canvas, 30f, yPos, "Arrendador:", landlordName, paint)
        yPos += 20f
        drawInfoRowReceipt(canvas, 30f, yPos, "Arrendatario:", "${tenant?.firstName ?: ""} ${tenant?.lastName ?: ""}", paint)
        yPos += 20f
        drawInfoRowReceipt(canvas, 30f, yPos, "Propiedad:", property?.name ?: "N/A", paint)
        
        yPos += 40f
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRect(30f, yPos - 20f, 370f, yPos + 60f, paint)
        
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("CONCEPTO:", 50f, yPos + 5f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Alquiler de ${monthNames.getOrElse(payment.month - 1) { "?" }} ${payment.year}", 50f, yPos + 25f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MONTO:", 50f, yPos + 50f, paint)
        paint.textSize = 16f
        paint.color = Color.rgb(0, 74, 173)
        canvas.drawText(nf.format(payment.amount), 120f, yPos + 52f, paint)

        // Payment Method
        yPos += 100f
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawInfoRowReceipt(canvas, 30f, yPos, "Método de Pago:", payment.paymentMethod.ifBlank { "N/A" }, paint)
        
        if (payment.notes.isNotBlank()) {
            yPos += 20f
            canvas.drawText("Notas: ${payment.notes}", 30f, yPos, paint)
        }

        // --- Signature ---
        yPos = 520f
        canvas.drawLine(100f, yPos, 300f, yPos, paint)
        paint.textSize = 9f
        canvas.drawText("Firma del Arrendador", 150f, yPos + 15f, paint)

        // --- Footer ---
        yPos = 570f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Este es un comprobante digital generado por RentApp.", 30f, yPos, paint)

        pdfDocument.finishPage(page)

        val fileName = savePdfToDownloads(context, pdfDocument, "Recibo_${payment.year}_${payment.month}")
        pdfDocument.close()
        return fileName
    }

    private fun drawInfoRowReceipt(canvas: Canvas, x: Float, y: Float, label: String, value: String, paint: Paint) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, x, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(value, x + 90f, y, paint)
    }

    private fun drawSectionHeader(canvas: Canvas, x: Float, y: Float, title: String) {
        val paint = Paint()
        paint.color = Color.rgb(0, 74, 173)
        canvas.drawRect(x, y, x + 5f, y + 25f, paint) // Accent bar
        
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(0, 74, 173)
        canvas.drawText(title, x + 15f, y + 18f, paint)
        
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(x, y + 30f, x + 495f, y + 30f, paint)
    }

    private fun drawInfoRow(canvas: Canvas, x: Float, y: Float, label: String, value: String, paint: Paint) {
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.GRAY
        canvas.drawText(label, x, y, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText(value, x + 130f, y, paint)
    }


    private fun savePdfToDownloads(context: Context, pdfDocument: PdfDocument, baseFileName: String): String {
        val fileName = "${baseFileName}_${System.currentTimeMillis()}.pdf"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                outputStream = FileOutputStream(file)
            }

            outputStream?.let {
                pdfDocument.writeTo(it)
                it.flush()
                it.close()
                Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return fileName
    }
}
