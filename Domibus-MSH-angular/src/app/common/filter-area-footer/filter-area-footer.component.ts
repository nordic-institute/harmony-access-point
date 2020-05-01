import {Component, Input} from '@angular/core';
import BaseListComponent from '../mixins/base-list.component';
import {IFilterableList} from '../mixins/ifilterable-list';

@Component({
  selector: 'filter-area-footer',
  templateUrl: './filter-area-footer.component.html',
})
export class FilterAreaFooterComponent {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any> & IFilterableList;

  @Input()
  isAdvancedVisible = true;

  toggleAdvancedSearch() {
    this.parent.advancedSearch = true;
    return false; // to prevent default navigation
  }

  toggleBasicSearch() {
    this.parent.advancedSearch = false;

    this.parent.resetAdvancedSearchParams();
    return false;
  }

  canSearch() {
    const form = this.parent.filterForm;
    if (!form) {
      return true;
    }
    return !form.invalid;
  }
}
