import {RowLimiterBase} from '../row-limiter/row-limiter-base';

type CallbackFunction = (newPageLimit: number) => Promise<boolean>;

export interface IPageableList {
  offset: number;
  rowLimiter: RowLimiterBase;
  onPageSizeChanging: CallbackFunction
}
