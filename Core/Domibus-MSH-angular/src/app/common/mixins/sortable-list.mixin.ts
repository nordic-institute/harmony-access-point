import {Constructable} from './base-list.component';
import {instanceOfFilterableList, instanceOfPageableList} from './type.utils';
import {OnInit} from '@angular/core';
import {ISortableList} from './isortable-list';
import {HttpParams} from '@angular/common/http';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * A mixin for components that display a list of items that are sorted on client
 */
export let ClientSortableListMixin = (superclass: Constructable) => class extends SortableListMixin(superclass)
  implements ISortableList, OnInit {

  constructor(...args) {
    super(...args);
  }

  /**
   * The method is called from grid sorting and just resets the filter params
   */
  public onSort(event) {
    if (instanceOfFilterableList(this)) {
      this.resetFilters();
    }
  }
};

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * A mixin for components that display a list of items that can be sorted on server
 * */
export let ServerSortableListMixin = (superclass: Constructable) => class extends SortableListMixin(superclass)
  implements ISortableList, OnInit {

  constructor(...args) {
    super(...args);
  }

  /**
   * The method is called from grid sorting and load data from server in specified order
   */
  public onSort(event) {
    this.doSort(event);
  }

  private async doSort(event) {
    let previousOrderBy = this.orderBy;
    let previousAsc = this.asc;

    this.orderBy = event.column.prop;
    this.asc = (event.newValue === 'desc') ? false : true;

    const success = await this.reload();

    if (!success) {
      this.orderBy = previousOrderBy;
      this.asc = previousAsc;
    }
  }

  private async reload() {
    if (instanceOfPageableList(this)) {
      return this.resetPage();
    }
    return true;
  }

  protected createAndSetParameters(): HttpParams {
    let params = super.createAndSetParameters();

    if (this.orderBy) {
      params = params.append('orderBy', this.orderBy);
    }
    if (this.asc != null) {
      params = params.append('asc', this.asc);
    }

    return params;
  }
};

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * A mixin for components that display a list of items that can be sorted on server
 * */
let SortableListMixin = (superclass: Constructable) => class extends superclass
  implements ISortableList, OnInit {

  public orderBy: string;
  public asc: boolean;

  public sortedColumns: { prop: string, dir: string }[];

  constructor(...args) {
    super(...args);
  }

  ngOnInit() {
    if (super.ngOnInit) {
      super.ngOnInit();
    }

    this.sortedColumns = [];
  }

  /**
   * The method is called from grid sorting
   */
  public onSort(event) {
  }

};

