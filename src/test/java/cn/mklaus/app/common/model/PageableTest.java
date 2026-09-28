package cn.mklaus.app.common.model;

import cn.mklaus.app.common.exception.CommonErrorCode;
import cn.mklaus.app.common.exception.ErrorCodeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分页约定用测试钉住：CLAUDE.md 承诺 curPage 从 1 开始、pageSize 默认 10。
 * 这类"约定值"靠静态检查抓不到，只能靠测试。
 *
 * @author klaus
 * @since 2026/9/28
 */
class PageableTest {

    @Test
    void shouldUseDocumentedDefaults() {
        Pageable pageable = new Pageable();

        assertEquals(Integer.valueOf(1), pageable.getCurPage());
        assertEquals(Integer.valueOf(10), pageable.getPageSize());
        assertTrue(pageable.getNeedTotal());
    }

    @Test
    void offsetShouldStartAtZeroOnFirstPage() {
        Pageable pageable = new Pageable();
        pageable.setCurPage(3);
        pageable.setPageSize(20);

        assertEquals(40, pageable.getOffset());
    }

    @Test
    void shouldRejectIllegalPagingParams() {
        Pageable pageable = new Pageable();

        pageable.setPageSize(0);
        ErrorCodeException sizeException = assertThrows(ErrorCodeException.class, pageable::getOffset);
        assertEquals(CommonErrorCode.PAGE_SIZE_ILLEGAL, sizeException.getErrorCode());

        pageable.setPageSize(10);
        pageable.setCurPage(0);
        ErrorCodeException pageException = assertThrows(ErrorCodeException.class, pageable::getOffset);
        assertEquals(CommonErrorCode.CUR_PAGE_ILLEGAL, pageException.getErrorCode());
    }

    @Test
    void shouldRejectNullPagingParamsInsteadOfThrowingNpe() {
        Pageable pageable = new Pageable();
        pageable.setPageSize(null);

        ErrorCodeException exception = assertThrows(ErrorCodeException.class, pageable::getOffset);
        assertEquals(CommonErrorCode.PAGE_SIZE_ILLEGAL, exception.getErrorCode());
    }

}
