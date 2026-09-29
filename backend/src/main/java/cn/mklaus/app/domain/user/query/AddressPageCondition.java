package cn.mklaus.app.domain.user.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Data
@Builder
public class AddressPageCondition {

    private Long userId;
    private Integer offset;
    private Integer size;

}
