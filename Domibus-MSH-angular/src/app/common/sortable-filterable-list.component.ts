import {Component, OnInit} from '@angular/core';
import {FilterableListComponent} from './filterable-list.component';
import {RowLimiterBase} from './row-limiter/row-limiter-base';

/**
 * Base class for components that display a list of items that can be ordered and paged
 * It is an embryo; more common functionality will be added in time
 *
 * @since 4.1
 */

export abstract class SortableFilterableListComponent extends FilterableListComponent implements OnInit {
  public orderBy: string;
  public asc: boolean;
  public rowLimiter: RowLimiterBase;

  protected constructor() {
    super();
  }

  /**
   * The method is abstract so the derived, actual components implement it
   */
  public abstract page(offset, pageSize) ;

  ngOnInit() {
  }

  /**
   * The method is called from grid sorting and it is referred in the grid params as it is visible in the derived components
   */
  public onSort(event) {
    this.doSort(event);
  }

  doSort(event) {
    this.orderBy = event.column.prop;
    this.asc = (event.newValue === 'desc') ? false : true;

    this.page(0, this.rowLimiter.pageSize);
  }
}
