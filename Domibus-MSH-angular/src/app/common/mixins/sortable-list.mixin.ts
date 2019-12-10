import {Constructable} from './base-list.component';
import {instanceOfPageableList} from './type.utils';
import {IFilterableList} from './ifilterable-list';
import {OnInit} from '@angular/core';
import {ISortableList} from './isortable-list';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * A mixin for components that display a list of items that can be ordered
 * */

let SortableListMixin = (superclass: Constructable) => class extends superclass
  implements ISortableList, OnInit {

  public orderBy: string;
  public asc: boolean;

  // activeOrderBy: string;
  // activeAsc: boolean;

  constructor(...args) {
    super(...args);
  }

  ngOnInit() {
    if (super.ngOnInit) {
      super.ngOnInit();
    }
  }

  /**
   * The method is abstract so the derived, actual components implement it
   * It is called by the infrastructure/mixin just before calling the reload data method, after setting the oredBy and asc parameters
   */
  public onBeforeSort() {
  }

  /**
   * The method is called from grid sorting and it is referred in the grid params as it is visible in the derived components
   */
  public onSort(event) {
    this.doSort(event);
  }

  public async doSort(event) {
    this.onBeforeSort();

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

  public async reload() {
    if (instanceOfPageableList(this)) {
      return this.resetPage();
    }
    return true;
  }

  protected onSetParameters() {
    super.onSetParameters();

    let params = this.GETParams;

    if (this.orderBy) {
      params = params.append('orderBy', this.orderBy);
    }
    if (this.asc != null) {
      params = params.append('asc', this.asc);
    }

    super.GETParams = params;
  }
};

export default SortableListMixin;
