package cn.mklaus.app.domain.user.points;

import cn.mklaus.app.domain.user.User;
import org.springframework.util.Assert;

/**
 * 注册赠送积分策略：低于 {@link #SENIOR_AGE} 岁送 {@link #JUNIOR_POINTS}，否则送 {@link #SENIOR_POINTS}。
 *
 * <p>
 * 它回答的是"送多少"而不是"是否满足约束"，任何年龄都有结果、没有失败态，
 * 因此是策略（Policy）而不是规格（Spec）。纯计算、无依赖，调用方直接 {@code new} 即可，
 * 不需要注册成 Bean。
 *
 * @author klaus
 * @since 2026/9/28
 */
public class RegistrationPointsPolicy {

    /**
     * 高年龄段起始年龄
     */
    public static final int SENIOR_AGE = 30;

    /**
     * 低年龄段赠送积分
     */
    public static final int JUNIOR_POINTS = 200;

    /**
     * 高年龄段赠送积分
     */
    public static final int SENIOR_POINTS = 300;

    /**
     * 计算注册赠送的积分。
     *
     * <p>
     * 注册流程已由 {@code UserMustBeAdultSpec} 保证年龄存在，这里对缺失年龄直接报错而不是猜一个档位。
     */
    public int pointsFor(User user) {
        Assert.notNull(user.getAge(), "用户年龄不能为空，无法计算注册积分");
        return user.getAge() >= SENIOR_AGE ? SENIOR_POINTS : JUNIOR_POINTS;
    }

}
