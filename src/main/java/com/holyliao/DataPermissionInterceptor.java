package com.holyliao;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.holyliao.visitor.SelectVisitorImpl;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;

/**
 * @author liaoqixing
 * @description 数据权限拦截器
 * @create 2018-04-24 下午3:12
 */
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
        }
)
public class DataPermissionInterceptor implements Interceptor {
    private final static Logger logger = LoggerFactory.getLogger(DataPermissionInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];
        String sql = mappedStatement.getBoundSql(parameter).getSql();
        //TODO:从当前线程获取需要进行数据权限控制的业务
        DataPermission dataPermission = DPHelper.getLocalDataPermissions();
        if (dataPermission != null && dataPermission.getAdmin() == false) {
            //获得方法类型
            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            if ("SELECT".equals(sqlCommandType)) {
                Select select = (Select) CCJSqlParserUtil.parse(sql);
                //访问各个visitor
                select.getSelectBody().accept(new SelectVisitorImpl());
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
