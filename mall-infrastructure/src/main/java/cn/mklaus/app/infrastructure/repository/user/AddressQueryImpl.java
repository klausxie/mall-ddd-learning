package cn.mklaus.app.infrastructure.repository.user;

import cn.mklaus.app.application.user.model.AddressDTO;
import cn.mklaus.app.application.user.query.AddressQuery;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.common.model.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Component
public class AddressQueryImpl implements AddressQuery {

    @Override
    public Page<AddressDTO> pageAddress(long userId, Pageable pageable) {
        return null;
    }

}
