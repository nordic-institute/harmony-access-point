import {RowLimiterBase} from '../row-limiter/row-limiter-base';

type CallbackFunction = (newPageLimit: number) => Promise<boolean>;

export enum PaginationType {
  Server,
  Client,
}

export interface IPageableList {
  type: PaginationType;
  offset: number;
  rowLimiter: RowLimiterBase;
  onPageSizeChanging: CallbackFunction;

  changePageSize(newPageLimit: number);
}
