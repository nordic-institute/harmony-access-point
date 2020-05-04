import {FormGroup, NgForm} from '@angular/forms';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the list with pagination ( client or server)
 * */
export interface IFilterableList {
  filterForm: NgForm;
  filter: any;
  activeFilter: any;
  advancedSearch: boolean;
  advancedFilters: Set<string>;

  filterData();

  tryFilter(): Promise<boolean>;

  setActiveFilter();

  resetFilters();

  resetAdvancedSearchParams();

  canSearch(): boolean | Promise<boolean>;
}

