package cn.mklaus.app.domain;

import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.AddressMapper;
import cn.mklaus.app.domain.user.Mobile;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserMapper;
import cn.mklaus.app.domain.user.query.AddressPageCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mapper 的真库冒烟测试：证明 XML 里的 SQL 与 Flyway 迁移建的表结构真的对得上。
 *
 * <p>
 * 默认跳过（需要能连上数据库），显式提供密码时才跑：
 *
 * <pre>
 * MALL_DB_PASSWORD=xxx ./mvnw test -Dtest=MapperSmokeTest
 * </pre>
 *
 * <p>
 * 类上的 {@link Transactional} 让每个用例结束后自动回滚，不会往库里留数据。
 * 依赖用构造器注入，与项目"禁止字段注入注解"的规范保持一致。
 *
 * @author klaus
 * @since 2026/9/28
 */
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@EnabledIfEnvironmentVariable(named = "MALL_DB_PASSWORD", matches = ".+")
class MapperSmokeTest {

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    MapperSmokeTest(UserMapper userMapper, AddressMapper addressMapper) {
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
    }

    @Test
    void userMapperShouldRoundTrip() {
        String mobile = uniqueMobile();

        User user = new User();
        user.setMobile(new Mobile(mobile));
        user.setPassword("pbkdf2$600000$c2FsdA==$aGFzaA==");
        user.setNickname("smoke");
        user.setAge(20);

        userMapper.saveUser(user);
        assertNotNull(user.getId(), "saveUser 应回填自增主键");
        // 值对象要能原样往返：Mobile 有值语义，所以这里直接比对象而不是比字符串
        assertEquals(new Mobile(mobile), userMapper.getUser(user.getId()).orElseThrow().getMobile());
        assertEquals(user.getId(), userMapper.getUserByMobile(new Mobile(mobile)).orElseThrow().getId());

        user.setNickname("smoke-updated");
        userMapper.updateUser(user);
        assertEquals("smoke-updated", userMapper.getUser(user.getId()).orElseThrow().getNickname());
    }

    @Test
    void addressMapperShouldRoundTripAndPage() {
        long userId = Math.abs(System.nanoTime());

        Address address = Address.builder()
            .userId(userId)
            .recipient("收件人")
            .phone("13900000000")
            .province("广东省")
            .city("深圳市")
            .district("南山区")
            .detail("科技园 1 号")
            .build();

        addressMapper.saveAddress(address);
        assertNotNull(address.getId(), "saveAddress 应回填自增主键");
        assertEquals("收件人", addressMapper.getAddress(address.getId()).orElseThrow().getRecipient());

        AddressPageCondition cnd = AddressPageCondition.builder().userId(userId).offset(0).size(10).build();
        assertEquals(1, addressMapper.listAddress(cnd).size());
        assertEquals(1L, addressMapper.countAddress(cnd));

        address.setDetail("科技园 2 号");
        addressMapper.updateAddress(address);
        assertEquals("科技园 2 号", addressMapper.getAddress(address.getId()).orElseThrow().getDetail());

        addressMapper.removeAddress(address);
        assertTrue(addressMapper.getAddress(address.getId()).isEmpty());
    }

    private static String uniqueMobile() {
        return "139" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }

}
