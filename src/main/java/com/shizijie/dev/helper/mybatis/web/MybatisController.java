package com.shizijie.dev.helper.mybatis.web;

import com.shizijie.dev.helper.common.BaseController;
import com.shizijie.dev.helper.common.ResponseBean;
import com.shizijie.dev.helper.mybatis.web.vo.GetTableColumnsVO;
import com.shizijie.dev.helper.utils.DataSourcesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author shizijie
 * @version 2019-11-11 下午3:16
 */
@RestController
@Slf4j
public class MybatisController extends BaseController {
    @PostMapping("/getTableColumns")
    public ResponseBean getsql(@Validated @RequestBody GetTableColumnsVO vo){
        Connection connection= DataSourcesUtils.getConnection(vo.getUrl(),vo.getUsername(),vo.getPwd(),vo.getDriver());
        if(connection==null){
            return fail("连接数据库失败！");
        }
        getColumns(vo.getTableName(),connection);
        return success(vo);
    }

    public void getColumns(String tableName,Connection connection){
        try {
            DatabaseMetaData databaseMetaData=connection.getMetaData();
            ResultSet resultSet=databaseMetaData.getTables(null,"%","%",new String[]{"TABLE"});
            while (resultSet.next()){
                String name=resultSet.getString("TABLE_NAME");
                if(tableName.equals(name)){
                    ResultSet columns=databaseMetaData.getColumns(null,"%",tableName,"%");
                    StringBuffer cols=new StringBuffer();
                    StringBuffer pros=new StringBuffer();
                    StringBuffer clazz=new StringBuffer();
                    StringBuffer update=new StringBuffer();
                    while (columns.next()){
                        String columnName=columns.getString("COLUMN_NAME");
                        String columnType=columns.getString("TYPE_NAME");
                        String remark=columns.getString("REMARKS");
                        String privateName= DataSourcesUtils.underlineToCamel(columnName);
                        clazz.append("/** "+remark+" */\n");
                        if("timestamp".equals(columnType.toLowerCase())){
                            clazz.append("private Date "+privateName+";\n");
                        }else if("numeric".equals(columnType.toLowerCase())){
                            clazz.append("private BigDecimal "+privateName+";\n");
                        }else{
                            clazz.append("private String "+privateName+";\n");
                        }
                    }
                    log.info(clazz.toString());
                    break;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
        }
    }
}
