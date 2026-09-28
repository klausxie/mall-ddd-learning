package cn.mklaus.app.infrastructure.persistence;

import cn.mklaus.app.domain.user.Mobile;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 值对象 {@link Mobile} 与数据库 varchar 列之间的桥。
 *
 * <p>
 * 注册方式见 {@code application.yaml} 的 {@code mybatis-plus.type-handlers-package}，
 * 注册后 Mapper 里可以直接用 {@code Mobile} 当参数/返回值，不需要在 XML 里写任何转换。
 *
 * <p>
 * 从库里读出来时也会走构造器校验：数据脏了要立刻炸，而不是带着坏数据继续跑。
 *
 * @author klaus
 * @since 2026/9/29
 */
@MappedTypes(Mobile.class)
public class MobileTypeHandler extends BaseTypeHandler<Mobile> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Mobile parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, parameter.value());
    }

    @Override
    public Mobile getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toMobile(rs.getString(columnName));
    }

    @Override
    public Mobile getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toMobile(rs.getString(columnIndex));
    }

    @Override
    public Mobile getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toMobile(cs.getString(columnIndex));
    }

    private static Mobile toMobile(String value) {
        return value == null ? null : new Mobile(value);
    }

}
