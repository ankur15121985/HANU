package com.hanu.pdfconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PdfConverter - Core engine for converting PDF to Excel, Word, and PowerPoint.
 *
 * Strategy:
 *  - Uses Android's built-in PdfRenderer to render each page as a Bitmap
 *  - Extracts text using com.tom-roush.pdfbox-android for Excel/Word text extraction
 *  - For Excel: organizes text lines as rows/cells
 *  - For Word: inserts text paragraphs + optional page images
 *  - For PPT: each PDF page becomes a slide with the rendered image
 */
public class PdfConverter {

    public interface ProgressCallback {
        void onProgress(int progress, String status);
    }

    private final Context context;
    private ProgressCallback progressCallback;

    public PdfConverter(Context context) {
        this.context = context;
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    private void reportProgress(int progress, String status) {
        if (progressCallback != null) {
            progressCallback.onProgress(progress, status);
        }
    }

    // ─────────────────────────────────────────────
    // PDF TO EXCEL
    // ─────────────────────────────────────────────
    public void pdfToExcel(Uri pdfUri, File outputFile) throws Exception {
        reportProgress(5, "Opening PDF…");

        List<String> allLines = extractTextLines(pdfUri);

        reportProgress(40, "Building Excel spreadsheet…");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Converted PDF");

        // Styles
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)34, (byte)139, (byte)87}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont whiteFont = workbook.createFont();
        whiteFont.setColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
        whiteFont.setBold(true);

        XSSFCellStyle contentStyle = workbook.createCellStyle();
        contentStyle.setWrapText(true);

        XSSFCellStyle altRowStyle = workbook.createCellStyle();
        altRowStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)242, (byte)247, (byte)244}, null));
        altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        altRowStyle.setWrapText(true);

        int rowNum = 0;
        int totalLines = allLines.size();

        for (int i = 0; i < totalLines; i++) {
            String line = allLines.get(i).trim();
            if (line.isEmpty()) continue;

            Row row = sheet.createRow(rowNum++);
            String[] cells = line.split("\t|  {2,}"); // tab or multiple spaces = column

            for (int c = 0; c < cells.length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(cells[c].trim());
                if (rowNum == 1) {
                    cell.setCellStyle(headerStyle);
                } else if (rowNum % 2 == 0) {
                    cell.setCellStyle(altRowStyle);
                } else {
                    cell.setCellStyle(contentStyle);
                }
            }

            int progress = 40 + (int) ((i / (float) totalLines) * 45);
            reportProgress(progress, "Writing row " + rowNum + " of ~" + totalLines + "…");
        }

        // Auto-size columns
        if (sheet.getRow(0) != null) {
            for (int c = 0; c < sheet.getRow(0).getLastCellNum(); c++) {
                sheet.autoSizeColumn(c);
            }
        }

        reportProgress(90, "Saving Excel file…");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            workbook.write(fos);
        }
        workbook.close();
        reportProgress(100, "Done!");
    }

    // ─────────────────────────────────────────────
    // PDF TO WORD
    // ─────────────────────────────────────────────
    public void pdfToWord(Uri pdfUri, File outputFile) throws Exception {
        reportProgress(5, "Opening PDF…");

        int pageCount = getPdfPageCount(pdfUri);
        reportProgress(15, "Found " + pageCount + " pages…");

        XWPFDocument document = new XWPFDocument();

        // Document title
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("Converted by Hanu PDF Converter");
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        titleRun.setColor("1a6b3a");
        titleRun.addBreak();

        List<String> allLines = extractTextLines(pdfUri);

        reportProgress(50, "Writing document content…");

        // Page separator tracking
        boolean firstPage = true;
        int lineIndex = 0;

        for (String line : allLines) {
            if (line.startsWith("--- Page ")) {
                if (!firstPage) {
                    // Add page break
                    XWPFParagraph breakPara = document.createParagraph();
                    XWPFRun breakRun = breakPara.createRun();
                    breakRun.addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);
                }
                // Page heading
                XWPFParagraph pagePara = document.createParagraph();
                pagePara.setStyle("Heading2");
                XWPFRun pageRun = pagePara.createRun();
                pageRun.setText(line.replace("---", "").trim());
                pageRun.setColor("888888");
                pageRun.setFontSize(9);
                firstPage = false;
            } else if (line.trim().isEmpty()) {
                document.createParagraph(); // blank line
            } else {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                run.setText(line);
                run.setFontFamily("Calibri");
                run.setFontSize(11);

                // Detect likely headings (short + no period at end)
                if (line.length() < 60 && !line.endsWith(".") && !line.endsWith(",")) {
                    run.setBold(true);
                    run.setFontSize(12);
                }
            }
            lineIndex++;
            int progress = 50 + (int) ((lineIndex / (float) allLines.size()) * 40);
            reportProgress(Math.min(progress, 90), "Writing content…");
        }

        reportProgress(93, "Saving Word document…");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            document.write(fos);
        }
        document.close();
        reportProgress(100, "Done!");
    }

    // ─────────────────────────────────────────────
    // PDF TO POWERPOINT
    // ─────────────────────────────────────────────
    public void pdfToPowerPoint(Uri pdfUri, File outputFile) throws Exception {
        reportProgress(5, "Opening PDF…");

        XMLSlideShow pptx = new XMLSlideShow();

        // Set slide dimensions (standard 16:9 widescreen)
        java.awt.Dimension pgSize = new java.awt.Dimension(
            (int)(10 * 72), // 10 inches width
            (int)(7.5 * 72)  // 7.5 inches height
        );
        pptx.setPageSize(pgSize);

        try (ParcelFileDescriptor pfd = context.getContentResolver()
                .openFileDescriptor(pdfUri, "r")) {

            PdfRenderer renderer = new PdfRenderer(pfd);
            int pageCount = renderer.getPageCount();

            reportProgress(10, "Rendering " + pageCount + " PDF pages…");

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                // Render at high quality
                int width = (int)(page.getWidth() * 2);
                int height = (int)(page.getHeight() * 2);
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(android.graphics.Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                // Convert bitmap to PNG bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos);
                byte[] imageBytes = bos.toByteArray();
                bitmap.recycle();

                // Create slide
                XSLFSlide slide = pptx.createSlide();
                slide.getBackground().setFillColor(java.awt.Color.WHITE);

                // Add image to fill entire slide
                XSLFPictureData pictureData = pptx.addPicture(imageBytes,
                    PictureData.PictureType.PNG);
                XSLFPictureShape shape = slide.createPicture(pictureData);
                shape.setAnchor(new java.awt.Rectangle(0, 0, pgSize.width, pgSize.height));

                // Add slide number label
                XSLFTextBox textBox = slide.createTextBox();
                textBox.setAnchor(new java.awt.Rectangle(pgSize.width - 80, pgSize.height - 30, 70, 24));
                XSLFTextParagraph tp = textBox.addNewTextParagraph();
                tp.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.RIGHT);
                XSLFTextRun tr = tp.addNewTextRun();
                tr.setText((i + 1) + " / " + pageCount);
                tr.setFontSize(9.0);
                tr.setFontColor(java.awt.Color.GRAY);

                renderer.close();
                renderer = new PdfRenderer(pfd); // Re-open for next page (workaround)

                // Re-open only if more pages
                int progress = 10 + (int) (((i + 1) / (float) pageCount) * 82);
                reportProgress(progress, "Converting page " + (i + 1) + " of " + pageCount + "…");

                // Break to avoid re-opening after last page
                if (i == pageCount - 1) break;
            }
            renderer.close();
        }

        reportProgress(94, "Saving PowerPoint file…");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            pptx.write(fos);
        }
        pptx.close();
        reportProgress(100, "Done!");
    }

    // ─────────────────────────────────────────────
    // HELPER: Extract text lines with page markers
    // ─────────────────────────────────────────────
    private List<String> extractTextLines(Uri pdfUri) throws Exception {
        List<String> lines = new ArrayList<>();

        try {
            // Use PdfBox for text extraction
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context);
            InputStream is = context.getContentResolver().openInputStream(pdfUri);
            com.tom_roush.pdfbox.pdmodel.PDDocument pdDoc =
                com.tom_roush.pdfbox.pdmodel.PDDocument.load(is);

            com.tom_roush.pdfbox.text.PDFTextStripper stripper =
                new com.tom_roush.pdfbox.text.PDFTextStripper();

            int totalPages = pdDoc.getNumberOfPages();
            for (int p = 1; p <= totalPages; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String pageText = stripper.getText(pdDoc);

                lines.add("--- Page " + p + " ---");
                String[] pageLines = pageText.split("\n");
                for (String l : pageLines) {
                    lines.add(l);
                }

                int progress = 5 + (int) ((p / (float) totalPages) * 30);
                reportProgress(progress, "Extracting text from page " + p + "/" + totalPages + "…");
            }
            pdDoc.close();
            if (is != null) is.close();

        } catch (Exception e) {
            // Fallback: return a message if text extraction fails
            lines.add("--- Page 1 ---");
            lines.add("This PDF may be image-based or encrypted.");
            lines.add("Text extraction was not possible.");
            lines.add("For scanned PDFs, please use an OCR tool first.");
        }

        return lines;
    }

    // ─────────────────────────────────────────────
    // HELPER: Get PDF page count
    // ─────────────────────────────────────────────
    private int getPdfPageCount(Uri pdfUri) {
        try (ParcelFileDescriptor pfd = context.getContentResolver()
                .openFileDescriptor(pdfUri, "r")) {
            PdfRenderer renderer = new PdfRenderer(pfd);
            int count = renderer.getPageCount();
            renderer.close();
            return count;
        } catch (Exception e) {
            return 1;
        }
    }
}
