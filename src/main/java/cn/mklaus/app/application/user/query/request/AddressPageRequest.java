package cn.mklaus.app.application.user.query.request;

import cn.mklaus.app.common.model.Pageable;
import cn.mklaus.app.domain.user.query.AddressPageCondition;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public class AddressPageRequest extends Pageable {

    public AddressPageCondition buildCondition() {
        return AddressPageCondition.builder()
            .offset(getOffset())
            .size(getPageSize())
            .build();
    }

}
