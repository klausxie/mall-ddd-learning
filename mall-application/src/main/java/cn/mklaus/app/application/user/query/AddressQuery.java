package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.model.AddressDTO;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.common.model.Pageable;

/**
 * @author klausxie
 * @since 2023/8/20
 */
public interface AddressQuery {

    Page<AddressDTO> pageAddress(long userId, Pageable pageable);

}
