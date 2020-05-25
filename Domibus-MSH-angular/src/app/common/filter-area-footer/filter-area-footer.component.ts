import {AfterViewInit, Component, Input} from '@angular/core';
import BaseListComponent from '../mixins/base-list.component';
import {IFilterableList} from '../mixins/ifilterable-list';
import {NgForm} from '@angular/forms';

@Component({
  selector: 'filter-area-footer',
  templateUrl: './filter-area-footer.component.html',
})
export class FilterAreaFooterComponent implements AfterViewInit {

  constructor() {
  }

  @Input()
  parent: BaseListComponent<any> & IFilterableList;

  @Input()
  form: NgForm;

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
    const canSearch = this.parent.canSearch();
    
    const form = this.form;
    if (!form) {
      return canSearch;
    }
    return !form.invalid && canSearch;
  }

  ngAfterViewInit(): void {
    this.parent.filterForm = this.form;
  }
}
