import {IBaseList} from './ibase-list';
import {IPageableList} from './ipageable-list';
import {IFilterableList} from './ifilterable-list';
import {IModifiableList} from './imodifiable-list';
import {ISortableList} from './isortable-list';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Helper methods for components with multiple mixins applied.
 * */

export function instanceOfIBaseList(object: any): object is IBaseList<any> {
  return 'rows' in object && 'count' in object && 'csvUrl' in object;
}

export function instanceOfPageableList(object: any): object is IPageableList {
  return 'offset' in object && 'changePageSize' in object && 'onPageSizeChanging' in object;
}

export function instanceOfFilterableList(object: any): object is IFilterableList {
  return 'filterData' in object && 'tryFilter' in object && 'activeFilter' in object;
}

export function instanceOfModifiableList(object: any): object is IModifiableList {
  return 'isDirty' in object && 'save' in object && 'saveIfNeeded' in object;
}

export function instanceOfSortableList(object: any): object is ISortableList {
  return 'onSort' in object && 'orderBy' in object;
}

