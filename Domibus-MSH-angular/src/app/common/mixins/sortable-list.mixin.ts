import {RowLimiterBase} from '../row-limiter/row-limiter-base';
import {Constructable} from '../base-list.component';

/**
 * A mixin for components that display a list of items that can be ordered
 * More functionality will be added
 *
 * @since 4.1
 */

let SortableListMixin = (superclass: Constructable) => class extends superclass {
  public orderBy: string;
  public asc: boolean;
  public rowLimiter: RowLimiterBase;

  constructor(...args) {
    super(...args);
  }

  /**
   * The method is abstract so the derived, actual components implement it
   */
  public page(offset, pageSize) {
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
};

export default SortableListMixin;
