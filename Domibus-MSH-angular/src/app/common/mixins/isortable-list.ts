import {RowLimiterBase} from '../row-limiter/row-limiter-base';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the list with pagination ( client or server)
 * */
export interface ISortableList {
  orderBy: string;
  asc: boolean;

  sortedColumns: { prop: 'timestamp', dir: 'desc' }[];

  onSort(event);

  doSort(event);
}
