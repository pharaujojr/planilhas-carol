package com.example.notacompare.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelCompareService {

    private final Pattern digitsPattern = Pattern.compile("(\\d+)");

    public static class NoteInfo {
        public final String original;
        public final String normalized;
        public final int rowNumber;

        public NoteInfo(String original, String normalized, int rowNumber) {
            this.original = original;
            this.normalized = normalized;
            this.rowNumber = rowNumber;
        }
    }

    public static class CompareResult {
        public final List<NoteInfo> onlyInSefaz;
        public final List<NoteInfo> onlyInSistema;

        public CompareResult(List<NoteInfo> onlyInSefaz, List<NoteInfo> onlyInSistema) {
            this.onlyInSefaz = onlyInSefaz;
            this.onlyInSistema = onlyInSistema;
        }
    }

    public CompareResult compare(InputStream sefazStream, InputStream sistemaStream) throws Exception {
        Map<String, NoteInfo> sefazMap = readSefaz(sefazStream);
        Map<String, NoteInfo> sistemaMap = readSistema(sistemaStream);

        List<NoteInfo> onlyInSefaz = new ArrayList<>();
        List<NoteInfo> onlyInSistema = new ArrayList<>();

        for (Map.Entry<String, NoteInfo> e : sefazMap.entrySet()) {
            if (!sistemaMap.containsKey(e.getKey())) {
                onlyInSefaz.add(e.getValue());
            }
        }

        for (Map.Entry<String, NoteInfo> e : sistemaMap.entrySet()) {
            if (!sefazMap.containsKey(e.getKey())) {
                onlyInSistema.add(e.getValue());
            }
        }

        onlyInSefaz.sort(Comparator.comparing(n -> n.normalized));
        onlyInSistema.sort(Comparator.comparing(n -> n.normalized));

        return new CompareResult(onlyInSefaz, onlyInSistema);
    }

    private Map<String, NoteInfo> readSefaz(InputStream in) throws Exception {
        // Sefaz: table starts at line 7 (header), so data starts at row index 7 (0-based)
        Workbook wb = WorkbookFactory.create(in);
        Sheet sheet = wb.getSheetAt(0);
        int headerRow = 6; // 0-based index for line 7
        int dataStart = headerRow + 1;
        int colIndex = 2; // column C -> 0-based index 2

        Map<String, NoteInfo> map = new HashMap<>();
        for (int r = dataStart; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell cell = row.getCell(colIndex);
            if (cell == null) continue;
            String orig = getCellAsString(cell);
            String norm = normalizeToDigits(orig);
            if (norm == null || norm.isEmpty()) continue;
            map.put(norm, new NoteInfo(orig, norm, r+1));
        }
        wb.close();
        return map;
    }

    private Map<String, NoteInfo> readSistema(InputStream in) throws Exception {
        // Sistema: table starts at line 3 (header), so data starts at row index 3 (0-based)
        Workbook wb = WorkbookFactory.create(in);
        Sheet sheet = wb.getSheetAt(0);
        int headerRow = 2; // line 3
        int dataStart = headerRow + 1;
        int colIndex = 1; // column B -> 0-based index 1

        Map<String, NoteInfo> map = new HashMap<>();
        for (int r = dataStart; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell cell = row.getCell(colIndex);
            if (cell == null) continue;
            String orig = getCellAsString(cell);
            String norm = normalizeToDigits(orig);
            if (norm == null || norm.isEmpty()) continue;
            map.put(norm, new NoteInfo(orig, norm, r+1));
        }
        wb.close();
        return map;
    }

    private String getCellAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: {
                double d = cell.getNumericCellValue();
                long l = (long) d;
                return String.valueOf(l);
            }
            case FORMULA:
                try { return cell.getStringCellValue(); } catch (Exception ex) { return String.valueOf(cell.getNumericCellValue()); }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private String normalizeToDigits(String s) {
        if (s == null) return null;
        s = s.trim();
        // If the input is purely numeric, return without leading zeros removed (but we'll trim leading zeros for comparison)
        Matcher m = digitsPattern.matcher(s);
        String last = null;
        while (m.find()) {
            last = m.group(1);
        }
        if (last == null) return null;
        // remove leading zeros but keep at least one digit
        last = last.replaceFirst("^0+(?!$)", "");
        return last;
    }
}
