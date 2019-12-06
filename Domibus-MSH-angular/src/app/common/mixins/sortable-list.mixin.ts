import {Constructable} from './base-list.component';
import {instanceOfPageableList} from './type.utils';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * A mixin for components that display a list of items that can be ordered
 * */

let SortableListMixin = (superclass: Constructable) => class extends superclass {
  public orderBy: string;
  public asc: boolean;

  constructor(...args) {
    super(...args);
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

    let orderBy = event.column.prop;
    let asc = (event.newValue === 'desc') ? false : true;

    const success = await this.reload();

    if (success) {
      this.orderBy = orderBy;
      this.asc = asc;
    }
  }

  public async reload() {
    if (instanceOfPageableList(this)) {
      return this.resetPage();
    }
    return true;
  }
};

export default SortableListMixin;
