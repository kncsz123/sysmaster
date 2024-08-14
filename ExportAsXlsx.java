package priv.cgroup.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportAsXlsx {

    /**
     * 导出数据到 Excel 文件并通过 HTTP 响应返回给客户端。
     *
     * @param data     要导出的数据，每个 Map 代表一行，Map 的 key 代表列名。
     * @param response HTTP 响应对象，用于将生成的文件返回给客户端。
     * @param filename 用户下载时看到的默认文件名。
     */
    public Map<String, Object> exportToXlsx(List<Map<String, Object>> data, HttpServletResponse response, String filename) {
        Map<String, Object> result = new HashMap<String, Object>();
        try{
            // 创建一个新的 Excel 工作簿（.xlsx 文件格式）
            Workbook workbook = new XSSFWorkbook();

            // 在工作簿中创建一个新的工作表
            Sheet sheet = workbook.createSheet("Task or Group Data");

            // 创建工作表的第一行作为标题行
            Row headerRow = sheet.createRow(0);

            // 检查数据列表是否非空，并创建标题单元格
            if (!data.isEmpty()) {
                // 获取第一行数据，使用它的 key 来生成标题
                Map<String, Object> headerData = data.getFirst();
                int headerCellIndex = 0;
                // 遍历所有的 key（即列名），并将其作为标题写入 Excel
                for (String key : headerData.keySet()) {
                    Cell cell = headerRow.createCell(headerCellIndex++);
                    cell.setCellValue(key);  // 设置单元格内容为列名
                }
            }

            // 遍历数据列表，将每一行数据写入工作表
            int rowNum = 1;  // 从第二行开始（第一行为标题行）
            for (Map<String, Object> rowData : data) {
                // 创建新的一行
                Row row = sheet.createRow(rowNum++);
                int cellIndex = 0;
                // 遍历行数据的值，将其依次写入单元格
                for (Object value : rowData.values()) {
                    Cell cell = row.createCell(cellIndex++);
                    // 根据值的类型设置单元格的内容
                    if (value instanceof String) {
                        cell.setCellValue((String) value);  // 如果是字符串类型
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);  // 如果是整型
                    } else if (value instanceof Double) {
                        cell.setCellValue((Double) value);  // 如果是浮点型
                    }
                    // 其他类型可以继续扩展处理
                }
            }

            // 设置 HTTP 响应头，这是一个代表excel文件的MIME，指定内容类型和附件下载方式,
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            // 将生成的 Excel 文件内容写入响应的输出流中
            workbook.write(response.getOutputStream());

            // 关闭工作簿，释放资源
            workbook.close();

            result.put("status", 200);
            result.put("message", "success");
        }catch(IOException e){
            result.put("status", 200);
            result.put("message", "success");
        }
        return result;
    }
}
