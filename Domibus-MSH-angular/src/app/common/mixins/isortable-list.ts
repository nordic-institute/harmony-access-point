
/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the sortable lists ( client or server)
 * */
export interface ISortableList {
  orderBy: string;
  asc: boolean;

  sortedColumns: { prop: string, dir: string }[];

  onSort(event);
}
