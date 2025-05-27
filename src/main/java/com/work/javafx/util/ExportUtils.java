package com.work.javafx.util;

import com.work.javafx.model.CourseRow;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.geometry.Bounds;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 导出和打印工具类
 */
public class ExportUtils {

    /**
     * 将课表导出为Excel文件(.xlsx) - 学生端
     *
     * 
     * @param tableView     课表TableView
     * @param academicYear  学年
     * @param semester      学期
     * @param scheduleType  课表类型
     * @param parentStage   父窗口Stage
     */
    public static void exportToExcel(TableView<CourseRow> tableView, 
                                    String academicYear, 
                                    String semester, 
                                    String scheduleType,
                                    Stage parentStage) {
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存Excel文件");
        fileChooser.setInitialFileName(academicYear + "-" + semester + "-" + scheduleType + ".xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"));
        
        // 显示保存对话框
        File file = fileChooser.showSaveDialog(parentStage);
        
        if (file != null) {
            try {
                // 创建临时目录来存放Excel XML文件
                Path tempDir = Files.createTempDirectory("excel_export_");
                
                // 创建Excel所需的文件夹结构
                createExcelFolderStructure(tempDir);
                
                // 表格数据
                List<CourseRow> rows = tableView.getItems();
                
                // 创建工作表内容
                createWorksheet(tempDir, academicYear, semester, scheduleType, rows);
                
                // 打包成ZIP文件（Excel本质上是ZIP文件）
                createExcelZipFile(tempDir, file.toPath());
                
                // 删除临时文件夹
                deleteDirectory(tempDir.toFile());
                
                ShowMessage.showInfoMessage("导出成功", "课表已导出到：" + file.getAbsolutePath());
                
            } catch (Exception e) {
                e.printStackTrace();
                ShowMessage.showErrorMessage("导出失败", "导出Excel时发生错误：" + e.getMessage());
            }
        }
    }
    /**
     * 分隔课程名
     *
     */
    private static String splitCourseName(String courseName) {
        // 假设课程名格式为 "课程代码 课程名称"，我们只取课程名称部分
        String[] parts = courseName.split(":", 3);
        if (parts.length >= 2) {
            return parts[2].trim(); // 返回课程名称部分
        }else{
            return courseName;
        }
    }


    /**
     * 将课表导出为Excel文件(.xlsx) - 教师端
     *
     * @param tableView     课表TableView
     * @param academicYear  学年
     * @param semester      学期
     * @param scheduleType  课表类型
     * @param parentStage   父窗口Stage
     */
    public static void exportToExcelForTeacher(TableView<CourseRow> tableView, 
                                    String academicYear, 
                                    String semester, 
                                    String scheduleType,
                                    Stage parentStage) {
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存Excel文件");
        fileChooser.setInitialFileName(academicYear + "-" + semester + "-" + scheduleType + ".xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"));
        
        // 显示保存对话框
        File file = fileChooser.showSaveDialog(parentStage);
        
        if (file != null) {
            try {
                // 创建临时目录来存放Excel XML文件
                Path tempDir = Files.createTempDirectory("excel_export_");
                
                // 创建Excel所需的文件夹结构
                createExcelFolderStructure(tempDir);
                
                // 表格数据 - 转换为学生端的CourseRow格式
                List<CourseRow> teacherRows = tableView.getItems();
                List<CourseRow> convertedRows = new ArrayList<>();

                // 转换教师端的CourseRow到学生端的CourseRow
                for (CourseRow teacherRow : teacherRows) {
                    CourseRow convertedRow = new CourseRow(
                            splitCourseName(teacherRow.getTime()),
                            splitCourseName(teacherRow.getMonday()),
                            splitCourseName(teacherRow.getTuesday()),
                            splitCourseName(teacherRow.getWednesday()),
                            splitCourseName(teacherRow.getThursday()),
                            splitCourseName(teacherRow.getFriday()),
                            (teacherRow.getSaturday()),
                            splitCourseName(teacherRow.getSunday())
                    );
                    convertedRows.add(convertedRow);
                }
                
                // 创建工作表内容
                createWorksheet(tempDir, academicYear, semester, scheduleType, convertedRows);
                
                // 打包成ZIP文件
                createExcelZipFile(tempDir, file.toPath());
                
                // 删除临时文件夹
                deleteDirectory(tempDir.toFile());
                
                ShowMessage.showInfoMessage("导出成功", "课表已导出到：" + file.getAbsolutePath());
                
            } catch (Exception e) {
                e.printStackTrace();
                ShowMessage.showErrorMessage("导出失败", "导出Excel时发生错误：" + e.getMessage());
            }
        }
    }
    
    /**
     * 创建Excel所需的文件夹结构
     */
    private static void createExcelFolderStructure(Path baseDir) throws IOException {
        // 创建主要目录
        Files.createDirectories(baseDir.resolve("_rels"));
        Files.createDirectories(baseDir.resolve("docProps"));
        Files.createDirectories(baseDir.resolve("xl"));
        Files.createDirectories(baseDir.resolve("xl/_rels"));
        Files.createDirectories(baseDir.resolve("xl/worksheets"));
        Files.createDirectories(baseDir.resolve("xl/theme"));
        Files.createDirectories(baseDir.resolve("xl/sharedStrings"));
        
        // 创建必要的XML文件
        
        String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"png\" ContentType=\"image/png\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
                "<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>" +
                "<Override PartName=\"/xl/theme/theme1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.theme+xml\"/>" +
                "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>" +
                "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>" +
                "</Types>";
        Files.write(baseDir.resolve("[Content_Types].xml"), contentTypes.getBytes(), StandardOpenOption.CREATE);
        
        String mainRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>" +
                "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/>" +
                "</Relationships>";
        Files.write(baseDir.resolve("_rels/.rels"), mainRels.getBytes(), StandardOpenOption.CREATE);
        
        String workbook = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets>" +
                "<sheet name=\"课表\" sheetId=\"1\" r:id=\"rId1\"/>" +
                "</sheets>" +
                "</workbook>";
        Files.write(baseDir.resolve("xl/workbook.xml"), workbook.getBytes(), StandardOpenOption.CREATE);
        
        String workbookRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
                "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
                "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" Target=\"theme/theme1.xml\"/>" +
                "<Relationship Id=\"rId4\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/>" +
                "</Relationships>";
        Files.write(baseDir.resolve("xl/_rels/workbook.xml.rels"), workbookRels.getBytes(), StandardOpenOption.CREATE);
        
        String appProps = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\">" +
                "<Application>JavaFX课表系统</Application>" +
                "</Properties>";
        Files.write(baseDir.resolve("docProps/app.xml"), appProps.getBytes(), StandardOpenOption.CREATE);
        
        String coreProps = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" " +
                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" " +
                "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<dc:title>课表</dc:title>" +
                "<dc:creator>课表查询系统</dc:creator>" +
                "<dc:description>学生课表</dc:description>" +
                "</cp:coreProperties>";
        Files.write(baseDir.resolve("docProps/core.xml"), coreProps.getBytes(), StandardOpenOption.CREATE);
        
        String styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
                "<fonts count=\"3\">" +
                "<font><sz val=\"11\"/><name val=\"宋体\"/></font>" +
                "<font><sz val=\"11\"/><name val=\"宋体\"/><b/></font>" +
                "<font><sz val=\"14\"/><name val=\"宋体\"/><b/></font>" +
                "</fonts>" +
                "<fills count=\"3\">" +
                "<fill><patternFill patternType=\"none\"/></fill>" +
                "<fill><patternFill patternType=\"gray125\"/></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFCCE8FF\"/></patternFill></fill>" +
                "</fills>" +
                "<borders count=\"2\">" +
                "<border><left/><right/><top/><bottom/></border>" +
                "<border><left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/></border>" +
                "</borders>" +
                "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
                "<cellXfs count=\"4\">" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
                "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyFont=\"1\" applyFill=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "</cellXfs>" +
                "</styleSheet>";
        Files.write(baseDir.resolve("xl/styles.xml"), styles.getBytes(), StandardOpenOption.CREATE);
        
        String theme = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Office Theme\">" +
                "<a:themeElements>" +
                "<a:clrScheme name=\"Office\">" +
                "<a:dk1><a:sysClr val=\"windowText\" lastClr=\"000000\"/></a:dk1>" +
                "<a:lt1><a:sysClr val=\"window\" lastClr=\"FFFFFF\"/></a:lt1>" +
                "<a:dk2><a:srgbClr val=\"1F497D\"/></a:dk2>" +
                "<a:lt2><a:srgbClr val=\"EEECE1\"/></a:lt2>" +
                "<a:accent1><a:srgbClr val=\"4F81BD\"/></a:accent1>" +
                "<a:accent2><a:srgbClr val=\"C0504D\"/></a:accent2>" +
                "<a:accent3><a:srgbClr val=\"9BBB59\"/></a:accent3>" +
                "<a:accent4><a:srgbClr val=\"8064A2\"/></a:accent4>" +
                "<a:accent5><a:srgbClr val=\"4BACC6\"/></a:accent5>" +
                "<a:accent6><a:srgbClr val=\"F79646\"/></a:accent6>" +
                "<a:hlink><a:srgbClr val=\"0000FF\"/></a:hlink>" +
                "<a:folHlink><a:srgbClr val=\"800080\"/></a:folHlink>" +
                "</a:clrScheme>" +
                "<a:fontScheme name=\"Office\">" +
                "<a:majorFont><a:latin typeface=\"Arial\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/></a:majorFont>" +
                "<a:minorFont><a:latin typeface=\"Arial\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/></a:minorFont>" +
                "</a:fontScheme>" +
                "<a:fmtScheme name=\"Office\">" +
                "<a:fillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:fillStyleLst>" +
                "<a:lnStyleLst><a:ln w=\"9525\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln></a:lnStyleLst>" +
                "<a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>" +
                "<a:bgFillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:bgFillStyleLst>" +
                "</a:fmtScheme>" +
                "</a:themeElements>" +
                "</a:theme>";
        Files.write(baseDir.resolve("xl/theme/theme1.xml"), theme.getBytes(), StandardOpenOption.CREATE);
    }
    
    /**
     * 创建工作表内容
     */
    private static void createWorksheet(Path baseDir, String academicYear, String semester, 
                                       String scheduleType, List<CourseRow> rows) throws IOException {
        // 收集所有字符串并创建SharedStrings表
        StringBuilder sharedStrings = new StringBuilder();
        sharedStrings.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sharedStrings.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"");
        
        // 稍后添加count和uniqueCount属性，先保留占位符
        sharedStrings.append(" count=\"TOTAL_COUNT\" uniqueCount=\"UNIQUE_COUNT\">");
        
        // 添加所有字符串到共享字符串表
        String titleText = academicYear + " " + semester + " " + scheduleType;
        String[] weekdays = {"时间", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        
        // 构建共享字符串索引映射
        Map<String, Integer> stringIndexMap = new HashMap<>();
        
        // 存储所有将被使用的字符串，用于计算总使用次数
        List<String> allUsedStrings = new ArrayList<>();
        
        // 添加标题和表头
        addStringToSharedStringTable(stringIndexMap, sharedStrings, titleText, allUsedStrings);
        
        for (String weekday : weekdays) {
            addStringToSharedStringTable(stringIndexMap, sharedStrings, weekday, allUsedStrings);
        }
        
        // 添加课程数据
        for (CourseRow row : rows) {
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getTime(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getMonday(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getTuesday(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getWednesday(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getThursday(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getFriday(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getSaturday(), allUsedStrings);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, row.getSunday(), allUsedStrings);
        }
        
        sharedStrings.append("</sst>");
        
        // 计算count和uniqueCount
        int totalCount = allUsedStrings.size();
        int uniqueCount = stringIndexMap.size();
        
        // 更新count和uniqueCount
        String completeSharedStrings = sharedStrings.toString()
                .replace("count=\"TOTAL_COUNT\"", "count=\"" + totalCount + "\"")
                .replace("uniqueCount=\"UNIQUE_COUNT\"", "uniqueCount=\"" + uniqueCount + "\"");
        
        Files.write(baseDir.resolve("xl/sharedStrings.xml"), completeSharedStrings.getBytes(), StandardOpenOption.CREATE);
        
        // 创建工作表
        StringBuilder sheetXml = new StringBuilder();
        sheetXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sheetXml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        
        // 列宽设置
        sheetXml.append("<cols>");
        sheetXml.append("<col min=\"1\" max=\"1\" width=\"15\" customWidth=\"1\"/>"); // 时间列
        for (int i = 2; i <= 8; i++) {
            sheetXml.append("<col min=\"").append(i).append("\" max=\"").append(i).append("\" width=\"20\" customWidth=\"1\"/>");
        }
        sheetXml.append("</cols>");
        
        // 表格数据
        sheetXml.append("<sheetData>");
        
        // 标题行
        sheetXml.append("<row r=\"1\" ht=\"30\" customHeight=\"1\">");
        sheetXml.append("<c r=\"A1\" s=\"2\" t=\"s\"><v>").append(stringIndexMap.get(titleText)).append("</v></c>");
        sheetXml.append("</row>");
        
        // 副标题行（星期几）
        sheetXml.append("<row r=\"2\" ht=\"25\" customHeight=\"1\">");
        String[] cols = {"A", "B", "C", "D", "E", "F", "G", "H"};
        for (int i = 0; i < weekdays.length; i++) {
            sheetXml.append("<c r=\"").append(cols[i]).append("2\" s=\"3\" t=\"s\"><v>").append(stringIndexMap.get(weekdays[i])).append("</v></c>");
        }
        sheetXml.append("</row>");
        
        // 课程数据行
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            CourseRow courseRow = rows.get(rowIndex);
            int excelRowIndex = rowIndex + 3; // +3 因为前两行是标题
            
            sheetXml.append("<row r=\"").append(excelRowIndex).append("\" ht=\"60\" customHeight=\"1\">");
            
            // 时间列
            addCellToSheet(sheetXml, "A", excelRowIndex, courseRow.getTime(), stringIndexMap);
            
            // 课程数据
            addCellToSheet(sheetXml, "B", excelRowIndex, courseRow.getMonday(), stringIndexMap);
            addCellToSheet(sheetXml, "C", excelRowIndex, courseRow.getTuesday(), stringIndexMap);
            addCellToSheet(sheetXml, "D", excelRowIndex, courseRow.getWednesday(), stringIndexMap);
            addCellToSheet(sheetXml, "E", excelRowIndex, courseRow.getThursday(), stringIndexMap);
            addCellToSheet(sheetXml, "F", excelRowIndex, courseRow.getFriday(), stringIndexMap);
            addCellToSheet(sheetXml, "G", excelRowIndex, courseRow.getSaturday(), stringIndexMap);
            addCellToSheet(sheetXml, "H", excelRowIndex, courseRow.getSunday(), stringIndexMap);
            
            sheetXml.append("</row>");
        }
        
        sheetXml.append("</sheetData>");
        
        // 合并单元格（标题行）
        sheetXml.append("<mergeCells count=\"1\">");
        sheetXml.append("<mergeCell ref=\"A1:H1\"/>");
        sheetXml.append("</mergeCells>");
        
        // 页面设置
        sheetXml.append("<pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        sheetXml.append("<pageSetup orientation=\"landscape\"/>");
        
        sheetXml.append("</worksheet>");
        
        Files.write(baseDir.resolve("xl/worksheets/sheet1.xml"), sheetXml.toString().getBytes(), StandardOpenOption.CREATE);
    }
    
    /**
     * 向共享字符串表添加字符串，并记录到列表中
     */
    private static void addStringToSharedStringTable(Map<String, Integer> map, 
                                                  StringBuilder sharedStrings, 
                                                  String text, 
                                                  List<String> allUsedStrings) {
        if (text != null && !text.trim().isEmpty()) {
            if (!map.containsKey(text)) {
                // 添加到映射，索引是当前映射大小
                int index = map.size();
                map.put(text, index);
                // 添加到共享字符串表
                sharedStrings.append("<si><t>").append(escapeXml(text)).append("</t></si>");
            }
            // 无论是否为新字符串，都添加到使用计数列表
            allUsedStrings.add(text);
        }
    }
    
    /**
     * 添加单元格到工作表
     */
    private static void addCellToSheet(StringBuilder sheet, String col, int row, String value, Map<String, Integer> stringIndexMap) {
        if (value != null && !value.trim().isEmpty()) {
            // 使用共享字符串表中的索引
            Integer stringIndex = stringIndexMap.get(value);
            if (stringIndex != null) {
                sheet.append("<c r=\"").append(col).append(row).append("\" s=\"1\" t=\"s\"><v>").append(stringIndex).append("</v></c>");
            }
        } else {
            sheet.append("<c r=\"").append(col).append(row).append("\" s=\"1\"/>");
        }
    }
    
    /**
     * 将XML字符转义
     */
    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
    
    /**
     * 创建Excel ZIP文件
     */
    private static void createExcelZipFile(Path sourceDir, Path targetZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZip.toFile()))) {
            // 递归添加所有文件和文件夹
            addToZip(sourceDir.toFile(), sourceDir.toFile(), zos);
        }
    }
    
    /**
     * 递归添加文件到ZIP
     */
    private static void addToZip(File baseDir, File fileToAdd, ZipOutputStream zos) throws IOException {
        // 获取相对路径
        String entryPath = baseDir.toURI().relativize(fileToAdd.toURI()).getPath();
        
        if (fileToAdd.isDirectory()) {
            // 如果是目录，递归添加其内容
            File[] files = fileToAdd.listFiles();
            if (files != null) {
                for (File file : files) {
                    addToZip(baseDir, file, zos);
                }
            }
        } else {
            // 如果是文件，添加到ZIP
            zos.putNextEntry(new ZipEntry(entryPath));
            Files.copy(fileToAdd.toPath(), zos);
            zos.closeEntry();
        }
    }
    
    /**
     * 递归删除目录
     */
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * 将TableView导出为CSV文件
     * 
     * @param <T> TableView的数据类型
     * @param tableView 要导出的表格
     * @param filePath 导出文件路径
     * @return 是否导出成功
     */
    public static <T> boolean exportToCSV(TableView<T> tableView, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // 导出表头
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < tableView.getColumns().size(); i++) {
                if (i > 0) {
                    header.append(",");
                }
                header.append(escapeCSV(tableView.getColumns().get(i).getText()));
            }
            writer.println(header.toString());
            
            // 导出数据行
            for (T item : tableView.getItems()) {
                StringBuilder row = new StringBuilder();
                for (int i = 0; i < tableView.getColumns().size(); i++) {
                    if (i > 0) {
                        row.append(",");
                    }
                    Object cellData = tableView.getColumns().get(i).getCellData(item);
                    row.append(escapeCSV(cellData != null ? cellData.toString() : ""));
                }
                writer.println(row.toString());
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 处理CSV中的特殊字符
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果包含逗号、引号或换行符，需要用引号包围并转义内部引号
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!needQuotes) {
            return value;
        }
        
        // 将文本中的引号替换为两个引号，并用引号包围整个文本
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * 将成绩表导出为Excel文件
     * 
     * @param <T> TableView的数据类型
     * @param tableView 成绩表格
     * @param title 表格标题
     * @param parentStage 父窗口
     * @param file 保存的文件
     * @return 导出是否成功
     */
    public static <T> boolean exportScoresToExcel(TableView<T> tableView, 
                                               String title,
                                               Stage parentStage,
                                               File file) {
        try {
            // 创建临时目录来存放Excel XML文件
            Path tempDir = Files.createTempDirectory("excel_export_");
            
            // 创建Excel所需的文件夹结构
            createExcelFolderStructure(tempDir);
            
            // 创建工作表内容（针对成绩表的通用版本）
            createScoreWorksheet(tempDir, title, tableView);
            
            // 打包成ZIP文件（Excel本质上是ZIP文件）
            createExcelZipFile(tempDir, file.toPath());
            
            // 删除临时文件夹
            deleteDirectory(tempDir.toFile());
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 创建成绩工作表内容
     */
    private static <T> void createScoreWorksheet(Path baseDir, String title, TableView<T> tableView) throws IOException {
        // 收集所有字符串并创建SharedStrings表
        StringBuilder sharedStrings = new StringBuilder();
        sharedStrings.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sharedStrings.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"");
        
        // 稍后添加count和uniqueCount属性，先保留占位符
        sharedStrings.append(" count=\"TOTAL_COUNT\" uniqueCount=\"UNIQUE_COUNT\">");
        
        // 构建共享字符串索引映射
        Map<String, Integer> stringIndexMap = new HashMap<>();
        
        // 存储所有将被使用的字符串，用于计算总使用次数
        List<String> allUsedStrings = new ArrayList<>();
        
        // 添加标题
        addStringToSharedStringTable(stringIndexMap, sharedStrings, title, allUsedStrings);
        
        // 添加表头
        List<String> headers = new ArrayList<>();
        for (javafx.scene.control.TableColumn<T, ?> column : tableView.getColumns()) {
            String headerText = column.getText();
            headers.add(headerText);
            addStringToSharedStringTable(stringIndexMap, sharedStrings, headerText, allUsedStrings);
        }
        
        // 添加表格数据
        List<List<String>> rowsData = new ArrayList<>();
        for (T item : tableView.getItems()) {
            List<String> rowData = new ArrayList<>();
            for (javafx.scene.control.TableColumn<T, ?> column : tableView.getColumns()) {
                Object cellValue = column.getCellData(item);
                String cellText = cellValue != null ? cellValue.toString() : "";
                rowData.add(cellText);
                addStringToSharedStringTable(stringIndexMap, sharedStrings, cellText, allUsedStrings);
            }
            rowsData.add(rowData);
        }
        
        sharedStrings.append("</sst>");
        
        // 计算count和uniqueCount
        int totalCount = allUsedStrings.size();
        int uniqueCount = stringIndexMap.size();
        
        // 更新count和uniqueCount
        String completeSharedStrings = sharedStrings.toString()
                .replace("count=\"TOTAL_COUNT\"", "count=\"" + totalCount + "\"")
                .replace("uniqueCount=\"UNIQUE_COUNT\"", "uniqueCount=\"" + uniqueCount + "\"");
        
        Files.write(baseDir.resolve("xl/sharedStrings.xml"), completeSharedStrings.getBytes(), StandardOpenOption.CREATE);
        
        // 创建工作表
        StringBuilder sheetXml = new StringBuilder();
        sheetXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sheetXml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        
        // 列宽设置
        sheetXml.append("<cols>");
        char lastColumn = (char)('A' + headers.size() - 1);
        for (int i = 0; i < headers.size(); i++) {
            char col = (char)('A' + i);
            sheetXml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                   .append("\" width=\"15\" customWidth=\"1\"/>");
        }
        sheetXml.append("</cols>");
        
        // 表格数据
        sheetXml.append("<sheetData>");
        
        // 标题行
        sheetXml.append("<row r=\"1\" ht=\"30\" customHeight=\"1\">");
        sheetXml.append("<c r=\"A1\" s=\"2\" t=\"s\"><v>").append(stringIndexMap.get(title)).append("</v></c>");
        sheetXml.append("</row>");
        
        // 表头行
        sheetXml.append("<row r=\"2\" ht=\"25\" customHeight=\"1\">");
        for (int i = 0; i < headers.size(); i++) {
            char col = (char)('A' + i);
            sheetXml.append("<c r=\"").append(col).append("2\" s=\"3\" t=\"s\"><v>")
                   .append(stringIndexMap.get(headers.get(i))).append("</v></c>");
        }
        sheetXml.append("</row>");
        
        // 数据行
        for (int rowIndex = 0; rowIndex < rowsData.size(); rowIndex++) {
            List<String> rowData = rowsData.get(rowIndex);
            int excelRowIndex = rowIndex + 3; // +3 因为前两行是标题和表头
            
            sheetXml.append("<row r=\"").append(excelRowIndex).append("\">");
            
            for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                char col = (char)('A' + colIndex);
                String cellValue = rowData.get(colIndex);
                if (!cellValue.isEmpty()) {
                    sheetXml.append("<c r=\"").append(col).append(excelRowIndex).append("\" s=\"1\" t=\"s\"><v>")
                           .append(stringIndexMap.get(cellValue)).append("</v></c>");
                } else {
                    sheetXml.append("<c r=\"").append(col).append(excelRowIndex).append("\" s=\"1\"/>");
                }
            }
            
            sheetXml.append("</row>");
        }
        
        sheetXml.append("</sheetData>");
        
        // 合并单元格（标题行）
        sheetXml.append("<mergeCells count=\"1\">");
        sheetXml.append("<mergeCell ref=\"A1:").append(lastColumn).append("1\"/>");
        sheetXml.append("</mergeCells>");
        
        // 页面设置
        sheetXml.append("<pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        sheetXml.append("<pageSetup orientation=\"portrait\"/>");
        
        sheetXml.append("</worksheet>");
        
        // 修改workbook.xml中的工作表名称
        String workbook = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets>" +
                "<sheet name=\"成绩表\" sheetId=\"1\" r:id=\"rId1\"/>" +
                "</sheets>" +
                "</workbook>";
        Files.write(baseDir.resolve("xl/workbook.xml"), workbook.getBytes(), StandardOpenOption.CREATE);
        
        Files.write(baseDir.resolve("xl/worksheets/sheet1.xml"), sheetXml.toString().getBytes(), StandardOpenOption.CREATE);
    }
}