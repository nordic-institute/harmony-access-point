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
  advancedSearch: boolean;
  advancedFilters: Set<string>;

  filterData();

  tryFilter(): Promise<boolean>;

  setActiveFilter();

  resetFilters();

  resetAdvancedSearchParams();

}

