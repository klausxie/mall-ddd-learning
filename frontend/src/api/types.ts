/** 通用响应模型，对应后端 common/model/Page.java。 */
export interface Page<T> {
  readonly curPage: number;
  readonly pageSize: number;
  readonly total: number;
  readonly records: T[];
}

/** 分页请求参数，对应后端 common/model/Pageable.java（字段名固定 curPage / pageSize）。 */
export type PageQuery = {
  readonly curPage?: number;
  readonly pageSize?: number;
};
