/**
 * @author Ion Perpegel
 * @since 4.1
 * A mixin for components that display a list of items that can be filtered
 */
import {Constructable} from './base-list.component';
import {OnInit} from '@angular/core';
import {PaginationType} from './Ipageable-list';

let FilterableListMixin = (superclass: Constructable) => class extends superclass implements OnInit {
  public filter: any;
  public activeFilter: any;

  constructor(...args) {
    super(...args);
    this.filter = {};
  }

  ngOnInit() {
    if (super.ngOnInit) {
      super.ngOnInit();
    }

    this.filter = {};
    this.activeFilter = {};
  }

  /**
   * The method takes the filter params set through widgets and copies them to the active params
   * active params are the ones that are used for actual filtering of data and can be different from the ones set by the user in the UI
   */
  protected setActiveFilter() {
    //just in case ngOnInit wasn't called from corresponding component class
    if (!this.activeFilter) {
      this.activeFilter = {};
    }
    Object.assign(this.activeFilter, this.filter);
  }

  /**
   * The method takes the actual filter params and copies them to the UI bound params thus synchronizing the pair so what you see it is what you get
   */
  protected resetFilters() {
    this.filter = {};
    Object.assign(this.filter, this.activeFilter);
  }

  /**
   * The method is supposed to be overridden in derived classes to implement actual search
   */
  public search() {
    if (super.hasMethod('page')) {
      super.offset = 0;
      this.page();
    }
  }

  /**
   * The method is trying to call the search if the component doesn't have unsaved changes, otherwise raises a popup to the client
   */
  public async trySearch(): Promise<boolean> {
    const canSearch = await this.canProceedToSearch();
    if (canSearch) {
      this.setActiveFilter();
      this.search();
    }
    return canSearch;
  }

  private canProceedToSearch(): Promise<boolean> {
    if (super.hasMethod('isDirty') && this.isDirty()) {
      return this.dialogsService.openCancelDialog();
    }
    return Promise.resolve(true);
  }

};

export default FilterableListMixin;
