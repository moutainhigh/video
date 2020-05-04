package com.cofc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.cofc.pojo.video.Channel;

/**
 * Author: Dreamer-1
 * Date: 2019-03-01
 * Time: 10:21
 * Description: 读取Excel内容
 */
public class ExcelReader {

   // private static Logger logger = Logger.getLogger(ExcelReader.class.getName()); // 日志打印类

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     * @param inputStream 读取文件的输入流
     * @param fileType 文件后缀名类型（xls或xlsx）
     * @return 包含文件数据的工作簿对象
     * @throws IOException
     */
    public static Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        Workbook workbook = null;
        if (fileType.equalsIgnoreCase(XLS)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (fileType.equalsIgnoreCase(XLSX)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        return workbook;
    }

    /**
     * 读取Excel文件内容
     * @param fileName 要读取的Excel文件所在路径
     * @return 读取结果列表，读取失败时返回null
     */
    public static List<Channel> readExcel(String fileName) {

        // 获取Excel后缀名
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        // 获取Excel文件
        File excelFile = new File(fileName);
        if (!excelFile.exists()) {
            System.out.println("指定的Excel文件不存在！");
            return null;
        }
    	
        return readExcel( excelFile,fileType);
    }

	public static List<Channel> readExcel(File excelFile,String fileType) {
		Workbook workbook = null;
        FileInputStream inputStream = null;

        try {
            // 获取Excel工作簿
            inputStream = new FileInputStream(excelFile);
            workbook = getWorkbook(inputStream, fileType);

            // 读取excel中的数据
            List<Channel> resultDataList = parseExcel(workbook);
            return resultDataList;
        } catch (Exception e) {
            System.out.println("解析Excel失败，文件名：" + excelFile.getName() + " 错误信息：" + e.getMessage());
            return null;
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                System.out.println("关闭数据流出错！错误信息：" + e.getMessage());
                return null;
            }
        }
	}

	 /**
     * 读取Excel文件内容
     * @param file 上传的Excel文件
     * @return 读取结果列表，读取失败时返回null
     */
    public static List<Channel> readExcel(MultipartFile file) {

        Workbook workbook = null;

        try {
            // 获取Excel后缀名
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty() || fileName.lastIndexOf(".") < 0) {
                System.out.println("解析Excel失败，因为获取到的Excel文件名非法！");
                return null;
            }
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

            // 获取Excel工作簿
            workbook = getWorkbook(file.getInputStream(), fileType);

            // 读取excel中的数据
            List<Channel> resultDataList = parseExcel(workbook);

            return resultDataList;
        } catch (Exception e) {
            System.out.println("解析Excel失败，文件名：" + file.getOriginalFilename() + " 错误信息：" + e.getMessage());
            return null;
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
            } catch (Exception e) {
                System.out.println("关闭数据流出错！错误信息：" + e.getMessage());
                return null;
            }
        }
    }
    /**
     * 解析Excel数据
     * @param workbook Excel工作簿对象
     * @return 解析结果
     */
    private static List<Channel> parseExcel(Workbook workbook) {
       System.out.println("parseExcel()");
       List<Channel> resultDataList = new ArrayList<>();
        // 解析sheet
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = workbook.getSheetAt(sheetNum);

            // 校验sheet是否合法
            if (sheet == null) {
                continue;
            }

            // 获取第一行数据
            int firstRowNum = ((org.apache.poi.ss.usermodel.Sheet) sheet).getFirstRowNum();
            Row firstRow = ((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(firstRowNum);
            if (null == firstRow) {
                System.out.println("解析Excel失败，在第一行没有读取到任何数据！");
            }

            // 解析每一行的数据，构造数据对象
            int rowStart = firstRowNum + 1;
            int rowEnd = ((org.apache.poi.ss.usermodel.Sheet) sheet).getPhysicalNumberOfRows();
            for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
                Row row = sheet.getRow(rowNum);

                if (null == row) {
                    continue;
                }

                Channel resultData = convertRowToData(row);
                if (null == resultData) {
                    System.out.println("第 " + row.getRowNum() + "行数据不合法，已忽略！");
                    continue;
                }
                resultDataList.add(resultData);
            }
        }

        return resultDataList;
    }

    /**
     * 将单元格内容转换为字符串
     * @param cell
     * @return
     */
    private static String convertCellValueToString(Cell cell) {
        if(cell==null){
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC:   //数字
                Double doubleValue = cell.getNumericCellValue();

                // 格式化科学计数法，取一位整数
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING:    //字符串
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN:   //布尔
                Boolean booleanValue = cell.getBooleanCellValue();
                returnValue = booleanValue.toString();
                break;
            case BLANK:     // 空值
                break;
            case FORMULA:   // 公式
                returnValue = cell.getCellFormula();
                break;
            case ERROR:     // 故障
                break;
            default:
                break;
        }
        return returnValue;
    }

    /**
     * 提取每一行中需要的数据，构造成为一个结果数据对象
     *
     * 当该行中有单元格的数据为空或不合法时，忽略该行的数据
     *
     * @param row 行数据
     * @return 解析后的行数据对象，行数据错误时返回null
     */
    private static Channel convertRowToData(Row row) {
    	Channel channel = new Channel();

        Cell cell;
        int cellNum = 0;
        // 获取频道号
        cell = row.getCell(0);
        String idStr = convertCellValueToString(cell);
        if (null == idStr || "".equals(idStr)) {
        	return channel;
        }else {
             Integer id =Integer.parseInt(idStr);
             channel.setId(id);
        }
        // 获取频道名
        cell = row.getCell(1);
        String name = convertCellValueToString(cell);
        channel.setName(name);
        // 获取频道播放串
        cell = row.getCell(2);
        String playUrl = convertCellValueToString(cell);
        channel.setPlayUrl(playUrl);
        // 获取频道海报
        cell = row.getCell(3);
        String posterUrl = convertCellValueToString(cell);
        channel.setPosterUrl(posterUrl);

        return channel;
    }
    
    public static void main(String[] args) {
        // 创建需要写入的数据列表
        // 设定Excel文件所在路径
        String excelFileName = "/Users/viviyiyi/Downloads/频道信息.xlsx";
        // 读取Excel文件内容
        List<Channel> list = ExcelReader.readExcel(excelFileName);
        if(null==list||list.isEmpty()) {
        	System.out.println("没有内容");  
        }else {
        for(Channel channel:list) {
        	System.out.println(channel.detail());  
          }
        }

    }
}
