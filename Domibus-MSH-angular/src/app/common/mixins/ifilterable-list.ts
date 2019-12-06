import {RowLimiterBase} from '../row-limiter/row-limiter-base';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the list with pagination ( client or server)
 * */
export interface IFilterableList {
  filter: any;
  activeFilter: any;

  search();

  trySearch(): Promise<boolean>;

  setActiveFilter();

  resetFilters();
}

