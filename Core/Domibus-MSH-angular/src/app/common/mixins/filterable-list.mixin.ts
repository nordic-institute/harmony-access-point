import {Constructable} from './base-list.component';
import {OnInit} from '@angular/core';
import {instanceOfPageableList} from './type.utils';
import {IFilterableList} from './ifilterable-list';
import {HttpParams} from '@angular/common/http';
import {PaginationType} from './ipageable-list';
import {AbstractControl, NgForm} from '@angular/forms';
import {SecurityService} from '../../security/security.service';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * A mixin for components that display a list of items that can be filtered
 */
let FilterableListMixin = (superclass: Constructable) => class extends superclass
  implements IFilterableList, OnInit {

  private initialFilter: any;

  public filterForm: NgForm;

  public filter: any;
  public activeFilter: any;

  public advancedSearch: boolean;

  public advancedFilters: Set<string>;

  constructor(...args) {
    super(...args);
    this.filter = {};
    this.advancedFilters = new Set<string>();
  }

  ngOnInit() {
    if (super.ngOnInit) {
      super.ngOnInit();
    }

    this.filter = {};
    this.activeFilter = {};
  }

  /**
   * The method is trying to call the search if the component doesn't have unsaved changes, otherwise raises a popup to the client
   */
  public async tryFilter(userInitiated = true): Promise<boolean> {
    if (userInitiated) {
      this.alertService.clearAlert();
      this.trimFields();
    }

    const canFilter = await this.canProceedToFilter();
    if (canFilter) {
      // this.onBeforeFilter();
      try {
        await this.filterData();
        return true;
      } catch (e) {
        return false;
      }
    }
    return false;
  }

  /**
   * The method is called from code when entering a page
   */
  public filterData(): Promise<any> {
    if (!this.initialFilter) {
      this.initialFilter = this.clone(this.filter);
    }

    this.onBeforeFilter();
    this.setActiveFilter();

    if (instanceOfPageableList(this)) {
      this.offset = 0;
    }

    return this.loadServerData();
  }

  public onResetAdvancedSearchParams() {
  }

  public resetAdvancedSearchParams() {
    this.advancedFilters.forEach(filterName => {
      if (typeof this.filter[filterName] === 'boolean') {
        this.filter[filterName] = false;
      } else {
        this.filter[filterName] = null;
      }
    });
    this.onResetAdvancedSearchParams();
  }

  resetFiltersToInitial() {
    this.filter = {};
    Object.assign(this.filter, this.initialFilter);
    this.onSetFilters();

    this.filterData();
  }

  protected createAndSetParameters(): HttpParams {
    let filterParams = super.createAndSetParameters();

    Object.keys(this.activeFilter).forEach((key: string) => {
      let value = this.activeFilter[key];
      if (typeof value === 'boolean') {
        filterParams = filterParams.append(key, value.toString());
      } else if (value) {
        if (value instanceof Date) {
          filterParams = filterParams.append(key, value.toISOString());
        } else if (value instanceof Array) {
          value.forEach(el => filterParams = filterParams.append(key, el));
        } else {
          filterParams = filterParams.append(key, value);
        }
      }
    });

    return filterParams;
  }

  /**
   * The method takes the filter params set through widgets and copies them to the active params
   * active params are the ones that are used for actual filtering of data and can be different from the ones set by the user in the UI
   */
  public setActiveFilter() {
    this.activeFilter = {};
    Object.assign(this.activeFilter, this.filter);
  }

  /**
   * The method takes the actual filter params and copies them to the UI bound params thus synchronizing the pair so what you see it is what you get
   */
  public resetFilters() {
    this.filter = {};
    Object.assign(this.filter, this.activeFilter);
    this.onSetFilters();
  }

  protected async canProceedToFilter(): Promise<boolean> {
    let securityService = this.applicationService.injector.get(SecurityService);
    return securityService.canAbandonUnsavedChanges(this);
  }

  canSearch(): boolean | Promise<boolean> {
    return !super.isBusy();
  }

  canResetSearch(): boolean | Promise<boolean> {
    return !super.isBusy();
  }

  private trimFields() {
    if (!this.filterForm) {
      console.warn('filterForm is null! exiting.');
      return;
    }
    for (const field in this.filterForm.controls) {
      const control: AbstractControl = this.filterForm.controls[field];
      if (control.value && typeof control.value == 'string') {
        const val = String.prototype.trim.apply(control.value);
        control.setValue(val);
      }
    }
  }

  protected onBeforeFilter() {
  }

  protected onSetFilters() {
  }

  clone(obj) {
    // Handle the 3 simple types, and null or undefined
    if (null == obj || 'object' != typeof obj) {
      return obj;
    }

    // Handle Date
    if (obj instanceof Date) {
      const copy = new Date();
      copy.setTime(obj.getTime());
      return copy;
    }

    // Handle Array
    if (obj instanceof Array) {
      const copy = [];
      for (let i = 0, len = obj.length; i < len; i++) {
        copy[i] = this.clone(obj[i]);
      }
      return copy;
    }

    // Handle Object
    if (obj instanceof Object) {
      const copy = {};
      for (let attr in obj) {
        if (obj.hasOwnProperty(attr)) {
          copy[attr] = this.clone(obj[attr]);
        }
      }
      return copy;
    }

    throw new Error('Unable to copy obj! Its type isn\'t supported.');
  }

};
export default FilterableListMixin;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that can be filtered on the client side(party, plugin users, users)
 */
export let ClientFilterableListMixin = (superclass: Constructable) => class extends FilterableListMixin(superclass) {
  constructor(...args) {
    super(...args);
    super.type = PaginationType.Client;
  }
};

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that can be filtered on the server side(messages, etc)
 */
export let ServerFilterableListMixin = (superclass: Constructable) => class extends FilterableListMixin(superclass) {
  constructor(...args) {
    super(...args);
    super.type = PaginationType.Client;
  }
};
