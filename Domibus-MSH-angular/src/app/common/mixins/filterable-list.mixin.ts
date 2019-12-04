/**
 * @author Ion Perpegel
 * @since 4.1
 * A mixin for components that display a list of items that can be filtered
 */
import {Constructable} from '../base-list.component';
import {OnInit} from '@angular/core';

let FilterableListMixin = (superclass: Constructable) => class extends superclass implements OnInit {
  public filter: any;
  public activeFilter: any;

  constructor(...args) {
    super(...args);
    this.filter = {};
  }

  ngOnInit() {
    if (super.ngOnInit) super.ngOnInit();

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
  protected search() {
  }

  /**
   * The method is trying to call the search if the component doesn't have unsaved changes, otherwise raises a popup to the client
   */
  public async trySearch(): Promise<boolean> {
    const canSearch = super.hasMethod('canProceed') ? await this.canProceed() : true;
    if (canSearch) {
      this.setActiveFilter();
      this.search();
    }
    return canSearch;
  }
};

export default FilterableListMixin;
